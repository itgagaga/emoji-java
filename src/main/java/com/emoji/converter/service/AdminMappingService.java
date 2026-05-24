package com.emoji.converter.service;

import com.emoji.converter.model.AdminMapping;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AdminMappingService {
    private static final Logger logger = LoggerFactory.getLogger(AdminMappingService.class);

    @Value("${data.path:./data}")
    private String dataPath;

    private final Map<String, AdminMapping> mappings = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private File mappingFile;

    public AdminMappingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        try {
            File dataDir = new File(dataPath);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            mappingFile = new File(dataDir, "admin_mappings.json");
            loadMappings();
            logger.info("✅ 管理员映射服务初始化完成，共加载 {} 条映射", mappings.size());
        } catch (Exception e) {
            logger.error("❌ 管理员映射服务初始化失败", e);
        }
    }

    private void loadMappings() {
        if (mappingFile.exists()) {
            try {
                List<AdminMapping> list = objectMapper.readValue(mappingFile,
                    new TypeReference<List<AdminMapping>>() {});
                mappings.clear();
                for (AdminMapping mapping : list) {
                    mappings.put(mapping.getId(), mapping);
                }
                logger.info("📂 从文件加载了 {} 条管理员映射", mappings.size());
            } catch (IOException e) {
                logger.error("❌ 读取管理员映射文件失败", e);
            }
        } else {
            logger.info("📝 管理员映射文件不存在，将创建新文件");
        }
    }

    private synchronized void saveMappings() {
        try {
            List<AdminMapping> list = new ArrayList<>(mappings.values());
            objectMapper.writeValue(mappingFile, list);
            logger.debug("💾 已保存 {} 条管理员映射到文件", list.size());
        } catch (IOException e) {
            logger.error("❌ 保存管理员映射文件失败", e);
        }
    }

    public AdminMapping addMapping(AdminMapping mapping) {
        if (mapping.getId() == null || mapping.getId().isEmpty()) {
            mapping.setId("adm_" + System.currentTimeMillis() + "_" + mapping.getKey().hashCode());
        }
        mapping.setKey(mapping.getKey().toLowerCase());
        mapping.setUpdatedAt(java.time.LocalDateTime.now());
        
        mappings.put(mapping.getId(), mapping);
        saveMappings();
        logger.info("➕ 新增管理员映射: {} -> {} [优先级:{}]", mapping.getKey(), mapping.getEmoji(), mapping.getPriority());
        return mapping;
    }

    public Optional<AdminMapping> getMapping(String id) {
        return Optional.ofNullable(mappings.get(id));
    }

    public List<AdminMapping> getAllMappings() {
        return new ArrayList<>(mappings.values())
            .stream()
            .sorted(Comparator.comparingInt(AdminMapping::getPriority).reversed())
            .collect(Collectors.toList());
    }

    public List<AdminMapping> getMappingsByKey(String key) {
        String keyLower = key.toLowerCase();
        return mappings.values().stream()
            .filter(m -> m.getKey().equals(keyLower))
            .sorted(Comparator.comparingInt(AdminMapping::getPriority).reversed())
            .collect(Collectors.toList());
    }

    public List<AdminMapping> getEnabledMappingsByKey(String key) {
        String keyLower = key.toLowerCase();
        return mappings.values().stream()
            .filter(m -> m.isEnabled() && m.getKey().equals(keyLower))
            .sorted(Comparator.comparingInt(AdminMapping::getPriority).reversed())
            .collect(Collectors.toList());
    }

    public List<AdminMapping> getEnabledMappings() {
        return mappings.values().stream()
            .filter(AdminMapping::isEnabled)
            .sorted(Comparator.comparingInt(AdminMapping::getPriority).reversed())
            .collect(Collectors.toList());
    }

    public List<AdminMapping> searchMappings(String keyword) {
        String keywordLower = keyword.toLowerCase();
        return mappings.values().stream()
            .filter(m -> 
                m.getKey().contains(keywordLower) ||
                m.getEmoji().contains(keywordLower) ||
                (m.getDescription() != null && m.getDescription().contains(keywordLower)) ||
                m.getType().contains(keywordLower))
            .sorted(Comparator.comparingInt(AdminMapping::getPriority).reversed())
            .collect(Collectors.toList());
    }

    public long getTotalCount() {
        return mappings.size();
    }

    public long getEnabledCount() {
        return mappings.values().stream().filter(AdminMapping::isEnabled).count();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", mappings.size());
        stats.put("enabled", getEnabledCount());
        stats.put("disabled", mappings.size() - getEnabledCount());
        
        Map<String, Long> typeStats = mappings.values().stream()
            .collect(Collectors.groupingBy(AdminMapping::getType, Collectors.counting()));
        stats.put("byType", typeStats);
        
        return stats;
    }

    public boolean toggleMapping(String id, boolean enabled) {
        AdminMapping mapping = mappings.get(id);
        if (mapping != null) {
            mapping.setEnabled(enabled);
            saveMappings();
            logger.info("{} 映射 {}: {}", enabled ? "✅ 启用" : "⛔ 禁用", id, mapping.getKey() + "->" + mapping.getEmoji());
            return true;
        }
        return false;
    }

    public AdminMapping updateMappingPriority(String id, int priority) {
        AdminMapping mapping = mappings.get(id);
        if (mapping != null) {
            mapping.setPriority(priority);
            saveMappings();
            logger.info("📊 更新映射 {} 优先级为: {}", id, priority);
            return mapping;
        }
        return null;
    }

    public boolean deleteMapping(String id) {
        AdminMapping removed = mappings.remove(id);
        if (removed != null) {
            saveMappings();
            logger.info("🗑️ 删除管理员映射: {} -> {}", removed.getKey(), removed.getEmoji());
            return true;
        }
        return false;
    }
}