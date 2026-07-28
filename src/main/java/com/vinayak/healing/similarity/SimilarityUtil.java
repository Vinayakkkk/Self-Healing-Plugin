
package com.vinayak.healing.similarity;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public final class SimilarityUtil {

    private SimilarityUtil() {
    }

    private static final Pattern CAMEL_CASE =
            Pattern.compile("([a-z])([A-Z])");

    private static final Pattern MULTIPLE_SPACES =
            Pattern.compile("\\s+");

    private static final Set<String> GENERIC_WORDS =
            Set.of(
                    "input",
                    "textbox",
                    "text",
                    "field",
                    "button",
                    "btn",
                    "link",
                    "label",
                    "icon",
                    "image",
                    "img",
                    "span",
                    "div",
                    "container",
                    "panel",
                    "section",
                    "menu",
                    "item",
                    "element",
                    "component",
                    "control",
                    "box",
                    "area",
                    "form",
                    "tab",
                    "header",
                    "footer",
                    "content",
                    "wrapper",
                    "layout",
                    "page",
                    "dialog",
                    "popup",
                    "window"
            );

    private static final Map<String, String> SYNONYMS =
            createSynonymMap();

    private static Map<String, String> createSynonymMap() {

        Map<String, String> map = new HashMap<>();

        map.put("btn", "button");
        map.put("txt", "text");
        map.put("usr", "user");
        map.put("pwd", "password");
        map.put("emp", "employee");
        map.put("fname", "firstname");
        map.put("lname", "lastname");
        map.put("addr", "address");
        map.put("dept", "department");
        map.put("dob", "birth");
        map.put("tel", "phone");
        map.put("mob", "mobile");
        map.put("msg", "message");
        map.put("img", "image");
        map.put("desc", "description");
        map.put("qty", "quantity");

        return Collections.unmodifiableMap(map);
    }

    public static String normalize(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = splitCamelCase(text);

        text = text.replace('_', ' ');
        text = text.replace('-', ' ');
        text = text.replace('.', ' ');
        text = text.replace('/', ' ');

        text = Normalizer.normalize(
                text,
                Normalizer.Form.NFD
        );

        text = text.replaceAll("\\p{M}", "");

        text = text.toLowerCase(Locale.ENGLISH);

        text = MULTIPLE_SPACES
                .matcher(text)
                .replaceAll(" ")
                .trim();

        return text;
    }

    public static String splitCamelCase(String value) {

        if (value == null) {
            return "";
        }

        return CAMEL_CASE
                .matcher(value)
                .replaceAll("$1 $2");
    }

    public static List<String> tokenize(String text) {

        text = normalize(text);

        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(text.split(" "))
                .filter(s -> !s.isBlank())
                .toList();
    }

    public static List<String> meaningfulTokens(String text) {

        List<String> tokens = tokenize(text);

        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String token : tokens) {

            token = SYNONYMS.getOrDefault(token, token);

            if (GENERIC_WORDS.contains(token)) {
                continue;
            }

            result.add(token);
        }

        return result;
    }

    public static String normalizeVariableName(
            String variableName) {

        return String.join(
                " ",
                meaningfulTokens(variableName)
        );
    }

    public static String normalizeLabel(
            String label) {

        return String.join(
                " ",
                meaningfulTokens(label)
        );
    }

    public static boolean containsMeaningfulToken(
            String source,
            String target) {

        Set<String> sourceTokens =
                new HashSet<>(meaningfulTokens(source));

        Set<String> targetTokens =
                new HashSet<>(meaningfulTokens(target));

        for (String token : sourceTokens) {

            if (targetTokens.contains(token)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGenericToken(
            String token) {

        if (token == null) {
            return true;
        }

        token = normalize(token);

        return GENERIC_WORDS.contains(token);
    }

    public static Set<String> tokenSet(String text) {

        return new LinkedHashSet<>(
                meaningfulTokens(text)
        );
    }
        public static double stringSimilarity(
            String first,
            String second) {

        first = normalize(first);
        second = normalize(second);

        if (first.isBlank() && second.isBlank()) {
            return 1.0;
        }

        if (first.isBlank() || second.isBlank()) {
            return 0.0;
        }

        return levenshteinSimilarity(first, second);
    }

    public static double tokenSimilarity(
            String first,
            String second) {

        Set<String> firstTokens =
                tokenSet(first);

        Set<String> secondTokens =
                tokenSet(second);

        if (firstTokens.isEmpty() &&
                secondTokens.isEmpty()) {

            return 1.0;
        }

        if (firstTokens.isEmpty() ||
                secondTokens.isEmpty()) {

            return 0.0;
        }

        int matches = 0;

        for (String token : firstTokens) {

            if (secondTokens.contains(token)) {
                matches++;
            }
        }

        return (double) matches /
                Math.max(
                        firstTokens.size(),
                        secondTokens.size()
                );
    }

    public static double jaccardSimilarity(
            String first,
            String second) {

        Set<String> union =
                new HashSet<>(tokenSet(first));

        Set<String> intersection =
                new HashSet<>(tokenSet(first));

        union.addAll(tokenSet(second));

        intersection.retainAll(
                tokenSet(second)
        );

        if (union.isEmpty()) {
            return 1.0;
        }

        return (double) intersection.size()
                / union.size();
    }

    public static int levenshteinDistance(
            String first,
            String second) {

        first = normalize(first);
        second = normalize(second);

        int[][] dp =
                new int[first.length() + 1]
                        [second.length() + 1];

        for (int i = 0;
             i <= first.length();
             i++) {

            dp[i][0] = i;
        }

        for (int j = 0;
             j <= second.length();
             j++) {

            dp[0][j] = j;
        }

        for (int i = 1;
             i <= first.length();
             i++) {

            for (int j = 1;
                 j <= second.length();
                 j++) {

                int cost =
                        first.charAt(i - 1)
                                == second.charAt(j - 1)
                                ? 0
                                : 1;

                dp[i][j] = Math.min(

                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1
                        ),

                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[first.length()]
                [second.length()];
    }

    public static double levenshteinSimilarity(
            String first,
            String second) {

        first = normalize(first);
        second = normalize(second);

        if (first.equals(second)) {
            return 1.0;
        }

        int max =
                Math.max(
                        first.length(),
                        second.length()
                );

        if (max == 0) {
            return 1.0;
        }

        int distance =
                levenshteinDistance(
                        first,
                        second
                );

        return 1.0 -
                ((double) distance / max);
    }

    public static double calculateOverallSimilarity(
            String first,
            String second) {

        double token =
                tokenSimilarity(first, second);

        double string =
                stringSimilarity(first, second);

        double jaccard =
                jaccardSimilarity(first, second);

        return (token * 0.45)
                + (string * 0.35)
                + (jaccard * 0.20);
    }

    public static boolean areSimilar(
            String first,
            String second,
            double threshold) {

        return calculateOverallSimilarity(
                first,
                second
        ) >= threshold;
    }

    public static String bestMatchingToken(
            String source,
            Collection<String> candidates) {

        if (candidates == null ||
                candidates.isEmpty()) {

            return null;
        }

        String best = null;
        double score = -1;

        for (String candidate : candidates) {

            double similarity =
                    calculateOverallSimilarity(
                            source,
                            candidate
                    );

            if (similarity > score) {

                score = similarity;
                best = candidate;
            }
        }

        return best;
    }
    public static double phraseSimilarity(
        String first,
        String second) {

    double token =
            tokenSimilarity(first, second);

    double string =
            stringSimilarity(first, second);

    double jaccard =
            jaccardSimilarity(first, second);

    return (token * 0.40)
            + (string * 0.40)
            + (jaccard * 0.20);
}

public static boolean isSimilar(
        String first,
        String second,
        double threshold) {

    return phraseSimilarity(first, second)
            >= threshold;
}
}