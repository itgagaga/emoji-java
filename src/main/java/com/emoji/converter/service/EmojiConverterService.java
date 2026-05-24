package com.emoji.converter.service;

import com.emoji.converter.model.dto.CandidateDTO;
import com.emoji.converter.model.dto.ConvertRequest;
import com.emoji.converter.model.dto.ConvertResponse;
import com.emoji.converter.model.AdminMapping;
import com.emoji.converter.util.FuzzyPinyinUtil;
import com.emoji.converter.util.PinyinUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmojiConverterService {

    private final JsonDataService jsonDataService;
    private final PinyinUtil pinyinUtil;
    private final FuzzyPinyinUtil fuzzyPinyinUtil;
    private final AdminMappingService adminMappingService;

    @Value("${matcher.max-window:4}")
    private int maxWindow;

    @Value("${matcher.max-candidates-per-key:80}")
    private int maxCandidatesPerKey;

    @Value("${matcher.enable-fuzzy:true}")
    private boolean enableFuzzy;

    @Value("${matcher.enable-single-syllable:true}")
    private boolean enableSingleSyllable;

    @Value("${matcher.tts-priority-bonus:20}")
    private int ttsPriorityBonus;

    @Value("${matcher.position-first-bonus:20}")
    private int positionFirstBonus;

    @Value("${matcher.fuzzy-priority-penalty:50}")
    private int fuzzyPriorityPenalty;

    private static final Pattern PUNCT_PATTERN = Pattern.compile(
            "[\\s，。！？、,.!?；;：:（）()《》<>【】\\[\\]\"'\"\"'']"
    );

    public ConvertResponse convert(ConvertRequest request) {
        ConvertResponse response = new ConvertResponse();
        response.setSuccess(true);

        if (request.getText() == null || request.getText().isEmpty()) {
            response.setOutput("");
            return response;
        }

        String text = request.getText();
        List<ConvertResponse.ConvertDetail> wordModeDetails = new ArrayList<>();
        List<ConvertResponse.ConvertDetail> charModeDetails = new ArrayList<>();

        String wordModeResult = convertInMode(text, "word", wordModeDetails);
        String charModeResult = convertInMode(text, "char", charModeDetails);

        String output;
        if ("auto".equals(request.getMode()) || "both".equals(request.getMode())) {
            output = wordModeResult;
            response.setWordModeOutput(wordModeResult);
            response.setCharModeOutput(charModeResult);
            response.setWordModeDetails(wordModeDetails);
            response.setCharModeDetails(charModeDetails);
        } else if ("word".equals(request.getMode())) {
            output = wordModeResult;
            response.setWordModeDetails(wordModeDetails);
        } else if ("char".equals(request.getMode())) {
            output = charModeResult;
            response.setCharModeDetails(charModeDetails);
        } else {
            output = wordModeResult;
        }

        response.setOutput(output);
        if (request.getShowDetails() != null && request.getShowDetails()) {
            response.setDetails(wordModeDetails);
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total_chars", text.length());
        statistics.put("converted_chars", countConvertedChars(output));
        statistics.put("conversion_rate", text.length() > 0 ? (double) countConvertedChars(output) / text.length() : 0);
        response.setStatistics(statistics);

        return response;
    }

    private String convertInMode(String text, String mode, List<ConvertResponse.ConvertDetail> details) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < text.length()) {
            char c = text.charAt(i);

            if (PUNCT_PATTERN.matcher(String.valueOf(c)).matches()) {
                result.append(c);
                i++;
                continue;
            }

            if ("word".equals(mode)) {
                boolean matched = false;
                for (int window = Math.min(maxWindow, text.length() - i); window >= 1; window--) {
                    String chunk = text.substring(i, i + window);
                    Map<String, Object> match = lookup(chunk);

                    if (match != null) {
                        String emoji = (String) match.get("emoji");
                        result.append(emoji);

                        if (details != null) {
                            ConvertResponse.ConvertDetail detail = new ConvertResponse.ConvertDetail();
                            detail.setOriginal(chunk);
                            detail.setInputPinyin(pinyinUtil.toPinyinString(chunk).toLowerCase());
                            detail.setConverted(chunk + emoji);
                            detail.setEmoji(emoji);
                            detail.setSourceWord((String) match.getOrDefault("source_word", chunk));
                            detail.setSourcePinyin((String) match.getOrDefault("source_pinyin", ""));
                            detail.setMatchType((String) match.getOrDefault("index_type", "word"));
                            detail.setPriority(((Number) match.getOrDefault("priority", 0)).intValue());
                            details.add(detail);
                        }

                        i += window;
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    result.append(c);
                    i++;
                }
            } else {
                Map<String, Object> match = lookup(String.valueOf(c));
                if (match != null) {
                    String emoji = (String) match.get("emoji");
                    result.append(emoji);

                    if (details != null) {
                        ConvertResponse.ConvertDetail detail = new ConvertResponse.ConvertDetail();
                        detail.setOriginal(String.valueOf(c));
                        detail.setInputPinyin(pinyinUtil.toPinyinString(String.valueOf(c)).toLowerCase());
                        detail.setConverted(c + emoji);
                        detail.setEmoji(emoji);
                        detail.setSourceWord((String) match.getOrDefault("source_word", String.valueOf(c)));
                        detail.setSourcePinyin((String) match.getOrDefault("source_pinyin", ""));
                        detail.setMatchType((String) match.getOrDefault("index_type", "char"));
                        detail.setPriority(((Number) match.getOrDefault("priority", 0)).intValue());
                        details.add(detail);
                    }
                } else {
                    result.append(c);
                }
                i++;
            }
        }

        return result.toString();
    }

    public Map<String, Object> lookup(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> allCandidates = new ArrayList<>();

        List<AdminMapping> adminMappings = adminMappingService.getEnabledMappingsByKey(chunk);
        for (AdminMapping adminMapping : adminMappings) {
            Map<String, Object> candidate = new LinkedHashMap<>();
            candidate.put("emoji", adminMapping.getEmoji());
            candidate.put("source_word", adminMapping.getKey());
            candidate.put("source_pinyin", "pinyin".equals(adminMapping.getType()) ? adminMapping.getKey() : toPinyinCached(adminMapping.getKey()));
            candidate.put("priority", adminMapping.getPriority());
            candidate.put("index_type", getAdminMappingIndexType(adminMapping));
            candidate.put("description", adminMapping.getDescription());
            allCandidates.add(candidate);
        }

        Map<String, List<Map<String, Object>>> wordIndex = jsonDataService.getWordIndex();
        if (wordIndex.containsKey(chunk)) {
            allCandidates.addAll(wordIndex.get(chunk));
        }

        String pinyinKey = toPinyinCached(chunk);
        if (!pinyinKey.isEmpty()) {
            List<AdminMapping> adminPinyinMappings = adminMappingService.getEnabledMappingsByKey(pinyinKey);
            for (AdminMapping adminMapping : adminPinyinMappings) {
                Map<String, Object> candidate = new LinkedHashMap<>();
                candidate.put("emoji", adminMapping.getEmoji());
                candidate.put("source_word", adminMapping.getKey());
                candidate.put("source_pinyin", pinyinKey);
                candidate.put("priority", adminMapping.getPriority());
                candidate.put("index_type", getAdminMappingIndexType(adminMapping));
                candidate.put("description", adminMapping.getDescription());
                allCandidates.add(candidate);
            }

            Map<String, List<Map<String, Object>>> pinyinExactIndex = jsonDataService.getPinyinExactIndex();
            if (pinyinExactIndex.containsKey(pinyinKey)) {
                allCandidates.addAll(pinyinExactIndex.get(pinyinKey));
            }

            if (enableFuzzy) {
                Map<String, List<Map<String, Object>>> pinyinFuzzyIndex = jsonDataService.getPinyinFuzzyIndex();
                if (pinyinFuzzyIndex.containsKey(pinyinKey)) {
                    allCandidates.addAll(pinyinFuzzyIndex.get(pinyinKey));
                }
            }

            if (enableSingleSyllable && chunk.length() == 1) {
                Map<String, List<Map<String, Object>>> pinyinSyllableIndex = jsonDataService.getPinyinSyllableIndex();
                if (pinyinSyllableIndex.containsKey(pinyinKey)) {
                    allCandidates.addAll(pinyinSyllableIndex.get(pinyinKey));
                }
            }
        }

        if (allCandidates.isEmpty()) {
            return null;
        }

        return pickBest(allCandidates);
    }

    public List<CandidateDTO> lookupAllCandidates(String chunk) {
        List<Map<String, Object>> allCandidates = new ArrayList<>();

        List<AdminMapping> adminMappings = adminMappingService.getEnabledMappingsByKey(chunk);
        for (AdminMapping adminMapping : adminMappings) {
            Map<String, Object> candidate = new LinkedHashMap<>();
            candidate.put("emoji", adminMapping.getEmoji());
            candidate.put("source_word", adminMapping.getKey());
            candidate.put("source_pinyin", "pinyin".equals(adminMapping.getType()) ? adminMapping.getKey() : toPinyinCached(adminMapping.getKey()));
            candidate.put("priority", adminMapping.getPriority());
            candidate.put("index_type", getAdminMappingIndexType(adminMapping));
            candidate.put("description", adminMapping.getDescription());
            allCandidates.add(candidate);
        }

        Map<String, List<Map<String, Object>>> wordIndex = jsonDataService.getWordIndex();
        if (wordIndex.containsKey(chunk)) {
            allCandidates.addAll(wordIndex.get(chunk));
        }

        String pinyinKey = toPinyinCached(chunk);
        if (!pinyinKey.isEmpty()) {
            List<AdminMapping> adminPinyinMappings = adminMappingService.getEnabledMappingsByKey(pinyinKey);
            for (AdminMapping adminMapping : adminPinyinMappings) {
                Map<String, Object> candidate = new LinkedHashMap<>();
                candidate.put("emoji", adminMapping.getEmoji());
                candidate.put("source_word", adminMapping.getKey());
                candidate.put("source_pinyin", pinyinKey);
                candidate.put("priority", adminMapping.getPriority());
                candidate.put("index_type", getAdminMappingIndexType(adminMapping));
                candidate.put("description", adminMapping.getDescription());
                allCandidates.add(candidate);
            }

            Map<String, List<Map<String, Object>>> pinyinExactIndex = jsonDataService.getPinyinExactIndex();
            if (pinyinExactIndex.containsKey(pinyinKey)) {
                allCandidates.addAll(pinyinExactIndex.get(pinyinKey));
            }

            if (enableFuzzy) {
                Map<String, List<Map<String, Object>>> pinyinFuzzyIndex = jsonDataService.getPinyinFuzzyIndex();
                if (pinyinFuzzyIndex.containsKey(pinyinKey)) {
                    allCandidates.addAll(pinyinFuzzyIndex.get(pinyinKey));
                }
            }

            if (chunk.length() == 1) {
                Map<String, List<Map<String, Object>>> pinyinSyllableIndex = jsonDataService.getPinyinSyllableIndex();
                if (pinyinSyllableIndex.containsKey(pinyinKey)) {
                    allCandidates.addAll(pinyinSyllableIndex.get(pinyinKey));
                }
            }
        }

        return deduplicateAndRank(allCandidates);
    }

    private List<CandidateDTO> deduplicateAndRank(List<Map<String, Object>> candidates) {
        Map<String, Map<String, Object>> unique = new LinkedHashMap<>();
        for (Map<String, Object> cand : candidates) {
            String key = cand.get("emoji") + "|" + cand.get("source_word");
            Map<String, Object> existing = unique.get(key);
            if (existing == null || getPriority(cand) > getPriority(existing)) {
                unique.put(key, cand);
            }
        }

        Map<String, CandidateDTO> baseEmojiMap = new LinkedHashMap<>();
        List<CandidateDTO> deduped = new ArrayList<>();

        for (Map<String, Object> cand : unique.values()) {
            String emoji = (String) cand.get("emoji");
            String base = getBaseEmoji(emoji);

            CandidateDTO dto = convertToDTO(cand);

            if (!baseEmojiMap.containsKey(base)) {
                baseEmojiMap.put(base, dto);
                deduped.add(dto);
            } else {
                CandidateDTO existing = baseEmojiMap.get(base);
                existing.setVariantCount(existing.getVariantCount() + 1);
                if (dto.getPriority() > existing.getPriority()) {
                    baseEmojiMap.put(base, dto);
                    int idx = deduped.indexOf(existing);
                    if (idx >= 0) {
                        deduped.set(idx, dto);
                    }
                }
            }
        }

        for (CandidateDTO dto : deduped) {
            String base = getBaseEmoji(dto.getEmoji());
            CandidateDTO info = baseEmojiMap.get(base);
            if (info != null && info.getVariantCount() > 0) {
                dto.setVariantCount(info.getVariantCount());
            }
        }

        return deduped.stream()
                .sorted(Comparator.comparingInt(CandidateDTO::getPriority).reversed())
                .collect(Collectors.toList());
    }

    private Map<String, Object> pickBest(List<Map<String, Object>> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        return Collections.max(candidates, Comparator.comparingInt(this::getPriority));
    }

    private String getAdminMappingIndexType(AdminMapping adminMapping) {
        if ("word".equals(adminMapping.getType())) {
            return "word";
        } else {
            String key = adminMapping.getKey();
            if (key != null && key.contains(" ")) {
                return "pinyin_exact";
            } else {
                return "pinyin_syllable";
            }
        }
    }

    private int getPriority(Map<String, Object> candidate) {
        int priority = ((Number) candidate.getOrDefault("priority", 0)).intValue();

        if (Boolean.TRUE.equals(candidate.get("tts_match"))) {
            priority += ttsPriorityBonus;
        }
        if (Boolean.TRUE.equals(candidate.get("position_first"))) {
            priority += positionFirstBonus;
        }
        if (Boolean.TRUE.equals(candidate.get("is_fuzzy"))) {
            priority -= fuzzyPriorityPenalty;
        }

        return priority;
    }

    @Cacheable(value = "pinyinCache", key = "#text")
    public String toPinyinCached(String text) {
        return pinyinUtil.toPinyinString(text).toLowerCase();
    }

    private String getBaseEmoji(String emoji) {
        String[] skinToneModifiers = {"🏻", "🏼", "🏽", "🏾", "🏿"};
        String base = emoji;
        for (String tone : skinToneModifiers) {
            base = base.replace(tone, "");
        }
        return base.isEmpty() ? emoji : base;
    }

    private CandidateDTO convertToDTO(Map<String, Object> cand) {
        CandidateDTO dto = new CandidateDTO();
        dto.setEmoji((String) cand.get("emoji"));
        dto.setSourceWord((String) cand.get("source_word"));
        dto.setSourcePinyin((String) cand.get("source_pinyin"));
        dto.setIndexType((String) cand.getOrDefault("index_type", "unknown"));
        dto.setPriority(getPriority(cand));
        dto.setVariantCount(0);
        return dto;
    }

    private int countConvertedChars(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (Character.isHighSurrogate(c) || Character.getType(c) == Character.SURROGATE) {
                count++;
            }
        }
        return count;
    }

    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("word_keys", jsonDataService.getWordIndex().size());
        stats.put("pinyin_exact_keys", jsonDataService.getPinyinExactIndex().size());
        stats.put("pinyin_syllable_keys", jsonDataService.getPinyinSyllableIndex().size());
        stats.put("pinyin_fuzzy_keys", jsonDataService.getPinyinFuzzyIndex().size());
        stats.put("total_emojis", jsonDataService.getEmojiConcepts().size());

        long totalCandidates = jsonDataService.getWordIndex().values().stream()
                .mapToInt(List::size)
                .sum();
        stats.put("total_word_candidates", totalCandidates);

        return stats;
    }
}
