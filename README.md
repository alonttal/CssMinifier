# css-minifier

A single-file CSS minifier in Java. No dependencies, no configuration — just correct, competitive minification.

**814 lines of code. 399 tests. Beats official minifiers on 6 of 8 major frameworks.**

## Benchmark

Tested against officially distributed minified versions (cssnano, clean-css, etc.):

| Framework | Original | Minified | Official | vs Official |
|---|---:|---:|---:|---:|
| Animate.css | 95 KB | 72 KB | 72 KB | +0.32% |
| Bootstrap | 281 KB | 232 KB | 233 KB | **-0.26%** |
| Bulma | 764 KB | 679 KB | 678 KB | +0.22% |
| Foundation | 161 KB | 131 KB | 131 KB | **-0.26%** |
| Materialize | 179 KB | 140 KB | 142 KB | **-1.58%** |
| PrimeFlex | 433 KB | 350 KB | 361 KB | **-3.09%** |
| Semantic UI | 752 KB | 563 KB | 564 KB | **-0.17%** |
| Tailwind | 3,642 KB | 2,910 KB | 2,934 KB | **-0.81%** |

Negative = our output is smaller than official.

## Usage

### Command line

```bash
# Build
mvn package -q -DskipTests

# Minify a file
java -jar target/css-minifier-1.0-SNAPSHOT.jar input.css output.css

# Pipe from stdin
cat input.css | java -jar target/css-minifier-1.0-SNAPSHOT.jar -

# Programmatic
java -cp target/classes cssminifier.CssMinifier input.css output.css
```

### As a library

```java
import cssminifier.CssMinifier;

String minified = CssMinifier.minify(css);
```

The entire API is one static method: `CssMinifier.minify(String) -> String`.

## What it does

### Minification pipeline

```
stripComments → collapseWhitespace → optimizeValues → optimizeQuotedTokens
  → collapseShorthand → removeDuplicateProperties → mergeAdjacentRules
```

### Optimizations

| Category | Examples |
|---|---|
| Comment removal | `/* comment */` removed, `/*! license */` preserved |
| Whitespace collapse | Strips around `{}:;,>+~`, preserves in `calc()` |
| Hex color shortening | `#ff0000` → `#f00`, `#aabbccdd` → `#abcd` |
| Zero unit removal | `0px` → `0`, `0em` → `0` (preserves in custom properties) |
| Leading zero removal | `0.5em` → `.5em` |
| Font-weight keywords | `bold` → `700`, `normal` → `400` |
| Keyframe selectors | `from` → `0%`, `100%` → `to` |
| Transform simplification | `translate3d(0,0,0)` → `translateZ(0)`, `rotate3d(0,0,1,X)` → `rotate(X)` |
| Background/outline | `background:transparent` → `background:0 0`, `outline:none` → `outline:0` |
| Attribute selector quotes | `[type="text"]` → `[type=text]` |
| URL quotes | `url("file.png")` → `url(file.png)` |
| Calc whitespace | `calc(100% * 2)` → `calc(100%*2)` |
| Margin/padding shorthand | Four longhands → shorthand |
| Duplicate properties | Removes duplicates, preserves vendor fallback chains |
| Adjacent rule merging | `a{x:1} a{y:2}` → `a{x:1;y:2}` |
| Trailing semicolons | `{color:red;}` → `{color:red}` |

### Safety features

- Preserves `/*! license */` comments
- Preserves vendor-prefix fallback chains (`-webkit-`, `-moz-`, `-ms-`)
- Preserves modern CSS function fallbacks (`calc()`, `var()`, `min()`, `max()`, `clamp()`, `env()`)
- Preserves spaces around `+` and `-` in `calc()`
- Preserves pseudo-class descendant spaces (`.parent :hover` vs `.parent:hover`)
- Preserves units in custom property declarations (`--gap: 0%` stays `--gap:0%`)

## Tests

```bash
mvn test
```

399 tests covering comments, whitespace, selectors, at-rules, property values, strings, hex colors, zero units, font-weight, shorthand collapse, leading zeros, license comments, pseudo-class spacing, calc spacing, duplicate removal, vendor fallbacks, rule merging, keyframes, transforms, background/outline, attribute selectors, URL quotes, calc whitespace, custom properties, and real-world CSS patterns.
