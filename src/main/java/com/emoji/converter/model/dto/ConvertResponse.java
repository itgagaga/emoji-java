package com.emoji.converter.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class ConvertResponse {
    private boolean success;
    private String output;
    private List<ConvertDetail> details = List.of();
    private Map<String, Object> statistics = Map.of();
    private String error = "";
    private String wordModeOutput = "";
    private String charModeOutput = "";
    private List<ConvertDetail> wordModeDetails = List.of();
    private List<ConvertDetail> charModeDetails = List.of();

    @Data
    public static class ConvertDetail {
        private String original;
        private String inputPinyin;
        private String converted;
        private String emoji;
        private String sourceWord;
        private String sourcePinyin;
        private String matchType;
        private int priority;
    }
}
