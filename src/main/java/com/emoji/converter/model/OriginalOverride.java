package com.emoji.converter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OriginalOverride {
    private String id;
    private String originalId;
    private String type;
    private String key;
    private Integer priority;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OriginalOverride fromOriginalMapping(String originalId, String type, String key) {
        return new OriginalOverride(
                "ovr_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000000000),
                originalId,
                type,
                key,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}