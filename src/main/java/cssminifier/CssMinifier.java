package cssminifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CssMinifier {

    public static String minify(String css) {
        return collapseWhitespace(stripComments(css));
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
