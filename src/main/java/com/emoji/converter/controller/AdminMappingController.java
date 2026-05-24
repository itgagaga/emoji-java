package com.emoji.converter.controller;

import com.emoji.converter.model.AdminMapping;
import com.emoji.converter.service.AdminMappingService;
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
}