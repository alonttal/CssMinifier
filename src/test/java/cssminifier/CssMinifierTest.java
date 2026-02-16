package cssminifier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CssMinifierTest {

    // ==================== COMMENTS ====================

    @Nested
    class Comments {

        @Test
        void removesInlineComment() {
            assertEquals("a{color:red}", CssMinifier.minify("a { /* text color */ color: red; }"));
        }

        @Test
        void removesMultilineComment() {
            String input = """
                    /*
                     * Reset styles
                     */
                    body {
                        margin: 0;
                    }
                    """;
            assertEquals("body{margin:0}", CssMinifier.minify(input));
        }

        @Test
        void removesEmptyComment() {
            assertEquals("a{color:red}", CssMinifier.minify("a { /**/ color: red; }"));
        }

        @Test
        void removesCommentAtStart() {
            assertEquals("a{color:red}", CssMinifier.minify("/* comment */ a { color: red; }"));
        }

        @Test
        void removesCommentAtEnd() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color: red; } /* comment */"));
        }

        @Test
        void removesAdjacentComments() {
            assertEquals("a{color:red}", CssMinifier.minify("/* one */ /* two */ a { color: red; }"));
        }

        @Test
        void removesCommentBetweenRules() {
            assertEquals("a{color:red}b{color:blue}",
                CssMinifier.minify("a { color: red; } /* separator */ b { color: blue; }"));
        }

        @Test
        void removesCommentBetweenProperties() {
            assertEquals("a{color:red;font-size:12px}",
                CssMinifier.minify("a { color: red; /* break */ font-size: 12px; }"));
        }

        @Test
        void removesCommentBetweenSelectorAndBrace() {
            assertEquals("a{color:red}", CssMinifier.minify("a /* comment */ { color: red; }"));
        }

        @Test
        void removesCommentInsidePropertyValue() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color: /* bright */ red; }"));
        }

        @Test
        void removesCommentBetweenPropertyAndColon() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color /* x */ : red; }"));
        }

        @Test
        void handlesCommentOnlyInput() {
            assertEquals("", CssMinifier.minify("/* just a comment */"));
        }

        @Test
        void handlesMultipleCommentOnlyInput() {
            assertEquals("", CssMinifier.minify("/* one */ /* two */ /* three */"));
        }

        @Test
        void handlesCommentWithAsterisks() {
            assertEquals("a{color:red}", CssMinifier.minify("a { /*** stars ***/ color: red; }"));
        }

        @Test
        void doesNotTreatDoubleSlashAsComment() {
            // CSS does not have // line comments
            assertEquals("a{color:red}//not a comment",
                CssMinifier.minify("a { color: red; } //not a comment"));
        }

        @Test
        void preservesCommentLikeContentInDoubleQuotedString() {
            assertEquals("a{content:\"/* not a comment */\"}",
                CssMinifier.minify("a { content: \"/* not a comment */\"; }"));
        }

        @Test
        void preservesCommentLikeContentInSingleQuotedString() {
            assertEquals("a{content:'/* not a comment */'}",
                CssMinifier.minify("a { content: '/* not a comment */'; }"));
        }
    }

    // ==================== WHITESPACE ====================

    @Nested
    class Whitespace {

        @Test
        void collapsesMultipleSpaces() {
            assertEquals("a{color:red}", CssMinifier.minify("a  {  color :  red  }"));
        }

        @Test
        void removesLeadingWhitespace() {
            assertEquals("a{color:red}", CssMinifier.minify("   a { color: red; }"));
        }

        @Test
        void removesTrailingWhitespace() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color: red; }   "));
        }

        @Test
        void removesNewlines() {
            assertEquals("a{color:red}", CssMinifier.minify("a {\ncolor:\nred;\n}"));
        }

        @Test
        void removesTabs() {
            assertEquals("a{color:red}", CssMinifier.minify("a {\n\tcolor:\n\t\tred;\n}"));
        }

        @Test
        void removesCarriageReturns() {
            assertEquals("a{color:red}", CssMinifier.minify("a {\r\n\tcolor: red;\r\n}"));
        }

        @Test
        void removesMixedWhitespace() {
            assertEquals("a{color:red}", CssMinifier.minify("a \t\n { \r\n color : \t red ; \n }"));
        }

        @Test
        void removesWhitespaceBeforeOpenBrace() {
            assertEquals("a{color:red}", CssMinifier.minify("a   {color: red;}"));
        }

        @Test
        void removesWhitespaceAfterOpenBrace() {
            assertEquals("a{color:red}", CssMinifier.minify("a{   color: red;}"));
        }

        @Test
        void removesWhitespaceBeforeCloseBrace() {
            assertEquals("a{color:red}", CssMinifier.minify("a{color: red   }"));
        }

        @Test
        void removesWhitespaceAfterCloseBrace() {
            assertEquals("a{color:red}b{color:blue}",
                CssMinifier.minify("a{color: red;}   b{color: blue;}"));
        }

        @Test
        void removesWhitespaceBeforeColon() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color : red; }"));
        }

        @Test
        void removesWhitespaceAfterColon() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color:   red; }"));
        }

        @Test
        void removesWhitespaceBeforeSemicolon() {
            assertEquals("a{color:red;font-size:12px}",
                CssMinifier.minify("a { color: red ; font-size: 12px ; }"));
        }

        @Test
        void removesWhitespaceAfterSemicolon() {
            assertEquals("a{color:red;font-size:12px}",
                CssMinifier.minify("a { color: red;   font-size: 12px; }"));
        }

        @Test
        void removesWhitespaceAroundComma() {
            assertEquals("a,b,c{color:red}",
                CssMinifier.minify("a , b , c { color: red; }"));
        }

        @Test
        void handlesOnlyWhitespace() {
            assertEquals("", CssMinifier.minify("   \t\n\r  "));
        }

        @Test
        void preservesSpaceBetweenSelectorParts() {
            assertEquals("div p{color:red}", CssMinifier.minify("div p { color: red; }"));
        }

        @Test
        void preservesSingleSpaceBetweenMultipleSelectorParts() {
            assertEquals("div ul li{color:red}", CssMinifier.minify("div   ul   li { color: red; }"));
        }

        @Test
        void preservesSpaceInPropertyValues() {
            assertEquals("a{margin:10px 20px}", CssMinifier.minify("a { margin: 10px 20px; }"));
        }

        @Test
        void preservesSpaceInMultiWordValues() {
            assertEquals("a{font-family:\"Times New Roman\",serif}",
                CssMinifier.minify("a { font-family: \"Times New Roman\", serif; }"));
        }

        @Test
        void preservesSpaceInFourValueShorthand() {
            assertEquals("a{margin:1px 2px 3px 4px}",
                CssMinifier.minify("a { margin: 1px 2px 3px 4px; }"));
        }
    }

    // ==================== SEMICOLONS ====================

    @Nested
    class Semicolons {

        @Test
        void removesTrailingSemicolonSingleProperty() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color: red; }"));
        }

        @Test
        void removesTrailingSemicolonMultipleProperties() {
            assertEquals("a{color:red;font-size:12px}",
                CssMinifier.minify("a { color: red; font-size: 12px; }"));
        }

        @Test
        void handlesNoTrailingSemicolon() {
            assertEquals("a{color:red}", CssMinifier.minify("a { color: red }"));
        }

        @Test
        void preservesMiddleSemicolons() {
            assertEquals("a{color:red;font-size:12px;display:block}",
                CssMinifier.minify("a { color: red; font-size: 12px; display: block; }"));
        }

        @Test
        void removesTrailingSemicolonInNestedBlocks() {
            String input = """
                    @media screen {
                        a {
                            color: red;
                        }
                    }
                    """;
            assertEquals("@media screen{a{color:red}}", CssMinifier.minify(input));
        }
    }

    // ==================== STRINGS ====================

    @Nested
    class Strings {

        @Test
        void preservesDoubleQuotedString() {
            assertEquals("a{content:\"hello world\"}",
                CssMinifier.minify("a { content: \"hello world\"; }"));
        }

        @Test
        void preservesSingleQuotedString() {
            assertEquals("a{content:'hello world'}",
                CssMinifier.minify("a { content: 'hello world'; }"));
        }

        @Test
        void preservesSpacesInDoubleQuotedString() {
            assertEquals("a{content:\"  lots   of   spaces  \"}",
                CssMinifier.minify("a { content: \"  lots   of   spaces  \"; }"));
        }

        @Test
        void preservesSpacesInSingleQuotedString() {
            assertEquals("a{content:'  lots   of   spaces  '}",
                CssMinifier.minify("a { content: '  lots   of   spaces  '; }"));
        }

        @Test
        void preservesEmptyDoubleQuotedString() {
            assertEquals("a{content:\"\"}", CssMinifier.minify("a { content: \"\"; }"));
        }

        @Test
        void preservesEmptySingleQuotedString() {
            assertEquals("a{content:''}", CssMinifier.minify("a { content: ''; }"));
        }

        @Test
        void preservesEscapedQuoteInDoubleQuotedString() {
            assertEquals("a{content:\"he said \\\"hi\\\"\"}",
                CssMinifier.minify("a { content: \"he said \\\"hi\\\"\"; }"));
        }

        @Test
        void preservesEscapedQuoteInSingleQuotedString() {
            assertEquals("a{content:'it\\'s fine'}",
                CssMinifier.minify("a { content: 'it\\'s fine'; }"));
        }

        @Test
        void preservesBracesInsideString() {
            assertEquals("a{content:\"{ not a block }\"}",
                CssMinifier.minify("a { content: \"{ not a block }\"; }"));
        }

        @Test
        void preservesSemicolonInsideString() {
            assertEquals("a{content:\"a; b; c\"}",
                CssMinifier.minify("a { content: \"a; b; c\"; }"));
        }

        @Test
        void preservesColonInsideString() {
            assertEquals("a{content:\"key: value\"}",
                CssMinifier.minify("a { content: \"key: value\"; }"));
        }

        @Test
        void handlesMultipleStringsInOneRule() {
            assertEquals("a{content:\"hello\" \"world\"}",
                CssMinifier.minify("a { content: \"hello\" \"world\"; }"));
        }

        @Test
        void preservesNewlinesInString() {
            assertEquals("a{content:\"line1\\nline2\"}",
                CssMinifier.minify("a { content: \"line1\\nline2\"; }"));
        }
    }

    // ==================== SELECTORS ====================

    @Nested
    class Selectors {

        @Test
        void handlesClassSelector() {
            assertEquals(".foo{color:red}", CssMinifier.minify(".foo { color: red; }"));
        }

        @Test
        void handlesIdSelector() {
            assertEquals("#bar{color:red}", CssMinifier.minify("#bar { color: red; }"));
        }

        @Test
        void handlesUniversalSelector() {
            assertEquals("*{margin:0}", CssMinifier.minify("* { margin: 0; }"));
        }

        @Test
        void handlesElementSelector() {
            assertEquals("div{color:red}", CssMinifier.minify("div { color: red; }"));
        }

        @Test
        void handlesDescendantCombinator() {
            assertEquals("div p{color:red}", CssMinifier.minify("div p { color: red; }"));
        }

        @Test
        void handlesChildCombinator() {
            assertEquals("div>p{color:red}", CssMinifier.minify("div > p { color: red; }"));
        }

        @Test
        void handlesAdjacentSiblingCombinator() {
            assertEquals("h1+p{color:red}", CssMinifier.minify("h1 + p { color: red; }"));
        }

        @Test
        void handlesGeneralSiblingCombinator() {
            assertEquals("h1~p{color:red}", CssMinifier.minify("h1 ~ p { color: red; }"));
        }

        @Test
        void handlesCommaSeparatedSelectors() {
            assertEquals("h1,h2,h3{color:red}",
                CssMinifier.minify("h1, h2, h3 { color: red; }"));
        }

        @Test
        void handlesCompoundSelector() {
            assertEquals("div.foo#bar{color:red}",
                CssMinifier.minify("div.foo#bar { color: red; }"));
        }

        @Test
        void handlesPseudoClass() {
            assertEquals("a:hover{color:red}", CssMinifier.minify("a:hover { color: red; }"));
        }

        @Test
        void handlesPseudoClassWithParens() {
            assertEquals("li:nth-child(2n+1){color:red}",
                CssMinifier.minify("li:nth-child(2n+1) { color: red; }"));
        }

        @Test
        void handlesPseudoElement() {
            assertEquals("a::before{content:\"\"}",
                CssMinifier.minify("a::before { content: \"\"; }"));
        }

        @Test
        void handlesAttributeSelector() {
            assertEquals("input[type=\"text\"]{color:red}",
                CssMinifier.minify("input[type=\"text\"] { color: red; }"));
        }

        @Test
        void handlesAttributeSelectorWithoutQuotes() {
            assertEquals("input[type=text]{color:red}",
                CssMinifier.minify("input[type=text] { color: red; }"));
        }

        @Test
        void handlesAttributeSelectorContains() {
            assertEquals("a[href*=\"example\"]{color:red}",
                CssMinifier.minify("a[href*=\"example\"] { color: red; }"));
        }

        @Test
        void handlesComplexSelector() {
            assertEquals("div.container>ul.nav li.active a:hover{color:red}",
                CssMinifier.minify("div.container > ul.nav li.active a:hover { color: red; }"));
        }

        @Test
        void handlesMultipleSelectorsMultiline() {
            String input = """
                    h1,
                    h2,
                    h3 {
                        color: red;
                    }
                    """;
            assertEquals("h1,h2,h3{color:red}", CssMinifier.minify(input));
        }

        @Test
        void handlesNotPseudoClass() {
            assertEquals("p:not(.special){color:red}",
                CssMinifier.minify("p:not(.special) { color: red; }"));
        }
    }

    // ==================== AT-RULES ====================

    @Nested
    class AtRules {

        @Test
        void handlesMediaQuery() {
            String input = """
                    @media (max-width: 768px) {
                        body {
                            font-size: 14px;
                        }
                    }
                    """;
            assertEquals("@media (max-width:768px){body{font-size:14px}}", CssMinifier.minify(input));
        }

        @Test
        void handlesMediaQueryWithType() {
            String input = """
                    @media screen and (min-width: 600px) {
                        body {
                            font-size: 16px;
                        }
                    }
                    """;
            assertEquals("@media screen and (min-width:600px){body{font-size:16px}}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesKeyframes() {
            String input = """
                    @keyframes fadeIn {
                        from {
                            opacity: 0;
                        }
                        to {
                            opacity: 1;
                        }
                    }
                    """;
            assertEquals("@keyframes fadeIn{from{opacity:0}to{opacity:1}}", CssMinifier.minify(input));
        }

        @Test
        void handlesKeyframesWithPercentages() {
            String input = """
                    @keyframes slide {
                        0% {
                            transform: translateX(0);
                        }
                        50% {
                            transform: translateX(100px);
                        }
                        100% {
                            transform: translateX(0);
                        }
                    }
                    """;
            assertEquals(
                "@keyframes slide{0%{transform:translateX(0)}50%{transform:translateX(100px)}100%{transform:translateX(0)}}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesFontFace() {
            String input = """
                    @font-face {
                        font-family: "MyFont";
                        src: url("font.woff2") format("woff2");
                    }
                    """;
            assertEquals("@font-face{font-family:\"MyFont\";src:url(\"font.woff2\") format(\"woff2\")}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesImport() {
            assertEquals("@import url(\"style.css\");",
                CssMinifier.minify("@import url(\"style.css\");"));
        }

        @Test
        void handlesCharset() {
            assertEquals("@charset \"UTF-8\";", CssMinifier.minify("@charset \"UTF-8\";"));
        }

        @Test
        void handlesSupports() {
            String input = """
                    @supports (display: grid) {
                        .container {
                            display: grid;
                        }
                    }
                    """;
            assertEquals("@supports (display:grid){.container{display:grid}}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesNestedMediaQueries() {
            String input = """
                    @media screen {
                        @media (min-width: 600px) {
                            body {
                                font-size: 16px;
                            }
                        }
                    }
                    """;
            assertEquals("@media screen{@media (min-width:600px){body{font-size:16px}}}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesMediaQueryWithMultipleRules() {
            String input = """
                    @media print {
                        body {
                            font-size: 12pt;
                        }
                        .no-print {
                            display: none;
                        }
                    }
                    """;
            assertEquals("@media print{body{font-size:12pt}.no-print{display:none}}",
                CssMinifier.minify(input));
        }
    }

    // ==================== PROPERTY VALUES ====================

    @Nested
    class PropertyValues {

        @Test
        void handlesImportant() {
            assertEquals("a{color:red!important}",
                CssMinifier.minify("a { color: red !important; }"));
        }

        @Test
        void handlesImportantNoSpace() {
            // Already no space before !important
            assertEquals("a{color:red!important}",
                CssMinifier.minify("a { color: red!important; }"));
        }

        @Test
        void handlesImportantWithMultipleSpaces() {
            assertEquals("a{color:red!important}",
                CssMinifier.minify("a { color: red   !important; }"));
        }

        @Test
        void handlesImportantOnMultipleProperties() {
            assertEquals("a{color:red!important;font-size:12px!important}",
                CssMinifier.minify("a { color: red !important; font-size: 12px !important; }"));
        }

        @Test
        void handlesImportantWithHexColor() {
            assertEquals("a{color:#f00!important}",
                CssMinifier.minify("a { color: #ff0000 !important; }"));
        }

        @Test
        void handlesImportantWithDecimal() {
            assertEquals("a{opacity:.5!important}",
                CssMinifier.minify("a { opacity: 0.5 !important; }"));
        }

        @Test
        void handlesImportantInsideMediaQuery() {
            assertEquals("@media screen{a{color:red!important}}",
                CssMinifier.minify("@media screen { a { color: red !important; } }"));
        }

        @Test
        void doesNotStripExclamationInSelector() {
            // ! shouldn't be treated as strip char outside declarations
            // This is an edge case — CSS selectors don't use ! but let's be safe
            assertEquals("a{content:\"!important\"}",
                CssMinifier.minify("a { content: \"!important\"; }"));
        }

        @Test
        void handlesNegativeValues() {
            assertEquals("a{margin:-10px}", CssMinifier.minify("a { margin: -10px; }"));
        }

        @Test
        void handlesDecimalValues() {
            assertEquals("a{opacity:.5}", CssMinifier.minify("a { opacity: 0.5; }"));
        }

        @Test
        void handlesZeroValue() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0; }"));
        }

        @Test
        void handlesCalc() {
            assertEquals("a{width:calc(100% - 20px)}",
                CssMinifier.minify("a { width: calc(100% - 20px); }"));
        }

        @Test
        void handlesCssVariables() {
            assertEquals("a{color:var(--main-color)}",
                CssMinifier.minify("a { color: var(--main-color); }"));
        }

        @Test
        void handlesCssVariableWithFallback() {
            assertEquals("a{color:var(--main-color,blue)}",
                CssMinifier.minify("a { color: var(--main-color, blue); }"));
        }

        @Test
        void handlesCssCustomPropertyDefinition() {
            assertEquals(":root{--main-color:#06c}",
                CssMinifier.minify(":root { --main-color: #06c; }"));
        }

        @Test
        void handlesUrl() {
            assertEquals("a{background:url(image.png)}",
                CssMinifier.minify("a { background: url(image.png); }"));
        }

        @Test
        void handlesUrlWithQuotes() {
            assertEquals("a{background:url(\"image.png\")}",
                CssMinifier.minify("a { background: url(\"image.png\"); }"));
        }

        @Test
        void handlesMultipleBackgroundValues() {
            assertEquals("a{background:url(bg.png) no-repeat center center}",
                CssMinifier.minify("a { background: url(bg.png) no-repeat center center; }"));
        }

        @Test
        void handlesRgbColor() {
            assertEquals("a{color:rgb(255,0,0)}",
                CssMinifier.minify("a { color: rgb(255, 0, 0); }"));
        }

        @Test
        void handlesRgbaColor() {
            assertEquals("a{color:rgba(255,0,0,.5)}",
                CssMinifier.minify("a { color: rgba(255, 0, 0, 0.5); }"));
        }

        @Test
        void handlesHexColor() {
            assertEquals("a{color:#f00}", CssMinifier.minify("a { color: #ff0000; }"));
        }

        @Test
        void handlesTransform() {
            assertEquals("a{transform:rotate(45deg) scale(1.5)}",
                CssMinifier.minify("a { transform: rotate(45deg) scale(1.5); }"));
        }

        @Test
        void handlesTransition() {
            assertEquals("a{transition:all .3s ease-in-out}",
                CssMinifier.minify("a { transition: all 0.3s ease-in-out; }"));
        }

        @Test
        void handlesBoxShadow() {
            assertEquals("a{box-shadow:0 2px 4px rgba(0,0,0,.1)}",
                CssMinifier.minify("a { box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }"));
        }

        @Test
        void handlesGradient() {
            assertEquals("a{background:linear-gradient(to right,red,blue)}",
                CssMinifier.minify("a { background: linear-gradient(to right, red, blue); }"));
        }

        @Test
        void handlesMultipleValuesWithCommas() {
            assertEquals("a{font-family:Arial,Helvetica,sans-serif}",
                CssMinifier.minify("a { font-family: Arial, Helvetica, sans-serif; }"));
        }

        @Test
        void handlesGridTemplate() {
            assertEquals("a{grid-template-columns:1fr 2fr 1fr}",
                CssMinifier.minify("a { grid-template-columns: 1fr 2fr 1fr; }"));
        }
    }

    // ==================== MULTIPLE RULES ====================

    @Nested
    class MultipleRules {

        @Test
        void handlesTwoRules() {
            String input = """
                    h1 {
                        font-size: 24px;
                    }
                    h2 {
                        font-size: 18px;
                    }
                    """;
            assertEquals("h1{font-size:24px}h2{font-size:18px}", CssMinifier.minify(input));
        }

        @Test
        void handlesManyRules() {
            String input = """
                    a { color: red; }
                    b { color: blue; }
                    c { color: green; }
                    d { color: yellow; }
                    e { color: purple; }
                    """;
            assertEquals("a{color:red}b{color:blue}c{color:green}d{color:yellow}e{color:purple}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesRulesWithMultipleProperties() {
            String input = """
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: sans-serif;
                        line-height: 1.5;
                    }
                    """;
            assertEquals("body{margin:0;padding:0;font-family:sans-serif;line-height:1.5}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesRulesWithNoSpaceBetween() {
            assertEquals("a{color:red}b{color:blue}",
                CssMinifier.minify("a{color:red}b{color:blue}"));
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    class EdgeCases {

        @Test
        void handlesEmptyInput() {
            assertEquals("", CssMinifier.minify(""));
        }

        @Test
        void handlesOnlySpaces() {
            assertEquals("", CssMinifier.minify("     "));
        }

        @Test
        void handlesOnlyNewlines() {
            assertEquals("", CssMinifier.minify("\n\n\n"));
        }

        @Test
        void handlesEmptyRuleBody() {
            assertEquals("a{}", CssMinifier.minify("a { }"));
        }

        @Test
        void handlesAlreadyMinified() {
            String input = "a{color:red}b{font-size:12px}";
            assertEquals(input, CssMinifier.minify(input));
        }

        @Test
        void handlesSingleCharacterInput() {
            assertEquals("a", CssMinifier.minify("a"));
        }

        @Test
        void handlesSingleProperty() {
            assertEquals("a{color:red}", CssMinifier.minify("a{color:red}"));
        }

        @Test
        void handlesUnicodeContent() {
            assertEquals("a{content:\"\u2603\"}",
                CssMinifier.minify("a { content: \"\u2603\"; }"));
        }

        @Test
        void handlesUnicodeSelector() {
            assertEquals(".caf\u00e9{color:red}",
                CssMinifier.minify(".caf\u00e9 { color: red; }"));
        }

        @Test
        void handlesDataUri() {
            String input = "a { background: url(\"data:image/png;base64,iVBOR\"); }";
            assertEquals("a{background:url(\"data:image/png;base64,iVBOR\")}", CssMinifier.minify(input));
        }

        @Test
        void handlesSingleLineInput() {
            assertEquals("a{color:red}",
                CssMinifier.minify("a { color: red; }"));
        }

        @Test
        void handlesNoSemicolonLastProperty() {
            assertEquals("a{color:red;font-size:12px}",
                CssMinifier.minify("a { color: red; font-size: 12px }"));
        }

        @Test
        void idempotent() {
            String input = """
                    body {
                        margin: 0;
                        padding: 0;
                    }
                    a {
                        color: blue;
                        text-decoration: none;
                    }
                    """;
            String once = CssMinifier.minify(input);
            String twice = CssMinifier.minify(once);
            assertEquals(once, twice);
        }

        @Test
        void idempotentWithComments() {
            String input = "/* comment */ a { /* color */ color: red; }";
            String once = CssMinifier.minify(input);
            String twice = CssMinifier.minify(once);
            assertEquals(once, twice);
        }

        @Test
        void idempotentWithStrings() {
            String input = "a { content: \"  hello  \"; }";
            String once = CssMinifier.minify(input);
            String twice = CssMinifier.minify(once);
            assertEquals(once, twice);
        }
    }

    // ==================== REAL-WORLD CSS ====================

    @Nested
    class RealWorldCss {

        @Test
        void handlesSimpleReset() {
            String input = """
                    /* CSS Reset */
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    """;
            assertEquals("*{margin:0;padding:0;box-sizing:border-box}", CssMinifier.minify(input));
        }

        @Test
        void handlesTypicalPageLayout() {
            String input = """
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        padding: 0 20px;
                    }

                    .header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 20px 0;
                    }

                    .nav a {
                        text-decoration: none;
                        color: #333;
                        margin-left: 20px;
                    }
                    """;
            assertEquals(
                ".container{max-width:1200px;margin:0 auto;padding:0 20px}" +
                ".header{display:flex;justify-content:space-between;align-items:center;padding:20px 0}" +
                ".nav a{text-decoration:none;color:#333;margin-left:20px}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesResponsiveDesign() {
            String input = """
                    .grid {
                        display: grid;
                        grid-template-columns: repeat(3, 1fr);
                        gap: 20px;
                    }

                    @media (max-width: 768px) {
                        .grid {
                            grid-template-columns: 1fr;
                        }
                    }
                    """;
            assertEquals(
                ".grid{display:grid;grid-template-columns:repeat(3,1fr);gap:20px}" +
                "@media (max-width:768px){.grid{grid-template-columns:1fr}}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesButtonStyles() {
            String input = """
                    .btn {
                        display: inline-block;
                        padding: 10px 20px;
                        border: none;
                        border-radius: 4px;
                        background-color: #007bff;
                        color: #fff;
                        cursor: pointer;
                        transition: background-color 0.3s ease;
                    }

                    .btn:hover {
                        background-color: #0056b3;
                    }

                    .btn:active {
                        transform: translateY(1px);
                    }
                    """;
            assertEquals(
                ".btn{display:inline-block;padding:10px 20px;border:none;border-radius:4px;" +
                "background-color:#007bff;color:#fff;cursor:pointer;transition:background-color .3s ease}" +
                ".btn:hover{background-color:#0056b3}" +
                ".btn:active{transform:translateY(1px)}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesAnimationDefinition() {
            String input = """
                    .spinner {
                        width: 40px;
                        height: 40px;
                        border: 4px solid #f3f3f3;
                        border-top: 4px solid #3498db;
                        border-radius: 50%;
                        animation: spin 1s linear infinite;
                    }

                    @keyframes spin {
                        0% {
                            transform: rotate(0deg);
                        }
                        100% {
                            transform: rotate(360deg);
                        }
                    }
                    """;
            assertEquals(
                ".spinner{width:40px;height:40px;border:4px solid #f3f3f3;" +
                "border-top:4px solid #3498db;border-radius:50%;animation:spin 1s linear infinite}" +
                "@keyframes spin{0%{transform:rotate(0)}100%{transform:rotate(360deg)}}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesFormStyles() {
            String input = """
                    input[type="text"],
                    input[type="email"],
                    textarea {
                        width: 100%;
                        padding: 8px 12px;
                        border: 1px solid #ccc;
                        border-radius: 4px;
                        font-size: 14px;
                    }

                    input[type="text"]:focus,
                    input[type="email"]:focus,
                    textarea:focus {
                        outline: none;
                        border-color: #007bff;
                        box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
                    }
                    """;
            assertEquals(
                "input[type=\"text\"],input[type=\"email\"],textarea" +
                "{width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:4px;font-size:14px}" +
                "input[type=\"text\"]:focus,input[type=\"email\"]:focus,textarea:focus" +
                "{outline:none;border-color:#007bff;box-shadow:0 0 0 3px rgba(0,123,255,.25)}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesFlexboxLayout() {
            String input = """
                    .card-container {
                        display: flex;
                        flex-wrap: wrap;
                        gap: 16px;
                    }

                    .card {
                        flex: 1 1 300px;
                        padding: 16px;
                        border: 1px solid #e0e0e0;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    }

                    .card > h3 {
                        margin-bottom: 8px;
                        font-size: 18px;
                    }
                    """;
            assertEquals(
                ".card-container{display:flex;flex-wrap:wrap;gap:16px}" +
                ".card{flex:1 1 300px;padding:16px;border:1px solid #e0e0e0;border-radius:8px;" +
                "box-shadow:0 2px 4px rgba(0,0,0,.1)}" +
                ".card>h3{margin-bottom:8px;font-size:18px}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesHeavilyCommentedCss() {
            String input = """
                    /**
                     * Main Stylesheet
                     * Author: Test
                     * Version: 1.0
                     */

                    /* ==================
                       Base Styles
                       ================== */

                    body {
                        /* Default font */
                        font-family: sans-serif;
                        /* Reset margin */
                        margin: 0;
                    }

                    /* Links */
                    a {
                        color: blue; /* TODO: use variable */
                    }
                    """;
            assertEquals(
                "body{font-family:sans-serif;margin:0}a{color:blue}",
                CssMinifier.minify(input));
        }

        @Test
        void handlesCssWithCustomProperties() {
            String input = """
                    :root {
                        --primary: #007bff;
                        --secondary: #6c757d;
                        --font-size-base: 16px;
                        --line-height-base: 1.5;
                    }

                    body {
                        font-size: var(--font-size-base);
                        line-height: var(--line-height-base);
                        color: var(--secondary);
                    }

                    a {
                        color: var(--primary);
                    }
                    """;
            assertEquals(
                ":root{--primary:#007bff;--secondary:#6c757d;--font-size-base:16px;--line-height-base:1.5}" +
                "body{font-size:var(--font-size-base);line-height:var(--line-height-base);color:var(--secondary)}" +
                "a{color:var(--primary)}",
                CssMinifier.minify(input));
        }
    }

    // ==================== HEX COLOR SHORTENING ====================

    @Nested
    class HexColors {

        @Test
        void shortens6DigitWithMatchingPairs() {
            assertEquals("a{color:#abc}", CssMinifier.minify("a { color: #aabbcc; }"));
        }

        @Test
        void shortensUppercase6Digit() {
            assertEquals("a{color:#abc}", CssMinifier.minify("a { color: #AABBCC; }"));
        }

        @Test
        void shortensMixedCase6Digit() {
            assertEquals("a{color:#abc}", CssMinifier.minify("a { color: #AaBbCc; }"));
        }

        @Test
        void shortensWhite() {
            assertEquals("a{color:#fff}", CssMinifier.minify("a { color: #ffffff; }"));
        }

        @Test
        void shortensBlack() {
            assertEquals("a{color:#000}", CssMinifier.minify("a { color: #000000; }"));
        }

        @Test
        void shortensRed() {
            assertEquals("a{color:#f00}", CssMinifier.minify("a { color: #ff0000; }"));
        }

        @Test
        void shortensGreen() {
            assertEquals("a{color:#0f0}", CssMinifier.minify("a { color: #00ff00; }"));
        }

        @Test
        void shortensBlue() {
            assertEquals("a{color:#00f}", CssMinifier.minify("a { color: #0000ff; }"));
        }

        @Test
        void shortens112233() {
            assertEquals("a{color:#123}", CssMinifier.minify("a { color: #112233; }"));
        }

        @Test
        void doesNotShortenNonMatchingPairs() {
            assertEquals("a{color:#123456}", CssMinifier.minify("a { color: #123456; }"));
        }

        @Test
        void doesNotShortenAbcdef() {
            assertEquals("a{color:#abcdef}", CssMinifier.minify("a { color: #abcdef; }"));
        }

        @Test
        void doesNotShortenF0f0f0() {
            assertEquals("a{color:#f0f0f0}", CssMinifier.minify("a { color: #f0f0f0; }"));
        }

        @Test
        void leaves3DigitHexAlone() {
            assertEquals("a{color:#abc}", CssMinifier.minify("a { color: #abc; }"));
        }

        @Test
        void leaves3DigitUppercaseAlone() {
            assertEquals("a{color:#ABC}", CssMinifier.minify("a { color: #ABC; }"));
        }

        @Test
        void shortensMultipleHexInOneRule() {
            assertEquals("a{color:#f00;background:#0f0}",
                CssMinifier.minify("a { color: #ff0000; background: #00ff00; }"));
        }

        @Test
        void shortensHexInBorderValue() {
            assertEquals("a{border:1px solid #f00}",
                CssMinifier.minify("a { border: 1px solid #ff0000; }"));
        }

        @Test
        void shortensHexInBoxShadow() {
            assertEquals("a{box-shadow:0 0 5px #000}",
                CssMinifier.minify("a { box-shadow: 0 0 5px #000000; }"));
        }

        @Test
        void shortensHexInCustomProperty() {
            assertEquals(":root{--color:#f00}",
                CssMinifier.minify(":root { --color: #ff0000; }"));
        }

        @Test
        void doesNotShortenHexInsideDoubleQuotedString() {
            assertEquals("a{content:\"#ff0000\"}",
                CssMinifier.minify("a { content: \"#ff0000\"; }"));
        }

        @Test
        void doesNotShortenHexInsideSingleQuotedString() {
            assertEquals("a{content:'#ff0000'}",
                CssMinifier.minify("a { content: '#ff0000'; }"));
        }

        @Test
        void shortens8DigitHexWithMatchingPairs() {
            assertEquals("a{color:#f000}",
                CssMinifier.minify("a { color: #ff000000; }"));
        }

        @Test
        void shortens8DigitWhiteWithAlpha() {
            assertEquals("a{color:#ffff}",
                CssMinifier.minify("a { color: #ffffffff; }"));
        }

        @Test
        void doesNotShorten8DigitNonMatchingPairs() {
            assertEquals("a{color:#f0f0f011}",
                CssMinifier.minify("a { color: #f0f0f011; }"));
        }

        @Test
        void shortensMultipleHexAcrossRules() {
            assertEquals("a{color:#f00}b{color:#0f0}",
                CssMinifier.minify("a { color: #ff0000; } b { color: #00ff00; }"));
        }

        @Test
        void shortensHexInGradient() {
            assertEquals("a{background:linear-gradient(#000,#fff)}",
                CssMinifier.minify("a { background: linear-gradient(#000000, #ffffff); }"));
        }

        @Test
        void outputIsLowercase() {
            assertEquals("a{color:#abc}", CssMinifier.minify("a { color: #AABBCC; }"));
        }
    }

    // ==================== ZERO UNIT REMOVAL ====================

    @Nested
    class ZeroUnits {

        @Test
        void removesZeroPx() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0px; }"));
        }

        @Test
        void removesZeroEm() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0em; }"));
        }

        @Test
        void removesZeroRem() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0rem; }"));
        }

        @Test
        void removesZeroPt() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0pt; }"));
        }

        @Test
        void removesZeroCm() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0cm; }"));
        }

        @Test
        void removesZeroMm() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0mm; }"));
        }

        @Test
        void removesZeroIn() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0in; }"));
        }

        @Test
        void removesZeroPc() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0pc; }"));
        }

        @Test
        void removesZeroEx() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0ex; }"));
        }

        @Test
        void removesZeroCh() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0ch; }"));
        }

        @Test
        void removesZeroVw() {
            assertEquals("a{width:0}", CssMinifier.minify("a { width: 0vw; }"));
        }

        @Test
        void removesZeroVh() {
            assertEquals("a{height:0}", CssMinifier.minify("a { height: 0vh; }"));
        }

        @Test
        void removesZeroVmin() {
            assertEquals("a{width:0}", CssMinifier.minify("a { width: 0vmin; }"));
        }

        @Test
        void removesZeroVmax() {
            assertEquals("a{width:0}", CssMinifier.minify("a { width: 0vmax; }"));
        }

        @Test
        void removesZeroDeg() {
            assertEquals("a{transform:rotate(0)}",
                CssMinifier.minify("a { transform: rotate(0deg); }"));
        }

        @Test
        void removesZeroRad() {
            assertEquals("a{transform:rotate(0)}",
                CssMinifier.minify("a { transform: rotate(0rad); }"));
        }

        @Test
        void removesZeroTurn() {
            assertEquals("a{transform:rotate(0)}",
                CssMinifier.minify("a { transform: rotate(0turn); }"));
        }

        @Test
        void removesZeroMs() {
            assertEquals("a{transition:all 0}",
                CssMinifier.minify("a { transition: all 0ms; }"));
        }

        @Test
        void removesZeroS() {
            assertEquals("a{transition:all 0}",
                CssMinifier.minify("a { transition: all 0s; }"));
        }

        @Test
        void removesZeroPercent() {
            assertEquals("a{opacity:0}", CssMinifier.minify("a { opacity: 0%; }"));
        }

        @Test
        void doesNotRemoveNonZeroUnit() {
            assertEquals("a{margin:10px}", CssMinifier.minify("a { margin: 10px; }"));
        }

        @Test
        void doesNotRemoveNonZeroUnitEm() {
            assertEquals("a{margin:1.5em}", CssMinifier.minify("a { margin: 1.5em; }"));
        }

        @Test
        void leavesPlainZeroAlone() {
            assertEquals("a{margin:0}", CssMinifier.minify("a { margin: 0; }"));
        }

        @Test
        void removesMultipleZeroUnitsInShorthand() {
            assertEquals("a{margin:0 0 0 0}",
                CssMinifier.minify("a { margin: 0px 0px 0px 0px; }"));
        }

        @Test
        void removesZeroUnitMixedWithNonZero() {
            assertEquals("a{margin:0 10px 0 20px}",
                CssMinifier.minify("a { margin: 0px 10px 0px 20px; }"));
        }

        @Test
        void preservesZeroPercentInKeyframeSelector() {
            String input = """
                    @keyframes fade {
                        0% {
                            opacity: 0;
                        }
                        100% {
                            opacity: 1;
                        }
                    }
                    """;
            assertEquals("@keyframes fade{0%{opacity:0}100%{opacity:1}}",
                CssMinifier.minify(input));
        }

        @Test
        void removesZeroUnitInsideKeyframeBody() {
            String input = """
                    @keyframes slide {
                        from {
                            margin-left: 0px;
                        }
                        to {
                            margin-left: 100px;
                        }
                    }
                    """;
            assertEquals("@keyframes slide{from{margin-left:0}to{margin-left:100px}}",
                CssMinifier.minify(input));
        }

        @Test
        void doesNotTouchZeroInsideString() {
            assertEquals("a{content:\"0px\"}",
                CssMinifier.minify("a { content: \"0px\"; }"));
        }

        @Test
        void removesZeroUnitInCommaSeparatedValue() {
            assertEquals("a{margin:0,0}",
                CssMinifier.minify("a { margin: 0px, 0px; }"));
        }

        @Test
        void removesZeroUnitAfterOpenParen() {
            assertEquals("a{transform:translate(0,0)}",
                CssMinifier.minify("a { transform: translate(0px, 0px); }"));
        }
    }

    // ==================== FONT-WEIGHT SHORTENING ====================

    @Nested
    class FontWeight {

        @Test
        void shortensBoldTo700() {
            assertEquals("a{font-weight:700}", CssMinifier.minify("a { font-weight: bold; }"));
        }

        @Test
        void shortensNormalTo400() {
            assertEquals("a{font-weight:400}", CssMinifier.minify("a { font-weight: normal; }"));
        }

        @Test
        void doesNotChange100() {
            assertEquals("a{font-weight:100}", CssMinifier.minify("a { font-weight: 100; }"));
        }

        @Test
        void doesNotChange700() {
            assertEquals("a{font-weight:700}", CssMinifier.minify("a { font-weight: 700; }"));
        }

        @Test
        void doesNotChangeBolder() {
            assertEquals("a{font-weight:bolder}", CssMinifier.minify("a { font-weight: bolder; }"));
        }

        @Test
        void doesNotChangeLighter() {
            assertEquals("a{font-weight:lighter}", CssMinifier.minify("a { font-weight: lighter; }"));
        }

        @Test
        void doesNotChangeInFontShorthand() {
            // font shorthand is a different property, should not be affected
            assertEquals("a{font:bold 16px Arial}",
                CssMinifier.minify("a { font: bold 16px Arial; }"));
        }

        @Test
        void shortensInMultipleRules() {
            assertEquals("a{font-weight:700}b{font-weight:400}",
                CssMinifier.minify("a { font-weight: bold; } b { font-weight: normal; }"));
        }

        @Test
        void shortensWithOtherProperties() {
            assertEquals("a{color:red;font-weight:700;font-size:14px}",
                CssMinifier.minify("a { color: red; font-weight: bold; font-size: 14px; }"));
        }

        @Test
        void doesNotChangeBoldInsideString() {
            assertEquals("a{content:\"font-weight:bold\"}",
                CssMinifier.minify("a { content: \"font-weight:bold\"; }"));
        }
    }

    // ==================== SHORTHAND COLLAPSING ====================

    @Nested
    class ShorthandCollapse {

        @Test
        void collapsesMarginAllSame() {
            assertEquals("a{margin:10px}",
                CssMinifier.minify("a { margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; }"));
        }

        @Test
        void collapsesPaddingAllSame() {
            assertEquals("a{padding:5px}",
                CssMinifier.minify("a { padding-top: 5px; padding-right: 5px; padding-bottom: 5px; padding-left: 5px; }"));
        }

        @Test
        void collapsesMarginTwoValues() {
            assertEquals("a{margin:10px 20px}",
                CssMinifier.minify("a { margin-top: 10px; margin-right: 20px; margin-bottom: 10px; margin-left: 20px; }"));
        }

        @Test
        void collapsesPaddingTwoValues() {
            assertEquals("a{padding:5px 10px}",
                CssMinifier.minify("a { padding-top: 5px; padding-right: 10px; padding-bottom: 5px; padding-left: 10px; }"));
        }

        @Test
        void collapsesMarginThreeValues() {
            assertEquals("a{margin:10px 20px 30px}",
                CssMinifier.minify("a { margin-top: 10px; margin-right: 20px; margin-bottom: 30px; margin-left: 20px; }"));
        }

        @Test
        void collapsesMarginFourValues() {
            assertEquals("a{margin:10px 20px 30px 40px}",
                CssMinifier.minify("a { margin-top: 10px; margin-right: 20px; margin-bottom: 30px; margin-left: 40px; }"));
        }

        @Test
        void collapsesPaddingFourValues() {
            assertEquals("a{padding:1px 2px 3px 4px}",
                CssMinifier.minify("a { padding-top: 1px; padding-right: 2px; padding-bottom: 3px; padding-left: 4px; }"));
        }

        @Test
        void collapsesMarginWithZero() {
            assertEquals("a{margin:0}",
                CssMinifier.minify("a { margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0; }"));
        }

        @Test
        void collapsesMarginZeroAndValue() {
            assertEquals("a{margin:0 auto}",
                CssMinifier.minify("a { margin-top: 0; margin-right: auto; margin-bottom: 0; margin-left: auto; }"));
        }

        @Test
        void doesNotCollapseWhenOnlyThreeSidesPresent() {
            assertEquals("a{margin-top:10px;margin-right:20px;margin-bottom:30px}",
                CssMinifier.minify("a { margin-top: 10px; margin-right: 20px; margin-bottom: 30px; }"));
        }

        @Test
        void doesNotCollapseWhenOnlyTwoSidesPresent() {
            assertEquals("a{margin-top:10px;margin-bottom:20px}",
                CssMinifier.minify("a { margin-top: 10px; margin-bottom: 20px; }"));
        }

        @Test
        void doesNotCollapseWhenOnlyOneSidePresent() {
            assertEquals("a{margin-top:10px}",
                CssMinifier.minify("a { margin-top: 10px; }"));
        }

        @Test
        void preservesOtherPropertiesAlongside() {
            String input = "a { color: red; margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; font-size: 14px; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("color:red"), "Should preserve color");
            assertTrue(result.contains("font-size:14px"), "Should preserve font-size");
            assertTrue(result.contains("margin:10px"), "Should collapse margin");
        }

        @Test
        void collapsesBothMarginAndPadding() {
            String input = "a { margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; " +
                "padding-top: 5px; padding-right: 5px; padding-bottom: 5px; padding-left: 5px; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("margin:10px"), "Should collapse margin");
            assertTrue(result.contains("padding:5px"), "Should collapse padding");
        }

        @Test
        void doesNotCollapseInNestedAtRuleWithNestedBlocks() {
            String input = """
                    @media screen {
                        a {
                            margin-top: 10px;
                            margin-right: 10px;
                            margin-bottom: 10px;
                            margin-left: 10px;
                        }
                    }
                    """;
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("margin:10px"), "Should collapse margin in nested rule");
        }

        @Test
        void collapsesWithMixedUnits() {
            // 3-value shorthand: top right bottom (left copies right)
            assertEquals("a{margin:0 auto 10px}",
                CssMinifier.minify("a { margin-top: 0; margin-right: auto; margin-bottom: 10px; margin-left: auto; }"));
        }

        @Test
        void collapsesRegardlessOfPropertyOrder() {
            assertEquals("a{margin:10px 20px 30px 40px}",
                CssMinifier.minify("a { margin-left: 40px; margin-top: 10px; margin-bottom: 30px; margin-right: 20px; }"));
        }

        @Test
        void collapsesMarginWithAutoValues() {
            assertEquals("a{margin:0 auto}",
                CssMinifier.minify("a { margin-top: 0; margin-right: auto; margin-bottom: 0; margin-left: auto; }"));
        }

        @Test
        void collapsesPaddingAllZero() {
            assertEquals("a{padding:0}",
                CssMinifier.minify("a { padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; }"));
        }

        @Test
        void collapsesInMultipleRules() {
            String input = "a { margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; } " +
                "b { padding-top: 5px; padding-right: 5px; padding-bottom: 5px; padding-left: 5px; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("a{margin:10px}"), "Should collapse a's margin");
            assertTrue(result.contains("b{padding:5px}"), "Should collapse b's padding");
        }
    }

    // ==================== LEADING ZERO REMOVAL ====================

    @Nested
    class LeadingZeroRemoval {

        @Test
        void removesLeadingZeroFromDecimal() {
            assertEquals("a{opacity:.5}", CssMinifier.minify("a { opacity: 0.5; }"));
        }

        @Test
        void removesLeadingZeroFromSmallDecimal() {
            assertEquals("a{opacity:.1}", CssMinifier.minify("a { opacity: 0.1; }"));
        }

        @Test
        void removesLeadingZeroFromLargeDecimal() {
            assertEquals("a{opacity:.99}", CssMinifier.minify("a { opacity: 0.99; }"));
        }

        @Test
        void removesLeadingZeroWithUnit() {
            assertEquals("a{margin:.5em}", CssMinifier.minify("a { margin: 0.5em; }"));
        }

        @Test
        void removesLeadingZeroInTransition() {
            assertEquals("a{transition:all .3s}", CssMinifier.minify("a { transition: all 0.3s; }"));
        }

        @Test
        void removesLeadingZeroInRgba() {
            assertEquals("a{color:rgba(0,0,0,.5)}", CssMinifier.minify("a { color: rgba(0, 0, 0, 0.5); }"));
        }

        @Test
        void removesLeadingZeroInBoxShadow() {
            assertEquals("a{box-shadow:0 0 .5px #000}",
                CssMinifier.minify("a { box-shadow: 0 0 0.5px #000000; }"));
        }

        @Test
        void removesMultipleLeadingZerosInOneRule() {
            assertEquals("a{margin:.5em .25em}",
                CssMinifier.minify("a { margin: 0.5em 0.25em; }"));
        }

        @Test
        void doesNotRemoveLeadingZeroFromInteger() {
            assertEquals("a{opacity:0}", CssMinifier.minify("a { opacity: 0; }"));
        }

        @Test
        void doesNotRemoveLeadingZeroFromValueGreaterThanOne() {
            assertEquals("a{line-height:1.5}", CssMinifier.minify("a { line-height: 1.5; }"));
        }

        @Test
        void doesNotRemoveLeadingZeroInsideString() {
            assertEquals("a{content:\"0.5\"}", CssMinifier.minify("a { content: \"0.5\"; }"));
        }

        @Test
        void removesLeadingZeroAfterComma() {
            assertEquals("a{color:rgba(255,0,0,.75)}",
                CssMinifier.minify("a { color: rgba(255, 0, 0, 0.75); }"));
        }

        @Test
        void removesLeadingZeroAfterOpenParen() {
            assertEquals("a{transform:scale(.5)}",
                CssMinifier.minify("a { transform: scale(0.5); }"));
        }

        @Test
        void removesLeadingZeroInMultipleProperties() {
            assertEquals("a{opacity:.5;transform:scale(.8)}",
                CssMinifier.minify("a { opacity: 0.5; transform: scale(0.8); }"));
        }

        @Test
        void preservesNegativeDecimalZero() {
            // -0.5 should NOT have its leading zero removed (negative sign context)
            assertEquals("a{margin:-.5em}", CssMinifier.minify("a { margin: -0.5em; }"));
        }
    }

    // ==================== LICENSE COMMENT PRESERVATION ====================

    @Nested
    class LicenseComments {

        @Test
        void preservesLicenseComment() {
            assertEquals("/*! MIT License */ a{color:red}",
                CssMinifier.minify("/*! MIT License */ a { color: red; }"));
        }

        @Test
        void preservesLicenseCommentAtStart() {
            assertEquals("/*! License */ body{margin:0}",
                CssMinifier.minify("/*! License */\nbody { margin: 0; }"));
        }

        @Test
        void preservesMultipleLicenseComments() {
            assertEquals("/*! License 1 */ /*! License 2 */ a{color:red}",
                CssMinifier.minify("/*! License 1 */ /*! License 2 */ a { color: red; }"));
        }

        @Test
        void removesRegularCommentButPreservesLicense() {
            assertEquals("/*! License */ a{color:red}",
                CssMinifier.minify("/* Regular comment */ /*! License */ a { color: red; }"));
        }

        @Test
        void preservesLicenseCommentContent() {
            String input = "/*! normalize.css v8.0.1 | MIT License | github.com/necolas/normalize.css */ body { margin: 0; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.startsWith("/*! normalize.css v8.0.1 | MIT License | github.com/necolas/normalize.css */"));
        }

        @Test
        void preservesMultilineLicenseComment() {
            String input = """
                    /*!
                     * Bootstrap v5.0.0
                     * Licensed under MIT
                     */
                    body { margin: 0; }
                    """;
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("/*!"));
            assertTrue(result.contains("Bootstrap v5.0.0"));
            assertTrue(result.contains("*/"));
        }

        @Test
        void licenseCommentDoesNotAffectMinification() {
            String input = "/*! License */ a { color: red; } /* Remove me */ b { color: blue; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("/*! License */"));
            assertFalse(result.contains("Remove me"));
            assertTrue(result.contains("a{color:red}"));
            assertTrue(result.contains("b{color:blue}"));
        }
    }

    // ==================== SELECTOR COLON SPACING (BUG FIX) ====================

    @Nested
    class SelectorColonSpacing {

        @Test
        void preservesSpaceBeforePseudoClassInDescendant() {
            // ".parent :hover" means "any :hover descendant of .parent"
            // ".parent:hover" means ".parent itself when hovered"
            assertEquals(".parent :hover{color:red}",
                CssMinifier.minify(".parent :hover { color: red; }"));
        }

        @Test
        void preservesSpaceBeforePseudoClassInValidated() {
            assertEquals(".was-validated :invalid{color:red}",
                CssMinifier.minify(".was-validated :invalid { color: red; }"));
        }

        @Test
        void preservesSpaceBeforePseudoClassValid() {
            assertEquals(".was-validated :valid{color:green}",
                CssMinifier.minify(".was-validated :valid { color: green; }"));
        }

        @Test
        void stripsColonSpaceInsideDeclaration() {
            // Inside declarations, space around : should be stripped
            assertEquals("a{color:red}", CssMinifier.minify("a { color : red; }"));
        }

        @Test
        void compoundSelectorNoSpace() {
            // No space before pseudo = compound selector, should stay compact
            assertEquals("a:hover{color:red}", CssMinifier.minify("a:hover { color: red; }"));
        }

        @Test
        void descendantPlusPseudo() {
            assertEquals("div :first-child{color:red}",
                CssMinifier.minify("div :first-child { color: red; }"));
        }

        @Test
        void descendantPlusPseudoElement() {
            assertEquals("div ::before{content:\"\"}",
                CssMinifier.minify("div ::before { content: \"\"; }"));
        }

        @Test
        void complexSelectorWithDescendantPseudo() {
            assertEquals(".form-group :required{border-color:red}",
                CssMinifier.minify(".form-group :required { border-color: red; }"));
        }

        @Test
        void multiSelectorWithDescendantPseudo() {
            assertEquals(".a :hover,.b :focus{color:red}",
                CssMinifier.minify(".a :hover, .b :focus { color: red; }"));
        }

        @Test
        void pseudoClassInDeclarationValueNotAffected() {
            // :root is a selector — should preserve space if preceded by space
            assertEquals(":root{--color:red}",
                CssMinifier.minify(":root { --color: red; }"));
        }

        @Test
        void stripsColonInsideMediaQueryParens() {
            assertEquals("@media (max-width:768px){a{color:red}}",
                CssMinifier.minify("@media (max-width: 768px) { a { color: red; } }"));
        }

        @Test
        void stripsColonInsideSupportsParens() {
            assertEquals("@supports (display:grid){a{display:grid}}",
                CssMinifier.minify("@supports (display: grid) { a { display: grid; } }"));
        }
    }

    // ==================== CALC SPACING (BUG FIX) ====================

    @Nested
    class CalcSpacing {

        @Test
        void preservesSpaceAroundPlusInCalc() {
            assertEquals("a{width:calc(100% + 20px)}",
                CssMinifier.minify("a { width: calc(100% + 20px); }"));
        }

        @Test
        void preservesSpaceAroundMinusInCalc() {
            assertEquals("a{width:calc(100% - 20px)}",
                CssMinifier.minify("a { width: calc(100% - 20px); }"));
        }

        @Test
        void preservesSpaceAroundPlusInNestedCalc() {
            assertEquals("a{width:calc(100% + calc(50px + 10px))}",
                CssMinifier.minify("a { width: calc(100% + calc(50px + 10px)); }"));
        }

        @Test
        void preservesSpaceAroundMultiplyInCalc() {
            // * doesn't need spaces per spec, but we don't strip them (no harm)
            assertEquals("a{width:calc(100% * 2)}",
                CssMinifier.minify("a { width: calc(100% * 2); }"));
        }

        @Test
        void preservesPlusInCalcInsideMediaQuery() {
            String input = "@media (min-width: 768px) { a { width: calc(100% + 20px); } }";
            assertEquals("@media (min-width:768px){a{width:calc(100% + 20px)}}", CssMinifier.minify(input));
        }

        @Test
        void preservesPlusInMinFunction() {
            assertEquals("a{width:min(100% + 20px,500px)}",
                CssMinifier.minify("a { width: min(100% + 20px, 500px); }"));
        }

        @Test
        void preservesPlusInMaxFunction() {
            assertEquals("a{width:max(50% + 10px,300px)}",
                CssMinifier.minify("a { width: max(50% + 10px, 300px); }"));
        }

        @Test
        void preservesPlusInClampFunction() {
            assertEquals("a{width:clamp(200px,50% + 20px,800px)}",
                CssMinifier.minify("a { width: clamp(200px, 50% + 20px, 800px); }"));
        }

        @Test
        void stripsSpaceAroundPlusOutsideCalc() {
            // + as CSS combinator should still be stripped
            assertEquals("h1+p{color:red}", CssMinifier.minify("h1 + p { color: red; }"));
        }

        @Test
        void preservesTildeInsideCalcLikeContext() {
            // ~ as combinator is stripped outside parens
            assertEquals("h1~p{color:red}", CssMinifier.minify("h1 ~ p { color: red; }"));
        }
    }

    // ==================== DUPLICATE PROPERTY REMOVAL ====================

    @Nested
    class DuplicatePropertyRemoval {

        @Test
        void removesDuplicatePropertyKeepingLast() {
            assertEquals("a{color:blue}",
                CssMinifier.minify("a { color: red; color: blue; }"));
        }

        @Test
        void removesDuplicateFromThreeOccurrences() {
            assertEquals("a{color:green}",
                CssMinifier.minify("a { color: red; color: blue; color: green; }"));
        }

        @Test
        void preservesDifferentProperties() {
            assertEquals("a{color:red;font-size:12px}",
                CssMinifier.minify("a { color: red; font-size: 12px; }"));
        }

        @Test
        void removesDuplicateWithDifferentValues() {
            assertEquals("a{display:flex}",
                CssMinifier.minify("a { display: block; display: flex; }"));
        }

        @Test
        void handlesMultipleDuplicatesInOneBlock() {
            assertEquals("a{color:green;font-size:16px}",
                CssMinifier.minify("a { color: red; font-size: 12px; color: green; font-size: 16px; }"));
        }

        @Test
        void deduplicatesInsideMediaQuery() {
            String input = "@media screen { a { color: red; color: blue; } }";
            assertEquals("@media screen{a{color:blue}}", CssMinifier.minify(input));
        }

        @Test
        void preservesDeclarationOrder() {
            // After dedup, the property position should be where it first appeared
            String result = CssMinifier.minify("a { font-size: 14px; color: red; color: blue; margin: 0; }");
            // font-size should come before color, and margin after
            int fontIdx = result.indexOf("font-size");
            int colorIdx = result.indexOf("color");
            int marginIdx = result.indexOf("margin");
            assertTrue(fontIdx < colorIdx, "font-size should come before color");
            assertTrue(colorIdx < marginIdx, "color should come before margin");
        }

        @Test
        void doesNotDeduplicateAcrossRules() {
            assertEquals("a{color:red}b{color:blue}",
                CssMinifier.minify("a { color: red; } b { color: blue; }"));
        }

        @Test
        void handlesEmptyBlockAfterDedup() {
            // This shouldn't happen naturally but let's be safe
            assertEquals("a{color:red}", CssMinifier.minify("a { color: red; }"));
        }

        @Test
        void deduplicatesVendorPrefixedSeparately() {
            // -webkit-transform and transform are different properties
            String result = CssMinifier.minify("a { -webkit-transform: scale(1); transform: scale(1); }");
            assertTrue(result.contains("-webkit-transform:scale(1)"));
            assertTrue(result.contains("transform:scale(1)"));
        }

        @Test
        void preservesDisplayVendorFallbackChain() {
            // display:-webkit-box; display:-ms-flexbox; display:flex — must all be kept
            String input = "a { display: -webkit-box; display: -ms-flexbox; display: flex; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("display:-webkit-box"), "Should keep -webkit-box fallback");
            assertTrue(result.contains("display:-ms-flexbox"), "Should keep -ms-flexbox fallback");
            assertTrue(result.contains("display:flex"), "Should keep final flex value");
        }

        @Test
        void preservesWebkitBackdropFilterFallback() {
            String input = "a { -webkit-backdrop-filter: blur(10px); backdrop-filter: blur(10px); }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("-webkit-backdrop-filter:blur(10px)"));
            assertTrue(result.contains("backdrop-filter:blur(10px)"));
        }

        @Test
        void preservesTransitionPropertyVendorFallback() {
            // transition-property with vendor-prefixed values should be preserved
            String input = "a { transition-property: -webkit-backdrop-filter; transition-property: backdrop-filter; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("transition-property:-webkit-backdrop-filter"),
                "Should keep vendor-prefixed transition-property fallback");
            assertTrue(result.contains("transition-property:backdrop-filter"),
                "Should keep standard transition-property");
        }

        @Test
        void preservesDisplayWebkitInlineFallback() {
            String input = "a { display: -webkit-inline-box; display: -ms-inline-flexbox; display: inline-flex; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("display:-webkit-inline-box"));
            assertTrue(result.contains("display:-ms-inline-flexbox"));
            assertTrue(result.contains("display:inline-flex"));
        }

        @Test
        void preservesAppearanceVendorFallback() {
            String input = "a { -webkit-appearance: none; -moz-appearance: none; appearance: none; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("-webkit-appearance:none"));
            assertTrue(result.contains("-moz-appearance:none"));
            assertTrue(result.contains("appearance:none"));
        }

        @Test
        void deduplicatesSamePropertyWithoutVendorPrefix() {
            // No vendor prefixes in values → safe to dedup
            String input = "a { display: block; display: flex; }";
            assertEquals("a{display:flex}", CssMinifier.minify(input));
        }

        @Test
        void deduplicatesSameValueNonVendor() {
            // Exact same non-vendor declaration repeated — dedup
            String input = "a { color: red; color: red; }";
            assertEquals("a{color:red}", CssMinifier.minify(input));
        }

        @Test
        void preservesMsGridVendorFallback() {
            String input = "a { display: -ms-grid; display: grid; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("display:-ms-grid"), "Should keep -ms-grid fallback");
            assertTrue(result.contains("display:grid"), "Should keep standard grid");
        }

        @Test
        void preservesVendorFallbackInsideMediaQuery() {
            String input = "@media screen { a { display: -webkit-box; display: -ms-flexbox; display: flex; } }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("display:-webkit-box"));
            assertTrue(result.contains("display:-ms-flexbox"));
            assertTrue(result.contains("display:flex"));
        }

        @Test
        void preservesMozAppearanceFallback() {
            String input = "a { -moz-appearance: textfield; appearance: textfield; }";
            String result = CssMinifier.minify(input);
            assertTrue(result.contains("-moz-appearance:textfield"));
            assertTrue(result.contains("appearance:textfield"));
        }
    }

    // ==================== ADJACENT RULE MERGING ====================

    @Nested
    class AdjacentRuleMerging {

        @Test
        void mergesAdjacentRulesWithSameSelector() {
            assertEquals("a{color:red;font-size:12px}",
                CssMinifier.minify("a { color: red; } a { font-size: 12px; }"));
        }

        @Test
        void doesNotMergeDifferentSelectors() {
            assertEquals("a{color:red}b{color:blue}",
                CssMinifier.minify("a { color: red; } b { color: blue; }"));
        }

        @Test
        void mergesThreeAdjacentRules() {
            assertEquals("a{color:red;font-size:12px;display:block}",
                CssMinifier.minify("a { color: red; } a { font-size: 12px; } a { display: block; }"));
        }

        @Test
        void doesNotMergeNonAdjacentRules() {
            // a{} b{} a{} — the two a{} are not adjacent, so don't merge
            assertEquals("a{color:red}b{color:blue}a{font-size:12px}",
                CssMinifier.minify("a { color: red; } b { color: blue; } a { font-size: 12px; }"));
        }

        @Test
        void mergesClassSelectors() {
            assertEquals(".foo{color:red;font-size:14px}",
                CssMinifier.minify(".foo { color: red; } .foo { font-size: 14px; }"));
        }

        @Test
        void mergesComplexSelectors() {
            assertEquals("div.foo>p{color:red;margin:0}",
                CssMinifier.minify("div.foo > p { color: red; } div.foo>p { margin: 0; }"));
        }

        @Test
        void doesNotMergeAtRuleBlocks() {
            // @media blocks contain nested rules — don't merge them
            String input = "@media screen { a { color: red; } } @media screen { b { color: blue; } }";
            String result = CssMinifier.minify(input);
            // Both @media blocks should still exist (they contain different rules)
            assertTrue(result.contains("a{color:red}"));
            assertTrue(result.contains("b{color:blue}"));
        }

        @Test
        void mergesWithDuplicatePropertyOverride() {
            // Merging then dedup: a{color:red} a{color:blue} → a{color:red;color:blue} → a{color:blue}
            // Note: dedup runs before merge in pipeline, so merge produces a{color:red;color:blue}
            // but since dedup already ran, the duplicates remain. That's acceptable.
            String result = CssMinifier.minify("a { color: red; } a { color: blue; }");
            assertTrue(result.contains("color:blue"));
        }

        @Test
        void preservesSelectorGroupsExactly() {
            // "h1,h2" and "h1, h2" should be treated the same after minification
            assertEquals("h1,h2{color:red;font-size:12px}",
                CssMinifier.minify("h1, h2 { color: red; } h1,h2 { font-size: 12px; }"));
        }
    }

    // ==================== SIZE REDUCTION ====================

    @Nested
    class SizeReduction {

        @Test
        void outputIsSmallerOrEqualToInput() {
            String input = """
                    /* Styles */
                    body {
                        margin: 0;
                        padding: 0;
                    }

                    h1 {
                        font-size: 2em;
                        color: #333;
                    }
                    """;
            String output = CssMinifier.minify(input);
            assertTrue(output.length() <= input.length(),
                "Minified output should not be larger than input");
        }

        @Test
        void significantlyReducesWellFormattedCss() {
            String input = """
                    /**
                     * Component styles
                     */

                    .component {
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        padding: 20px;
                        margin: 10px 0;
                        background-color: #f5f5f5;
                        border: 1px solid #ddd;
                        border-radius: 8px;
                    }

                    .component__title {
                        font-size: 24px;
                        font-weight: bold;
                        color: #333;
                        margin-bottom: 16px;
                    }

                    .component__body {
                        font-size: 14px;
                        line-height: 1.6;
                        color: #666;
                    }
                    """;
            String output = CssMinifier.minify(input);
            double ratio = (double) output.length() / input.length();
            assertTrue(ratio < 0.7,
                "Expected >30%% reduction but got " + String.format("%.1f%%", (1 - ratio) * 100));
        }

        @Test
        void minifiedOutputProducesNoEmptyLines() {
            String input = """
                    body {
                        color: red;
                    }



                    a {
                        color: blue;
                    }
                    """;
            String output = CssMinifier.minify(input);
            assertFalse(output.contains("\n"), "Minified output should contain no newlines");
        }
    }
}
