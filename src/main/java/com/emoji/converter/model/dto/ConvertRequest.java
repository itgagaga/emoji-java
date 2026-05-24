package com.emoji.converter.model.dto;

import lombok.Data;

import java.util.List;


@Data
public class ConvertRequest {
    private String text;
    private String mode = "auto";
    private Boolean showDetails = true;
    private Boolean enableFuzzy;
    private Boolean enableSingleSyllable;
}
