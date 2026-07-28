package com.vinayak.healing.source;
import com.vinayak.healing.logging.HealingLogger;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;

public class JavaLocatorExtractor {

    public VariableMatch find(
            String sourceCode,
            String failedLocator) {

        if (sourceCode == null
                || sourceCode.isBlank()
                || failedLocator == null
                || failedLocator.isBlank()) {

            return null;
        }

        try {

            CompilationUnit compilationUnit =
                    StaticJavaParser.parse(sourceCode);

            LocatorInfo failed =
                    parseFailedLocator(failedLocator);

            if (failed == null) {

                HealingLogger.debug(
                        "FAILED LOCATOR COULD NOT BE PARSED : "
                                + failedLocator);

                return null;
            }

            // ======================================
            // 1. SEARCH STATIC FIELD LOCATORS
            // ======================================

            for (FieldDeclaration field
                    : compilationUnit.findAll(
                            FieldDeclaration.class)) {

                for (VariableDeclarator variable
                        : field.getVariables()) {

                    if (variable.getInitializer().isEmpty()) {
                        continue;
                    }

                    String initializer =
                            variable.getInitializer()
                                    .get()
                                    .toString();

                    LocatorInfo declared =
                            parseByExpression(initializer);

                    if (declared == null) {
                        continue;
                    }

                    if (!sameLocator(
                            failed,
                            declared)) {

                        continue;
                    }

                    String declaration =
                            field.toString();

                    HealingLogger.debug(
                            "STATIC LOCATOR MATCH FOUND : "
                                    + variable.getNameAsString());

                    HealingLogger.debug(
                            "DECLARATION : "
                                    + declaration);

                    return new VariableMatch(
                            variable.getNameAsString(),
                            declaration,
                            false);
                }
            }

            // ======================================
            // 2. SEARCH DYNAMIC LOCATOR METHODS
            // ======================================

            for (MethodDeclaration method
                    : compilationUnit.findAll(
                            MethodDeclaration.class)) {

                if (!"By".equals(
                        method.getType().asString())) {

                    continue;
                }

                ReturnStmt returnStmt =
                        method.findFirst(
                                ReturnStmt.class)
                                .orElse(null);

                if (returnStmt == null
                        || returnStmt.getExpression()
                                .isEmpty()) {

                    continue;
                }

                String returnExpression =
                        returnStmt.getExpression()
                                .get()
                                .toString();

                LocatorInfo returned =
                        parseByExpression(
                                returnExpression);

                if (returned == null) {
                    continue;
                }

                /*
                 * Dynamic locator methods may contain
                 * runtime parameters, so exact value
                 * comparison is not always possible.
                 *
                 * However, the locator TYPE must match.
                 */
                if (!sameLocatorType(
                        failed.type,
                        returned.type)) {

                    continue;
                }

                /*
                 * If the returned locator is completely
                 * static, it must match exactly.
                 */
                if (!containsDynamicExpression(
                        returnExpression)) {

                    if (!sameLocator(
                            failed,
                            returned)) {

                        continue;
                    }
                }

                HealingLogger.debug(
                        "DYNAMIC LOCATOR METHOD FOUND : "
                                + method.getNameAsString());

                return new VariableMatch(
                        method.getNameAsString(),
                        returnStmt.toString(),
                        true);
            }

       } catch (Exception e) {

    HealingLogger.error(
            "JavaParser lookup failed.", e);

    e.printStackTrace();
}

        return null;
    }

    // ==========================================
    // PARSE SELENIUM FAILED LOCATOR
    // Example:
    // By.className: cart
    // By.cssSelector: [data-test='title']
    // ==========================================

    private LocatorInfo parseFailedLocator(
            String locator) {

        if (locator == null
                || locator.isBlank()) {

            return null;
        }

        int colon =
                locator.indexOf(":");

        if (colon == -1) {
            return null;
        }

        String left =
                locator.substring(
                        0,
                        colon)
                        .trim();

        String value =
                locator.substring(
                        colon + 1)
                        .trim();

        String type =
                left.replace(
                        "By.",
                        "")
                        .trim();

        if (type.isBlank()
                || value.isBlank()) {

            return null;
        }

        return new LocatorInfo(
                normalizeType(type),
                normalizeValue(value));
    }

