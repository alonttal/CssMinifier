# CLAUDE.md

## Project overview

Single-file CSS minifier in Java. One class (`CssMinifier.java`), one test class (`CssMinifierTest.java`), zero dependencies.

## Build & test

```bash
mvn test              # Run all 416 tests
mvn package -q -DskipTests  # Build JAR
```

## Architecture

All code is in `src/main/java/cssminifier/CssMinifier.java` (~883 lines).

### Minification pipeline (in order)

1. `stripComments()` — Remove `/* */`, preserve `/*! */`
2. `collapseWhitespace()` — Character-by-character walk tracking brace/paren depth and strings
3. `optimizeValues()` — Splits CSS at string boundaries, calls `optimizeSegment()` on non-string parts
4. `optimizeQuotedTokens()` — Separate pass for URL and attribute selector quote removal (these span string boundaries that `optimizeValues` can't see)
5. `collapseShorthand()` — margin/padding longhand → shorthand
6. `removeDuplicateProperties()` — Dedup within blocks, preserving vendor fallback chains
7. `mergeAdjacentRules()` — Merge adjacent rules with same selector

### Key design decisions

- **String-awareness**: Steps 2–7 all track CSS string context (`"..."` / `'...'`) to avoid modifying string contents. `optimizeValues` is the string-splitting hub — `optimizeSegment` only sees non-string text.
- **`optimizeQuotedTokens` exists because**: Quotes inside `url("...")` and `[attr="..."]` are treated as string boundaries by `optimizeValues`, so `optimizeSegment` never sees the full pattern. This separate pass handles them with its own character walk.
- **Custom property safety**: `isInCustomProperty()` prevents zero-unit stripping in `--var: 0%` declarations (textual substitution means `0` ≠ `0%`).
- **Keyframe selector context**: `from` → `0%` and `100%` → `to` use `(?<=[{}])` lookbehind to avoid matching class names like `.from`.
- **Vendor fallback preservation**: `deduplicateBlock` checks for vendor prefixes (`-webkit-`, `-moz-`, etc.) and modern CSS functions (`calc()`, `var()`, etc.) in values before deduplicating. Also checks property names for vendor prefixes and their unprefixed counterparts. Never deduplicates `src` properties (`@font-face` pattern).
- **Escaped backslash handling**: `isEscaped()` helper counts consecutive backslashes to correctly detect escaped characters (e.g., `"test\\"` where `\\` is an escaped backslash).
- **String-aware splitting**: `splitDeclarations()` splits on `;` while respecting string boundaries, preventing incorrect parsing of `content: "a;b"`.

### Adding a new optimization to `optimizeSegment()`

1. Add a `private static final Pattern` with the regex
2. Add a `segment = PATTERN.matcher(segment).replaceAll(...)` line in `optimizeSegment()`
3. These patterns operate on non-string segments only (strings already excluded)
4. Add tests in a new `@Nested` class in `CssMinifierTest.java`

## Test structure

Tests are in `src/test/java/cssminifier/CssMinifierTest.java` (~3172 lines, 416 tests).

Organized as `@Nested` classes by category: `Comments`, `Whitespace`, `Selectors`, `AtRules`, `PropertyValues`, `HexColors`, `ZeroUnits`, `FontWeight`, `ShorthandCollapse`, `DuplicatePropertyRemoval`, `AdjacentRuleMerging`, `KeyframeFromTo`, `Transform3dSimplification`, `Rotate3dSimplification`, `BackgroundShorthand`, `OutlineShorthand`, `AttributeSelectorQuotes`, `UrlQuotes`, `CalcWhitespace`, `CustomPropertyZeroPreservation`, `EscapedBackslash`, `SemicolonsInStrings`, `CssIdentifierValidation`, `UrlSemicolons`, `FontFaceSrcDedup`, `VendorPropertyNamePairing`, `RealWorldCss`, `EdgeCases`, `SizeReduction`, etc.

## Benchmark test files

`test-files/` contains original and official minified CSS for 8 frameworks (animate, bootstrap, bulma, foundation, materialize, primeflex, semantic-ui, tailwind). Re-minify with:

```bash
for f in animate bootstrap bulma foundation materialize primeflex semantic-ui tailwind; do
  java -cp target/classes cssminifier.CssMinifier "test-files/${f}.css" "test-files/${f}.min.css"
done
```
