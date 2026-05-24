package com.emoji.converter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateDTO {
    private String emoji;
    private String sourceWord;
    private String sourcePinyin;
    private String indexType;
    private int priority;
    private int variantCount;
}
