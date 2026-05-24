package com.emoji.converter.util;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FuzzyPinyinUtil {

    private static final Map<String, String[]> FUZZY_RULES = new HashMap<>();

    static {
        FUZZY_RULES.put("zh", new String[]{"z"});
        FUZZY_RULES.put("ch", new String[]{"c"});
        FUZZY_RULES.put("sh", new String[]{"s"});
        FUZZY_RULES.put("n", new String[]{"l"});
        FUZZY_RULES.put("l", new String[]{"n"});
        FUZZY_RULES.put("an", new String[]{"ang"});
        FUZZY_RULES.put("ang", new String[]{"an"});
        FUZZY_RULES.put("en", new String[]{"eng"});
        FUZZY_RULES.put("eng", new String[]{"en"});
        FUZZY_RULES.put("in", new String[]{"ing"});
        FUZZY_RULES.put("ing", new String[]{"in"});
    }

    public List<String> getFuzzyVariants(String pinyin) {
        Set<String> variants = new HashSet<>();
        variants.add(pinyin);

        for (Map.Entry<String, String[]> entry : FUZZY_RULES.entrySet()) {
            if (pinyin.startsWith(entry.getKey())) {
                String suffix = pinyin.substring(entry.getKey().length());
                for (String replacement : entry.getValue()) {
                    variants.add(replacement + suffix);
                }
            }
        }

        return new ArrayList<>(variants);
    }

    public boolean isFuzzyMatch(String original, String candidate) {
        List<String> variants = getFuzzyVariants(original);
        return variants.contains(candidate);
    }
}
