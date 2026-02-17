package cssminifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssMinifier {

    private static boolean isEscaped(String s, int pos) {
        int backslashes = 0;
        for (int k = pos - 1; k >= 0 && s.charAt(k) == '\\'; k--) backslashes++;
        return (backslashes % 2) != 0;
    }

    private static java.util.List<String> splitDeclarations(String block) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        char stringChar = 0;
        for (int i = 0; i < block.length(); i++) {
            char c = block.charAt(i);
            if (inString) {
                current.append(c);
                if (c == stringChar && !isEscaped(block, i)) inString = false;
                continue;
            }
            if (c == '"' || c == '\'') { inString = true; stringChar = c; }
            if (c == ';' && !inString) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    public static String minify(String css) {
        String result = stripComments(css);
        result = collapseWhitespace(result);
        result = optimizeValues(result);
        result = optimizeQuotedTokens(result);
        result = collapseShorthand(result);
        result = removeDuplicateProperties(result);
        result = mergeAdjacentRules(result);
        return result;
    }

    private static String stripComments(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inComment = false;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);
            char next = i + 1 < css.length() ? css.charAt(i + 1) : 0;

            if (!inString && !inComment && c == '/' && next == '*') {
                // Check for /*! license comment — preserve it
                if (i + 2 < css.length() && css.charAt(i + 2) == '!') {
                    int end = css.indexOf("*/", i + 3);
                    if (end != -1) {
                        result.append(css, i, end + 2);
                        i = end + 1; // will be incremented by loop
                        continue;
                    }
                }
                inComment = true;
                i++;
                continue;
            }
            if (inComment) {
                if (c == '*' && next == '/') {
                    inComment = false;
                    i++;
                }
                continue;
            }

            if (inString) {
                result.append(c);
                if (c == stringChar && !isEscaped(css, i)) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
            }

            result.append(c);
        }

        return result.toString();
    }

    private static boolean isStripChar(char c, int braceDepth, int parenDepth) {
        // Always strip around these
        if (c == '{' || c == '}' || c == ';' || c == ',') return true;
        // Strip around ':' inside declarations (braces) or inside parens (media queries, @supports)
        // but NOT in selectors where it precedes pseudo-classes
        if (c == ':' && (braceDepth > 0 || parenDepth > 0)) return true;
        // Strip around combinators '>' '+' '~' only outside parentheses
        // (inside parens, '+' and '-' are math operators in calc/min/max/clamp)
        if ((c == '>' || c == '+' || c == '~') && parenDepth == 0) return true;
        // Strip around '*' '/' inside parentheses (calc operators — spaces optional per spec)
        if ((c == '*' || c == '/') && parenDepth > 0) return true;
        // Strip space before '!' (for !important: "red !important" -> "red!important")
        if (c == '!' && braceDepth > 0) return true;
        return false;
    }

    private static String collapseWhitespace(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;
        int braceDepth = 0;
        int parenDepth = 0;

        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);

            if (inString) {
                result.append(c);
                if (c == stringChar && !isEscaped(css, i)) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                result.append(c);
                continue;
            }

            // Track brace and paren depth
            if (c == '{') braceDepth++;
            else if (c == '}') braceDepth--;
            else if (c == '(') parenDepth++;
            else if (c == ')') parenDepth--;

            if (Character.isWhitespace(c)) {
                char prev = result.length() > 0 ? result.charAt(result.length() - 1) : 0;
                if (isStripChar(prev, braceDepth, parenDepth)) {
                    continue;
                }
                int j = i + 1;
                while (j < css.length() && Character.isWhitespace(css.charAt(j))) j++;
                char nextNonWs = j < css.length() ? css.charAt(j) : 0;
                if (isStripChar(nextNonWs, braceDepth, parenDepth)) {
                    continue;
                }
                if (prev != ' ' && prev != 0) {
                    result.append(' ');
                }
                continue;
            }

            if (c == '}' && result.length() > 0 && result.charAt(result.length() - 1) == ';') {
                result.setLength(result.length() - 1);
            }

            result.append(c);
        }

        return result.toString().trim();
    }

    // Matches 6-digit hex where pairs are identical: #AABBCC (case-insensitive backrefs)
    private static final Pattern HEX6 = Pattern.compile(
        "#([0-9a-fA-F])\\1([0-9a-fA-F])\\2([0-9a-fA-F])\\3(?![0-9a-fA-F])",
        Pattern.CASE_INSENSITIVE);

    // Matches 8-digit hex where pairs are identical: #AABBCCDD (case-insensitive backrefs)
    private static final Pattern HEX8 = Pattern.compile(
        "#([0-9a-fA-F])\\1([0-9a-fA-F])\\2([0-9a-fA-F])\\3([0-9a-fA-F])\\4(?![0-9a-fA-F])",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern ZERO_UNIT = Pattern.compile(
        "(?<=[:\\s,(/])0(px|em|rem|pt|cm|mm|in|pc|ex|ch|vw|vh|vmin|vmax|deg|rad|turn|%)(?![0-9a-zA-Z%])");

    private static final Pattern LEADING_ZERO = Pattern.compile(
        "(?<=[:\\s,(/\\-])0(\\.\\d+)");

    private static final Pattern FONT_WEIGHT = Pattern.compile(
        "font-weight:(normal|bold)(?=[;}\"])");

    // 6. Keyframe from → 0%, 100% → to (only in keyframe context: preceded by { or })
    // NOTE: These patterns could theoretically match a `from` element selector directly
    // after `}` with no class/id prefix, but this never occurs in practice. A proper fix
    // would require structural keyframe-context parsing.
    private static final Pattern KEYFRAME_FROM = Pattern.compile(
        "(?<=[{}])from(?=\\s*[{,])");
    private static final Pattern KEYFRAME_100 = Pattern.compile(
        "(?<=[{}])100%(?=\\s*[{,])");

    // 7. translate3d(0,0,X) → translateZ(X) (any Z value, including 0)
    private static final Pattern TRANSLATE3D_Z = Pattern.compile(
        "translate3d\\(0,\\s*0,\\s*([^)]+)\\)");

    // 8. scale3d(1,1,1) → scaleX(1)
    private static final Pattern SCALE3D_IDENTITY = Pattern.compile(
        "scale3d\\(1,\\s*1,\\s*1\\)");

    // 9. rotate3d single-axis → rotate/rotateX/rotateY
    private static final Pattern ROTATE3D_Z = Pattern.compile(
        "rotate3d\\(0,\\s*0,\\s*1,\\s*([^)]+)\\)");
    private static final Pattern ROTATE3D_Y = Pattern.compile(
        "rotate3d\\(0,\\s*1,\\s*0,\\s*([^)]+)\\)");
    private static final Pattern ROTATE3D_X = Pattern.compile(
        "rotate3d\\(1,\\s*0,\\s*0,\\s*([^)]+)\\)");

    // 10. background:transparent/none → background:0 0
    private static final Pattern BACKGROUND_TRANSPARENT = Pattern.compile(
        "background:(transparent|none)(?=[;},!])");

    // 11. outline:none → outline:0
    private static final Pattern OUTLINE_NONE = Pattern.compile(
        "outline:none(?=[;},!])");

    static String optimizeValues(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;
        int segmentStart = 0;

        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);

            if (inString) {
                if (c == stringChar && !isEscaped(css, i)) {
                    inString = false;
                    // Append string verbatim (including closing quote)
                    result.append(css, segmentStart, i + 1);
                    segmentStart = i + 1;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                // Process the non-string segment before this string
                result.append(optimizeSegment(css.substring(segmentStart, i)));
                inString = true;
                stringChar = c;
                segmentStart = i; // string start (will be appended verbatim when string ends or at EOF)
                continue;
            }
        }

        // Process remaining segment
        if (segmentStart < css.length()) {
            if (inString) {
                result.append(css, segmentStart, css.length());
            } else {
                result.append(optimizeSegment(css.substring(segmentStart)));
            }
        }

        return result.toString();
    }

    private static boolean isInCustomProperty(String segment, int pos) {
        // Scan backward to find start of current declaration (after ; or { or start)
        int declStart = 0;
        for (int k = pos - 1; k >= 0; k--) {
            char ch = segment.charAt(k);
            if (ch == ';' || ch == '{') {
                declStart = k + 1;
                break;
            }
        }
        // Check if declaration starts with '--'
        return declStart + 1 < segment.length()
            && segment.charAt(declStart) == '-'
            && segment.charAt(declStart + 1) == '-';
    }

    private static String optimizeSegment(String segment) {
        // 1. Shorten 8-digit hex colors
        Matcher m8 = HEX8.matcher(segment);
        StringBuilder sb = new StringBuilder();
        while (m8.find()) {
            String replacement = "#" + m8.group(1).toLowerCase() + m8.group(2).toLowerCase()
                + m8.group(3).toLowerCase() + m8.group(4).toLowerCase();
            m8.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m8.appendTail(sb);
        segment = sb.toString();

        // 2. Shorten 6-digit hex colors
        Matcher m6 = HEX6.matcher(segment);
        sb = new StringBuilder();
        while (m6.find()) {
            String replacement = "#" + m6.group(1).toLowerCase() + m6.group(2).toLowerCase()
                + m6.group(3).toLowerCase();
            m6.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m6.appendTail(sb);
        segment = sb.toString();

        // 3. Remove units on zero values (skip keyframe selectors and custom properties)
        Matcher mz = ZERO_UNIT.matcher(segment);
        sb = new StringBuilder();
        while (mz.find()) {
            int afterMatch = mz.end();
            // Don't strip 0% when it's a keyframe selector (followed by '{')
            if (mz.group(1).equals("%") && afterMatch < segment.length() && segment.charAt(afterMatch) == '{') {
                mz.appendReplacement(sb, Matcher.quoteReplacement(mz.group()));
            // Don't strip units inside custom property declarations (--name:0px)
            } else if (isInCustomProperty(segment, mz.start())) {
                mz.appendReplacement(sb, Matcher.quoteReplacement(mz.group()));
            } else {
                mz.appendReplacement(sb, "0");
            }
        }
        mz.appendTail(sb);
        segment = sb.toString();

        // 4. Remove leading zeros from decimals (0.25 -> .25)
        Matcher mlz = LEADING_ZERO.matcher(segment);
        sb = new StringBuilder();
        while (mlz.find()) {
            mlz.appendReplacement(sb, Matcher.quoteReplacement(mlz.group(1)));
        }
        mlz.appendTail(sb);
        segment = sb.toString();

        // 5. Shorten font-weight keywords
        Matcher mfw = FONT_WEIGHT.matcher(segment);
        sb = new StringBuilder();
        while (mfw.find()) {
            String val = mfw.group(1).equals("bold") ? "font-weight:700" : "font-weight:400";
            mfw.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        mfw.appendTail(sb);
        segment = sb.toString();

        // 6. Keyframe from → 0%, 100% → to
        segment = KEYFRAME_FROM.matcher(segment).replaceAll("0%");
        segment = KEYFRAME_100.matcher(segment).replaceAll("to");

        // 7. translate3d(0,0,X) → translateZ(X)
        segment = TRANSLATE3D_Z.matcher(segment).replaceAll("translateZ($1)");

        // 8. scale3d(1,1,1) → scaleX(1)
        segment = SCALE3D_IDENTITY.matcher(segment).replaceAll("scaleX(1)");

        // 9. rotate3d single-axis → rotate/rotateX/rotateY
        segment = ROTATE3D_Z.matcher(segment).replaceAll("rotate($1)");
        segment = ROTATE3D_Y.matcher(segment).replaceAll("rotateY($1)");
        segment = ROTATE3D_X.matcher(segment).replaceAll("rotateX($1)");

        // 10. background:transparent/none → background:0 0
        segment = BACKGROUND_TRANSPARENT.matcher(segment).replaceAll("background:0 0");

        // 11. outline:none → outline:0
        segment = OUTLINE_NONE.matcher(segment).replaceAll("outline:0");

        return segment;
    }

    /**
     * Optimizes quoted tokens that span across what optimizeValues() considers string boundaries:
     * attribute selector quotes and url() quotes.
     */
    static String optimizeQuotedTokens(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;
        int i = 0;

        while (i < css.length()) {
            char c = css.charAt(i);

            if (inString) {
                result.append(c);
                if (c == stringChar && !isEscaped(css, i)) {
                    inString = false;
                }
                i++;
                continue;
            }

            // Try URL quote removal: url("...") → url(...)
            if (c == 'u' && css.startsWith("url(", i)) {
                int qPos = i + 4;
                if (qPos < css.length()) {
                    char q = css.charAt(qPos);
                    if (q == '"' || q == '\'') {
                        int closeQ = css.indexOf(q, qPos + 1);
                        if (closeQ > 0 && closeQ + 1 < css.length() && css.charAt(closeQ + 1) == ')') {
                            String content = css.substring(qPos + 1, closeQ);
                            if (!content.isEmpty() && content.indexOf(' ') < 0
                                    && content.indexOf('(') < 0 && content.indexOf(')') < 0
                                    && content.indexOf(';') < 0) {
                                result.append("url(").append(content).append(')');
                                i = closeQ + 2;
                                continue;
                            }
                        }
                    }
                }
            }

            // Try attribute selector quote removal: [attr="value"] → [attr=value]
            if (c == '[') {
                int j = i + 1;
                // Skip attribute name (letters, digits, hyphens)
                while (j < css.length() && (Character.isLetterOrDigit(css.charAt(j)) || css.charAt(j) == '-')) j++;
                // Check for optional operator (^, $, *, ~, |)
                if (j < css.length() && "^$*~|".indexOf(css.charAt(j)) >= 0) j++;
                // Check for =
                if (j < css.length() && css.charAt(j) == '=') {
                    j++;
                    if (j < css.length() && (css.charAt(j) == '"' || css.charAt(j) == '\'')) {
                        char q = css.charAt(j);
                        int closeQ = css.indexOf(q, j + 1);
                        if (closeQ > 0 && closeQ + 1 < css.length() && css.charAt(closeQ + 1) == ']') {
                            String value = css.substring(j + 1, closeQ);
                            if (isValidCssIdentifier(value)) {
                                result.append(css, i, j); // [attr=
                                result.append(value);      // value without quotes
                                result.append(']');
                                i = closeQ + 2;
                                continue;
                            }
                        }
                    }
                }
            }

            // Enter string mode for actual CSS strings
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    private static boolean isValidCssIdentifier(String value) {
        if (value.isEmpty()) return false;
        char first = value.charAt(0);
        if (!Character.isLetter(first) && first != '_' && first != '-') return false;
        if (first == '-') {
            if (value.length() < 2) return false;
            char second = value.charAt(1);
            if (!Character.isLetter(second) && second != '_' && second != '-') return false;
        }
        for (int k = 1; k < value.length(); k++) {
            char ch = value.charAt(k);
            if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '-') return false;
        }
        return true;
    }

    private static final String[] SIDES = {"top", "right", "bottom", "left"};

    static String collapseShorthand(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;
        int i = 0;

        while (i < css.length()) {
            char c = css.charAt(i);

            if (inString) {
                result.append(c);
                if (c == stringChar && !isEscaped(css, i)) {
                    inString = false;
                }
                i++;
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                result.append(c);
                i++;
                continue;
            }

            if (c == '{') {
                // Find matching closing brace
                int braceDepth = 1;
                int blockStart = i + 1;
                int j = blockStart;
                boolean blockInString = false;
                char blockStringChar = 0;
                while (j < css.length() && braceDepth > 0) {
                    char bc = css.charAt(j);
                    if (blockInString) {
                        if (bc == blockStringChar && !isEscaped(css, j)) {
                            blockInString = false;
                        }
                    } else if (bc == '"' || bc == '\'') {
                        blockInString = true;
                        blockStringChar = bc;
                    } else if (bc == '{') {
                        braceDepth++;
                    } else if (bc == '}') {
                        braceDepth--;
                    }
                    j++;
                }
                int blockEnd = j - 1; // index of '}'
                String block = css.substring(blockStart, blockEnd);

                if (block.contains("{")) {
                    // Recurse into nested blocks (e.g., @media)
                    block = collapseShorthand(block);
                } else {
                    block = collapseBlock(block, "margin");
                    block = collapseBlock(block, "padding");
                }

                result.append('{');
                result.append(block);
                result.append('}');
                i = blockEnd + 1;
                continue;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    private static String collapseBlock(String block, String property) {
        java.util.List<String> declarations = splitDeclarations(block);
        Map<String, String> sideValues = new LinkedHashMap<>();
        java.util.Set<String> sideProps = new java.util.HashSet<>();
        for (String side : SIDES) {
            sideProps.add(property + "-" + side);
        }

        for (String decl : declarations) {
            int colon = decl.indexOf(':');
            if (colon <= 0) continue;
            String prop = decl.substring(0, colon);
            if (sideProps.contains(prop)) {
                String side = prop.substring(property.length() + 1);
                sideValues.put(side, decl.substring(colon + 1).trim());
            }
        }

        if (sideValues.size() != 4) {
            return block;
        }

        String top = sideValues.get("top");
        String right = sideValues.get("right");
        String bottom = sideValues.get("bottom");
        String left = sideValues.get("left");

        // Build shorthand value
        String shorthand;
        if (top.equals(right) && right.equals(bottom) && bottom.equals(left)) {
            shorthand = top;
        } else if (top.equals(bottom) && right.equals(left)) {
            shorthand = top + " " + right;
        } else if (right.equals(left)) {
            shorthand = top + " " + right + " " + bottom;
        } else {
            shorthand = top + " " + right + " " + bottom + " " + left;
        }

        // Rebuild block without the 4 longhand declarations, then append shorthand
        StringBuilder sb = new StringBuilder();
        for (String decl : declarations) {
            int colon = decl.indexOf(':');
            if (colon > 0 && sideProps.contains(decl.substring(0, colon))) continue;
            if (decl.isEmpty()) continue;
            if (sb.length() > 0) sb.append(';');
            sb.append(decl);
        }

        if (sb.length() > 0) {
            sb.append(';').append(property).append(':').append(shorthand);
        } else {
            sb.append(property).append(':').append(shorthand);
        }

        return sb.toString();
    }

    static String removeDuplicateProperties(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;
        int i = 0;

        while (i < css.length()) {
            char c = css.charAt(i);

            if (inString) {
                result.append(c);
                if (c == stringChar && !isEscaped(css, i)) {
                    inString = false;
                }
                i++;
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                result.append(c);
                i++;
                continue;
            }

            if (c == '{') {
                int braceDepth = 1;
                int blockStart = i + 1;
                int j = blockStart;
                boolean bInString = false;
                char bStringChar = 0;
                while (j < css.length() && braceDepth > 0) {
                    char bc = css.charAt(j);
                    if (bInString) {
                        if (bc == bStringChar && !isEscaped(css, j)) bInString = false;
                    } else if (bc == '"' || bc == '\'') {
                        bInString = true;
                        bStringChar = bc;
                    } else if (bc == '{') {
                        braceDepth++;
                    } else if (bc == '}') {
                        braceDepth--;
                    }
                    j++;
                }
                int blockEnd = j - 1;
                String block = css.substring(blockStart, blockEnd);

                if (block.contains("{")) {
                    block = removeDuplicateProperties(block);
                } else {
                    block = deduplicateBlock(block);
                }

                result.append('{');
                result.append(block);
                result.append('}');
                i = blockEnd + 1;
                continue;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    private static boolean hasVendorPrefix(String value) {
        return value.contains("-webkit-") || value.contains("-moz-")
            || value.contains("-ms-") || value.contains("-o-");
    }

    private static boolean hasModernCssFunction(String value) {
        return value.contains("calc(") || value.contains("var(")
            || value.contains("min(") || value.contains("max(")
            || value.contains("clamp(") || value.contains("env(");
    }

    private static String deduplicateBlock(String block) {
        if (block.isEmpty()) return block;

        java.util.List<String> declarations = splitDeclarations(block);

        // Group declaration indices by property name
        LinkedHashMap<String, java.util.List<Integer>> propIndices = new LinkedHashMap<>();
        for (int i = 0; i < declarations.size(); i++) {
            String decl = declarations.get(i);
            int colon = decl.indexOf(':');
            if (colon > 0) {
                String prop = decl.substring(0, colon);
                propIndices.computeIfAbsent(prop, k -> new java.util.ArrayList<>()).add(i);
            }
        }

        // For properties with duplicates: if any value has a vendor prefix,
        // keep all (it's a browser fallback chain). Otherwise keep only last.
        java.util.Set<Integer> toRemove = new java.util.HashSet<>();
        for (var entry : propIndices.entrySet()) {
            java.util.List<Integer> indices = entry.getValue();
            if (indices.size() <= 1) continue;

            String propName = entry.getKey();

            boolean isFallbackChain = false;
            for (int idx : indices) {
                String value = declarations.get(idx).substring(declarations.get(idx).indexOf(':') + 1);
                if (hasVendorPrefix(value) || hasModernCssFunction(value)) {
                    isFallbackChain = true;
                    break;
                }
            }

            // Fix 5: Never deduplicate 'src' — multiple src declarations are standard in @font-face
            if (propName.equals("src")) {
                isFallbackChain = true;
            }

            // Fix 6: Check if property name itself is vendor-prefixed
            if (!isFallbackChain) {
                if (propName.startsWith("-webkit-") || propName.startsWith("-moz-")
                        || propName.startsWith("-ms-") || propName.startsWith("-o-")) {
                    isFallbackChain = true;
                }
            }

            // Fix 6: Check if a vendor-prefixed counterpart exists in the block
            if (!isFallbackChain) {
                if (propIndices.containsKey("-webkit-" + propName)
                        || propIndices.containsKey("-moz-" + propName)
                        || propIndices.containsKey("-ms-" + propName)
                        || propIndices.containsKey("-o-" + propName)) {
                    isFallbackChain = true;
                }
            }

            if (!isFallbackChain) {
                // Safe to dedup: remove all but the last
                for (int i = 0; i < indices.size() - 1; i++) {
                    toRemove.add(indices.get(i));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < declarations.size(); i++) {
            if (declarations.get(i).isEmpty() || toRemove.contains(i)) continue;
            if (sb.length() > 0) sb.append(';');
            sb.append(declarations.get(i));
        }
        return sb.toString();
    }

    static String mergeAdjacentRules(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;
        int i = 0;

        String prevSelector = null;
        int prevBodyStart = -1; // index in result where previous rule's body starts (after '{')

        while (i < css.length()) {
            char c = css.charAt(i);

            if (inString) {
                result.append(c);
                if (c == stringChar && !isEscaped(css, i)) {
                    inString = false;
                }
                i++;
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                result.append(c);
                i++;
                continue;
            }

            if (c == '{') {
                // Find matching close brace
                int braceDepth = 1;
                int blockStart = i + 1;
                int j = blockStart;
                boolean bInString = false;
                char bStringChar = 0;
                while (j < css.length() && braceDepth > 0) {
                    char bc = css.charAt(j);
                    if (bInString) {
                        if (bc == bStringChar && !isEscaped(css, j)) bInString = false;
                    } else if (bc == '"' || bc == '\'') {
                        bInString = true;
                        bStringChar = bc;
                    } else if (bc == '{') {
                        braceDepth++;
                    } else if (bc == '}') {
                        braceDepth--;
                    }
                    j++;
                }
                int blockEnd = j - 1;
                String body = css.substring(blockStart, blockEnd);

                // Extract selector: everything from the end of previous rule to this '{'
                // The selector is what's currently buffered since the last '}' or start
                String selector = extractTrailingSelector(result);

                if (selector != null && selector.equals(prevSelector) && prevBodyStart >= 0
                        && !body.contains("{")) {
                    // Merge: remove the '}' that closed previous rule and the selector chars
                    // result currently = "...prevBody}selector"
                    // We want to cut back to "...prevBody", then append ";newBody}"
                    result.setLength(result.length() - selector.length() - 1);
                    result.append(';').append(body).append('}');
                    // prevBodyStart stays the same, prevSelector stays the same
                } else {
                    // Normal rule — write it
                    result.append('{');
                    prevBodyStart = result.length();
                    prevSelector = selector;
                    result.append(body);
                    result.append('}');
                }

                i = blockEnd + 1;
                continue;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    private static String extractTrailingSelector(StringBuilder sb) {
        // Walk backward from end of sb to find the selector
        // Selector ends at the current position and starts after the last '}' or at index 0
        int end = sb.length();
        int start = end;
        for (int k = end - 1; k >= 0; k--) {
            if (sb.charAt(k) == '}') {
                start = k + 1;
                break;
            }
            if (k == 0) {
                start = 0;
            }
        }
        if (start >= end) return null;
        String sel = sb.substring(start, end).trim();
        return sel.isEmpty() ? null : sel;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: css-minifier <input.css> [output.css]");
            System.err.println("       cat input.css | css-minifier -");
            System.exit(1);
        }

        String css;
        if ("-".equals(args[0])) {
            css = new String(System.in.readAllBytes());
        } else {
            css = Files.readString(Path.of(args[0]));
        }

        String minified = minify(css);

        if (args.length >= 2) {
            Files.writeString(Path.of(args[1]), minified);
            long originalSize = css.length();
            long minifiedSize = minified.length();
            double savings = (1.0 - (double) minifiedSize / originalSize) * 100;
            System.out.printf("Minified: %d -> %d bytes (%.1f%% smaller)%n", originalSize, minifiedSize, savings);
        } else {
            System.out.print(minified);
        }
    }
}
