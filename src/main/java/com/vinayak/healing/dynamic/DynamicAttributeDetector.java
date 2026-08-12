package com.vinayak.healing.dynamic;

import java.util.regex.Pattern;

public class DynamicAttributeDetector {

    private static final Pattern NUMERIC_SUFFIX =
            Pattern.compile("^(.+?)[_-]\\d+$");

    private static final Pattern NUMERIC_PREFIX =
            Pattern.compile("^\\d+[_-](.+)$");

    private static final Pattern UUID =
            Pattern.compile(
                    "^[0-9a-fA-F]{8}-" +
                    "[0-9a-fA-F]{4}-" +
                    "[0-9a-fA-F]{4}-" +
                    "[0-9a-fA-F]{4}-" +
                    "[0-9a-fA-F]{12}$"
            );

    private static final Pattern HEX_HASH =
            Pattern.compile("^[a-fA-F0-9]{16,}$");

    private static final Pattern REACT =
            Pattern.compile("^react-.*\\d+.*");

    private static final Pattern ANGULAR =
            Pattern.compile("^cdk-.*\\d+.*");

    private static final Pattern SESSION =
            Pattern.compile("^session[_-].*");

    private static final Pattern TIMESTAMP =
            Pattern.compile(".*(16|17|18|19|20)\\d{8,}.*");

    public DynamicAttributeResult analyze(
            String attributeName,
            String value) {

        DynamicAttributeResult result =
                new DynamicAttributeResult();

        result.setAttributeName(attributeName);
        result.setOriginalValue(value);
        result.setNormalizedValue(value);
        result.setPatternType(DynamicPatternType.NONE);
        result.setDynamic(false);
        result.setStabilityScore(100);

        if (value == null || value.isBlank()) {
            return result;
        }

        value = value.trim();

        if (UUID.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.UUID);

            result.setDynamic(true);

            result.setNormalizedValue("[UUID]");

            result.setStabilityScore(5);

            return result;
        }

        if (HEX_HASH.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.HEX_HASH);

            result.setDynamic(true);

            result.setNormalizedValue("[HEX_HASH]");

            result.setStabilityScore(10);

            return result;
        }

        if (REACT.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.REACT_ID);

            result.setDynamic(true);

            result.setNormalizedValue(
                    value.replaceAll("\\d+", "[dynamic]"));

            result.setStabilityScore(25);

            return result;
        }

        if (ANGULAR.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.ANGULAR_ID);

            result.setDynamic(true);

            result.setNormalizedValue(
                    value.replaceAll("\\d+", "[dynamic]"));

            result.setStabilityScore(25);

            return result;
        }

        if (SESSION.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.SESSION_ID);

            result.setDynamic(true);

            result.setNormalizedValue(
                    value.replaceAll("\\d+", "[dynamic]"));

            result.setStabilityScore(15);

            return result;
        }

        if (TIMESTAMP.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.TIMESTAMP);

            result.setDynamic(true);

            result.setNormalizedValue(
                    value.replaceAll("\\d+", "[dynamic]"));

            result.setStabilityScore(20);

            return result;
        }

        if (NUMERIC_SUFFIX.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.NUMERIC_SUFFIX);

            result.setDynamic(true);

            result.setNormalizedValue(
                    value.replaceAll("\\d+$", "[dynamic]"));

            result.setStabilityScore(35);

            return result;
        }

        if (NUMERIC_PREFIX.matcher(value).matches()) {

            result.setPatternType(
                    DynamicPatternType.NUMERIC_PREFIX);

            result.setDynamic(true);

            result.setNormalizedValue(
                    value.replaceAll("^\\d+", "[dynamic]"));

            result.setStabilityScore(35);

            return result;
        }

        return result;
    }

}