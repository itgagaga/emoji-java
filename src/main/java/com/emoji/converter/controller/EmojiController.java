package com.emoji.converter.controller;

import com.emoji.converter.model.dto.CandidateDTO;
import com.emoji.converter.model.dto.ConvertRequest;
import com.emoji.converter.model.dto.ConvertResponse;
import com.emoji.converter.service.EmojiConverterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmojiController {

    private final EmojiConverterService emojiConverterService;

    @PostMapping("/convert")
    public ResponseEntity<ConvertResponse> convert(@RequestBody ConvertRequest request) {
        ConvertResponse response = emojiConverterService.convert(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/lookup-candidates")
    public ResponseEntity<Map<String, Object>> lookupCandidates(@RequestBody Map<String, String> requestBody) {
        String text = requestBody.get("text");

        List<CandidateDTO> candidates = emojiConverterService.lookupAllCandidates(text);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("candidates", candidates);
        response.put("total_count", candidates.size());
        response.put("error", "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> matcher = new HashMap<>();
        matcher.put("max_window", 4);
        matcher.put("max_candidates_per_key", 80);
        matcher.put("enable_fuzzy", true);
        matcher.put("enable_single_syllable", true);

        Map<String, Object> output = new HashMap<>();
        output.put("mode", "auto");
        output.put("show_details", true);

        config.put("matcher", matcher);
        config.put("output", output);

        return ResponseEntity.ok(config);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = emojiConverterService.getIndexStats();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("index_info", stats);

        return ResponseEntity.ok(response);
    }
}
