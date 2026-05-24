package com.emoji.converter.controller;

import com.emoji.converter.model.AdminMapping;
import com.emoji.converter.model.dto.AllMappingDTO;
import com.emoji.converter.model.OriginalOverride;
import com.emoji.converter.service.AdminMappingService;
import com.emoji.converter.service.JsonDataService;
import com.emoji.converter.service.EmojiConverterService;
import com.emoji.converter.service.OriginalOverrideService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/admin/mappings")
public class AdminMappingController {
    private static final Logger logger = LoggerFactory.getLogger(AdminMappingController.class);

    @Autowired
    private AdminMappingService adminMappingService;

    @Autowired
    private JsonDataService jsonDataService;

    @Autowired
    private EmojiConverterService emojiConverterService;

    @Autowired
    private OriginalOverrideService originalOverrideService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMappings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String key) {
        
        List<AdminMapping> mappings;
        
        if (key != null && !key.isEmpty()) {
            mappings = adminMappingService.getMappingsByKey(key);
        } else if (keyword != null && !keyword.isEmpty()) {
            mappings = adminMappingService.searchMappings(keyword);
        } else if (type != null && !type.isEmpty()) {
            mappings = adminMappingService.getAllMappings().stream()
                .filter(m -> m.getType().equals(type))
                .toList();
        } else {
            mappings = adminMappingService.getAllMappings();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("mappings", mappings);
        response.put("total", mappings.size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMapping(@PathVariable String id) {
        Optional<AdminMapping> mapping = adminMappingService.getMapping(id);
        
        if (mapping.isPresent()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("mapping", mapping.get());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "映射不存在");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addMapping(@RequestBody AdminMapping mapping) {
        try {
            if (mapping.getKey() == null || mapping.getKey().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Key不能为空"
                ));
            }
            
            if (mapping.getEmoji() == null || mapping.getEmoji().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Emoji不能为空"
                ));
            }
            
            if (mapping.getType() == null || mapping.getType().trim().isEmpty()) {
                mapping.setType("pinyin");
            }
            
            AdminMapping saved = adminMappingService.addMapping(mapping);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "映射添加成功");
            response.put("mapping", saved);
            
            logger.info("✅ 新增映射: {} -> {}", saved.getKey(), saved.getEmoji());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ 添加映射失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "添加失败: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<Map<String, Object>> updatePriority(
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        
        Integer priority = body.get("priority");
        if (priority == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "优先级不能为空"
            ));
        }
        
        AdminMapping updated = adminMappingService.updateMappingPriority(id, priority);
        if (updated != null) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "优先级更新成功");
            response.put("mapping", updated);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleMapping(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "enabled参数不能为空"
            ));
        }
        
        boolean success = adminMappingService.toggleMapping(id, enabled);
        if (success) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", enabled ? "已启用" : "已禁用");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/original/{type}/{key}/priority")
    public ResponseEntity<Map<String, Object>> updateOriginalPriority(
            @PathVariable String type,
            @PathVariable String key,
            @RequestBody Map<String, Integer> body) {
        
        Integer priority = body.get("priority");
        if (priority == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "优先级不能为空"
            ));
        }

        originalOverrideService.updatePriority(type, key, priority);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "原始数据优先级更新成功");
        response.put("type", type);
        response.put("key", key);
        response.put("priority", priority);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/original/{type}/{key}/toggle")
    public ResponseEntity<Map<String, Object>> toggleOriginalMapping(
            @PathVariable String type,
            @PathVariable String key,
            @RequestBody Map<String, Boolean> body) {
        
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "enabled参数不能为空"
            ));
        }

        originalOverrideService.updateEnabled(type, key, enabled);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", enabled ? "原始数据已启用" : "原始数据已禁用");
        response.put("type", type);
        response.put("key", key);
        response.put("enabled", enabled);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/original/overrides")
    public ResponseEntity<Map<String, Object>> getAllOriginalOverrides() {
        List<OriginalOverride> overrides = originalOverrideService.getAllOverrides();
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("overrides", overrides);
        response.put("total", overrides.size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMapping(@PathVariable String id) {
        boolean success = adminMappingService.deleteMapping(id);
        if (success) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = adminMappingService.getStats();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("stats", stats);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMappings(@RequestParam String keyword) {
        List<AdminMapping> results = adminMappingService.searchMappings(keyword);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("results", results);
        response.put("total", results.size());
        response.put("keyword", keyword);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMappingsCombined(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        List<AllMappingDTO> allMappings = new java.util.ArrayList<>();

        if (source == null || source.isEmpty() || "original".equals(source)) {
            addOriginalMappings(allMappings, type, keyword);
        }
        
        if (source == null || source.isEmpty() || "admin".equals(source)) {
            addAdminMappings(allMappings, type, keyword);
        }

        allMappings.sort((a, b) -> {
            int priorityCompare = Integer.compare(b.getPriority(), a.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return a.getKey().compareTo(b.getKey());
        });

        int total = allMappings.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        
        List<AllMappingDTO> pagedMappings;
        if (fromIndex < total) {
            pagedMappings = allMappings.subList(fromIndex, toIndex);
        } else {
            pagedMappings = new java.util.ArrayList<>();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("mappings", pagedMappings);
        response.put("pagination", Map.of(
            "page", page,
            "size", size,
            "totalElements", total,
            "totalPages", totalPages,
            "hasNext", page < totalPages,
            "hasPrev", page > 1
        ));

        long originalCount = allMappings.stream().filter(m -> "original".equals(m.getSource())).count();
        long adminCount = allMappings.stream().filter(m -> "admin".equals(m.getSource())).count();
        response.put("stats", Map.of(
            "originalCount", originalCount,
            "adminCount", adminCount
        ));

        return ResponseEntity.ok(response);
    }

    private void addOriginalMappings(List<AllMappingDTO> allMappings, String typeFilter, String keyword) {
        try {
            var wordIndex = jsonDataService.getWordIndex();
            
            for (var entry : wordIndex.entrySet()) {
                String key = entry.getKey();
                List<Map<String, Object>> candidates = entry.getValue();
                
                for (Map<String, Object> cand : candidates) {
                    String emoji = (String) cand.get("emoji");
                    String indexType = (String) cand.getOrDefault("index_type", "word");
                    int priority = ((Number) cand.getOrDefault("priority", 0)).intValue();
                    
                    if (typeFilter != null && !typeFilter.isEmpty() && !indexType.contains(typeFilter)) {
                        continue;
                    }
                    
                    if (keyword != null && !keyword.isEmpty()) {
                        String searchStr = (key + emoji).toLowerCase();
                        if (!searchStr.contains(keyword.toLowerCase())) {
                            continue;
                        }
                    }
                    
                    AllMappingDTO dto = new AllMappingDTO();
                    dto.setId("orig-" + key + "-" + emoji.hashCode());
                    dto.setType(indexType.startsWith("pinyin") ? "pinyin" : "word");
                    dto.setKey(key);
                    dto.setEmoji(emoji);
                    dto.setDescription((String) cand.getOrDefault("description", ""));
                    
                    // 应用覆盖层
                    Integer overriddenPriority = originalOverrideService.getOverriddenPriority(dto.getType(), key);
                    dto.setPriority(overriddenPriority != null ? overriddenPriority : priority);
                    
                    Boolean overriddenEnabled = originalOverrideService.getOverriddenEnabled(dto.getType(), key);
                    dto.setEnabled(overriddenEnabled != null ? overriddenEnabled : true);
                    
                    dto.setSource("original");
                    dto.setIndexType(indexType);
                    
                    allMappings.add(dto);
                }
            }

            var pinyinExactIndex = jsonDataService.getPinyinExactIndex();
            for (var entry : pinyinExactIndex.entrySet()) {
                String key = entry.getKey();
                List<Map<String, Object>> candidates = entry.getValue();
                
                for (Map<String, Object> cand : candidates) {
                    String emoji = (String) cand.get("emoji");
                    int priority = ((Number) cand.getOrDefault("priority", 0)).intValue();
                    
                    if (typeFilter != null && !typeFilter.isEmpty() && !"pinyin".equals(typeFilter)) {
                        continue;
                    }
                    
                    if (keyword != null && !keyword.isEmpty()) {
                        String searchStr = (key + emoji).toLowerCase();
                        if (!searchStr.contains(keyword.toLowerCase())) {
                            continue;
                        }
                    }
                    
                    AllMappingDTO dto = new AllMappingDTO();
                    dto.setId("orig-py-exact-" + key + "-" + emoji.hashCode());
                    dto.setType("pinyin");
                    dto.setKey(key);
                    dto.setEmoji(emoji);
                    dto.setDescription("");
                    
                    // 应用覆盖层
                    Integer overriddenPriority = originalOverrideService.getOverriddenPriority("pinyin", key);
                    dto.setPriority(overriddenPriority != null ? overriddenPriority : priority);
                    
                    Boolean overriddenEnabled = originalOverrideService.getOverriddenEnabled("pinyin", key);
                    dto.setEnabled(overriddenEnabled != null ? overriddenEnabled : true);
                    
                    dto.setSource("original");
                    dto.setIndexType("pinyin_exact");
                    
                    allMappings.add(dto);
                }
            }

            var pinyinSyllableIndex = jsonDataService.getPinyinSyllableIndex();
            for (var entry : pinyinSyllableIndex.entrySet()) {
                String key = entry.getKey();
                List<Map<String, Object>> candidates = entry.getValue();
                
                for (Map<String, Object> cand : candidates) {
                    String emoji = (String) cand.get("emoji");
                    int priority = ((Number) cand.getOrDefault("priority", 0)).intValue();
                    
                    if (typeFilter != null && !typeFilter.isEmpty() && !"pinyin".equals(typeFilter)) {
                        continue;
                    }
                    
                    if (keyword != null && !keyword.isEmpty()) {
                        String searchStr = (key + emoji).toLowerCase();
                        if (!searchStr.contains(keyword.toLowerCase())) {
                            continue;
                        }
                    }
                    
                    AllMappingDTO dto = new AllMappingDTO();
                    dto.setId("orig-py-syllable-" + key + "-" + emoji.hashCode());
                    dto.setType("pinyin");
                    dto.setKey(key);
                    dto.setEmoji(emoji);
                    dto.setDescription("");
                    
                    // 应用覆盖层
                    Integer overriddenPriority = originalOverrideService.getOverriddenPriority("pinyin", key);
                    dto.setPriority(overriddenPriority != null ? overriddenPriority : priority);
                    
                    Boolean overriddenEnabled = originalOverrideService.getOverriddenEnabled("pinyin", key);
                    dto.setEnabled(overriddenEnabled != null ? overriddenEnabled : true);
                    
                    dto.setSource("original");
                    dto.setIndexType("pinyin_syllable");
                    
                    allMappings.add(dto);
                }
            }
        } catch (Exception e) {
            logger.error("加载原始映射数据失败", e);
        }
    }

    private void addAdminMappings(List<AllMappingDTO> allMappings, String typeFilter, String keyword) {
        List<AdminMapping> adminMappings = adminMappingService.getAllMappings();
        
        for (AdminMapping mapping : adminMappings) {
            if (typeFilter != null && !typeFilter.isEmpty() && !mapping.getType().equals(typeFilter)) {
                continue;
            }
            
            if (keyword != null && !keyword.isEmpty()) {
                String searchStr = (mapping.getKey() + mapping.getEmoji()).toLowerCase();
                if (!searchStr.contains(keyword.toLowerCase())) {
                    continue;
                }
            }
            
            AllMappingDTO dto = new AllMappingDTO();
            dto.setId(mapping.getId());
            dto.setType(mapping.getType());
            dto.setKey(mapping.getKey());
            dto.setEmoji(mapping.getEmoji());
            dto.setDescription(mapping.getDescription());
            dto.setPriority(mapping.getPriority());
            dto.setSource("admin");
            dto.setIndexType(determineIndexType(mapping));
            dto.setEnabled(mapping.isEnabled());
            
            allMappings.add(dto);
        }
    }

    private String determineIndexType(AdminMapping mapping) {
        if ("word".equals(mapping.getType())) {
            return "word";
        } else {
            String key = mapping.getKey();
            if (key != null && key.contains(" ")) {
                return "pinyin_exact";
            } else {
                return "pinyin_syllable";
            }
        }
    }
}