    // ==========================================
    // PARSE SOURCE CODE By EXPRESSIONS
    //
    // By.id("username")
    // By.className("cart")
    // By.cssSelector("[data-test='title']")
    // ==========================================

    private LocatorInfo parseByExpression(
            String expression) {

        if (expression == null
                || expression.isBlank()) {

            return null;
        }

        try {

            MethodCallExpr methodCall =
                    StaticJavaParser.parseExpression(
                                    expression)
                            .asMethodCallExpr();

            if (methodCall.getScope().isEmpty()) {
                return null;
            }

            if (!"By".equals(
                    methodCall.getScope()
                            .get()
                            .toString())) {

                return null;
            }

            if (methodCall.getArguments()
                    .isEmpty()) {

                return null;
            }

            String type =
                    methodCall.getNameAsString();

            String argument =
                    methodCall.getArgument(0)
                            .toString();

            /*
             * Remove surrounding quotes only.
             *
             * Do not inspect the complete declaration,
             * because variable names such as cartTitle
             * must never influence locator matching.
             */
            if ((argument.startsWith("\"")
                    && argument.endsWith("\""))
                    || (argument.startsWith("'")
                    && argument.endsWith("'"))) {

                argument =
                        argument.substring(
                                1,
                                argument.length() - 1);
            }

            return new LocatorInfo(
                    normalizeType(type),
                    normalizeValue(argument));

        } catch (Exception ignored) {

            return null;
        }
    }

    // ==========================================
    // EXACT LOCATOR COMPARISON
    // ==========================================

    private boolean sameLocator(
            LocatorInfo failed,
            LocatorInfo declared) {

        if (failed == null
                || declared == null) {

            return false;
        }

        return sameLocatorType(
                failed.type,
                declared.type)

                && failed.value.equals(
                        declared.value);
    }

    // ==========================================
    // NORMALIZE EQUIVALENT SELENIUM TYPES
    // ==========================================

    private boolean sameLocatorType(
            String first,
            String second) {

        return normalizeType(first)
                .equals(
                        normalizeType(second));
    }

    private String normalizeType(
            String type) {

        if (type == null) {
            return "";
        }

        String normalized =
                type.trim()
                        .toLowerCase();

        return switch (normalized) {

            case "cssselector", "css" ->
                    "css";

            case "classname", "class" ->
                    "class";

            default ->
                    normalized;
        };
    }

    private String normalizeValue(
            String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // ==========================================
    // DETECT DYNAMIC JAVA EXPRESSIONS
    // ==========================================

    private boolean containsDynamicExpression(
            String expression) {

        if (expression == null) {
            return false;
        }

        /*
         * Examples:
         *
         * By.id("add-to-cart-" + productId)
         *
         * By.xpath("//div[text()='"
         *          + productName
         *          + "']")
         */
        return expression.contains("+");
    }

    // ==========================================
    // INTERNAL LOCATOR MODEL
    // ==========================================

    private static class LocatorInfo {

        private final String type;
        private final String value;

        private LocatorInfo(
                String type,
                String value) {

            this.type = type;
            this.value = value;
        }
    }

    // ==========================================
    // RESULT MODEL
    // ==========================================

    public static class VariableMatch {

        private final String variableName;
        private final String declaration;
        private final boolean dynamic;

        public VariableMatch(
                String variableName,
                String declaration,
                boolean dynamic) {

            this.variableName =
                    variableName;

            this.declaration =
                    declaration;

            this.dynamic =
                    dynamic;
        }

        public String getVariableName() {
            return variableName;
        }

        public String getDeclaration() {
            return declaration;
        }

        public boolean isDynamic() {
            return dynamic;
        }
    }
}