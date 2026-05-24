package com.emoji.converter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateGroupDTO {
    private String baseEmoji;
    private String sourceWord;
    private String sourcePinyin;
    private String indexType;
    private int priority;
    private String description;
    private List<SkinToneVariant> variants;
    private boolean hasVariants;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkinToneVariant {
        private String emoji;
        private String skinToneName;
        private String skinToneCode;
        private boolean isDefault;
    }
}