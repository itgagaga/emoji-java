package com.emoji.converter.service;

import com.emoji.converter.model.OriginalOverride;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OriginalOverrideService {
    private static final String OVERRIDES_FILE = "data/original_overrides.json";
    private final Map<String, OriginalOverride> overrides = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final File dataFile;

    public OriginalOverrideService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.dataFile = new File(OVERRIDES_FILE);
    }

    @PostConstruct
    public void init() {
        loadOverrides();
        log.info("原始数据覆盖层已加载，共 {} 条覆盖记录", overrides.size());
    }

    private void loadOverrides() {
        if (!dataFile.exists()) {
            log.info("覆盖层文件不存在，将创建新文件");
            return;
        }

        try {
            List<OriginalOverride> overrideList = objectMapper.readValue(
                    dataFile,
                    new TypeReference<List<OriginalOverride>>() {}
            );

            overrides.clear();
            for (OriginalOverride override : overrideList) {
                String key = generateKey(override.getType(), override.getKey());
                overrides.put(key, override);
            }
        } catch (IOException e) {
            log.error("加载覆盖层数据失败", e);
        }
    }

    private synchronized void saveOverrides() {
        try {
            List<OriginalOverride> overrideList = overrides.values().stream()
                    .collect(Collectors.toList());
            
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, overrideList);
        } catch (IOException e) {
            log.error("保存覆盖层数据失败", e);
        }
    }

    private String generateKey(String type, String key) {
        return type + ":" + key.toLowerCase();
    }

    public OriginalOverride getOrCreateOverride(String type, String key) {
        String mapKey = generateKey(type, key);
        
        if (!overrides.containsKey(mapKey)) {
            OriginalOverride override = OriginalOverride.fromOriginalMapping(
                    "original_" + mapKey,
                    type,
                    key
            );
            overrides.put(mapKey, override);
            saveOverrides();
            log.info("创建新的覆盖记录: {} - {}", type, key);
        }
        
        return overrides.get(mapKey);
    }

    public OriginalOverride getOverride(String type, String key) {
        String mapKey = generateKey(type, key);
        return overrides.get(mapKey);
    }

    public void updatePriority(String type, String key, int priority) {
        OriginalOverride override = getOrCreateOverride(type, key);
        override.setPriority(priority);
        override.setUpdatedAt(java.time.LocalDateTime.now());
        saveOverrides();
        log.info("更新优先级: {} - {} -> {}", type, key, priority);
    }

    public void updateEnabled(String type, String key, boolean enabled) {
        OriginalOverride override = getOrCreateOverride(type, key);
        override.setEnabled(enabled);
        override.setUpdatedAt(java.time.LocalDateTime.now());
        saveOverrides();
        log.info("更新状态: {} - {} -> {}", type, key, enabled ? "启用" : "禁用");
    }

    public boolean isOverridden(String type, String key) {
        String mapKey = generateKey(type, key);
        return overrides.containsKey(mapKey) && overrides.get(mapKey).getEnabled() != null;
    }

    public Integer getOverriddenPriority(String type, String key) {
        OriginalOverride override = getOverride(type, key);
        return override != null ? override.getPriority() : null;
    }

    public Boolean getOverriddenEnabled(String type, String key) {
        OriginalOverride override = getOverride(type, key);
        return override != null ? override.getEnabled() : null;
    }

    public List<OriginalOverride> getAllOverrides() {
        return List.copyOf(overrides.values());
    }

    public void removeOverride(String type, String key) {
        String mapKey = generateKey(type, key);
        if (overrides.remove(mapKey) != null) {
            saveOverrides();
            log.info("移除覆盖记录: {} - {}", type, key);
        }
    }
}