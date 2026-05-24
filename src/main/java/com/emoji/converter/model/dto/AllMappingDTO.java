package com.emoji.converter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllMappingDTO {
    private String id;
    private String type;           // pinyin / word
    private String key;            // 拼音或词语
    private String emoji;
    private String description;
    private int priority;
    private String source;         // original / admin
    private String indexType;      // word / pinyin_exact / pinyin_syllable / pinyin_fuzzy
    private boolean enabled;
}