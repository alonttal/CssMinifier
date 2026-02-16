package cssminifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssMinifier {

    public static String minify(String css) {
        return collapseShorthand(optimizeValues(collapseWhitespace(stripComments(css))));
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
                if (c == stringChar && (i == 0 || css.charAt(i - 1) != '\\')) {
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

    private static boolean isStripChar(char c) {
        return c == '{' || c == '}' || c == ';' || c == ':' || c == ','
            || c == '>' || c == '+' || c == '~';
    }

    private static String collapseWhitespace(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);

            if (inString) {
                result.append(c);
                if (c == stringChar && (i == 0 || css.charAt(i - 1) != '\\')) {
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

            if (Character.isWhitespace(c)) {
                char prev = result.length() > 0 ? result.charAt(result.length() - 1) : 0;
                if (isStripChar(prev)) {
                    continue;
                }
                int j = i + 1;
                while (j < css.length() && Character.isWhitespace(css.charAt(j))) j++;
                char nextNonWs = j < css.length() ? css.charAt(j) : 0;
                if (isStripChar(nextNonWs)) {
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
        "(?<=[:\\s,(/])0(px|em|rem|pt|cm|mm|in|pc|ex|ch|vw|vh|vmin|vmax|deg|rad|turn|ms|s|%)(?![0-9a-zA-Z%])");

    private static final Pattern FONT_WEIGHT = Pattern.compile(
        "font-weight:(normal|bold)(?=[;}\"])");

    static String optimizeValues(String css) {
        StringBuilder result = new StringBuilder(css.length());
        boolean inString = false;
        char stringChar = 0;
        int segmentStart = 0;

        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);

            if (inString) {
                if (c == stringChar && css.charAt(i - 1) != '\\') {
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

        // 3. Remove units on zero values (skip 0% in keyframe selectors like "0%{")
        Matcher mz = ZERO_UNIT.matcher(segment);
        sb = new StringBuilder();
        while (mz.find()) {
            int afterMatch = mz.end();
            // Don't strip 0% when it's a keyframe selector (followed by '{')
            if (mz.group(1).equals("%") && afterMatch < segment.length() && segment.charAt(afterMatch) == '{') {
                mz.appendReplacement(sb, Matcher.quoteReplacement(mz.group()));
            } else {
                mz.appendReplacement(sb, "0");
            }
        }
        mz.appendTail(sb);
        segment = sb.toString();

        // 4. Shorten font-weight keywords
        Matcher mfw = FONT_WEIGHT.matcher(segment);
        sb = new StringBuilder();
        while (mfw.find()) {
            String val = mfw.group(1).equals("bold") ? "font-weight:700" : "font-weight:400";
            mfw.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        mfw.appendTail(sb);
        segment = sb.toString();

        return segment;
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
                if (c == stringChar && (i == 0 || css.charAt(i - 1) != '\\')) {
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
                        if (bc == blockStringChar && css.charAt(j - 1) != '\\') {
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
        Map<String, String> sideValues = new LinkedHashMap<>();
        for (String side : SIDES) {
            String prop = property + "-" + side;
            // Match property-side:value (value runs to ; or end of block)
            Pattern p = Pattern.compile("(?:^|;)" + Pattern.quote(prop) + ":([^;]+)");
            Matcher m = p.matcher(block);
            if (m.find()) {
                sideValues.put(side, m.group(1).trim());
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

        // Remove the 4 longhand declarations
        for (String side : SIDES) {
            String prop = property + "-" + side;
            block = block.replaceFirst("(?:;|^)" + Pattern.quote(prop) + ":[^;]+", "");
        }
        // Clean up leading semicolons
        if (block.startsWith(";")) {
            block = block.substring(1);
        }

        // Append shorthand
        if (!block.isEmpty()) {
            block = block + ";" + property + ":" + shorthand;
        } else {
            block = property + ":" + shorthand;
        }

        return block;
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
