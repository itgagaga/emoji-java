package com.emoji.converter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class JsonDataService {

    @Value("${emoji.data.path:data}")
    private String dataPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Object> runtimeIndex;
    private List<Map<String, Object>> emojiConcepts;

    @PostConstruct
    public void init() {
        try {
            loadRuntimeIndex();
            loadEmojiConcepts();
            log.info("✅ JSON 数据加载完成");
            log.info("   - 索引条目: {}", getWordIndex().size());
            log.info("   - Emoji 数量: {}", emojiConcepts.size());
        } catch (IOException e) {
            log.error("❌ 加载 JSON 数据失败", e);
            throw new RuntimeException("无法加载数据文件，请确认 data/ 目录存在且包含必要的 JSON 文件", e);
        }
    }

    public void loadRuntimeIndex() throws IOException {
        File file = new File(dataPath + File.separator + "runtime_index.json");
        if (!file.exists()) {
            throw new FileNotFoundException("找不到运行时索引文件: " + file.getAbsolutePath());
        }
        runtimeIndex = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});
    }

    public void loadEmojiConcepts() throws IOException {
        File file = new File(dataPath + File.separator + "emoji_concepts.json");
        if (!file.exists()) {
            throw new FileNotFoundException("找不到 Emoji 数据文件: " + file.getAbsolutePath());
        }
        emojiConcepts = objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
    }

    public Map<String, Object> getRuntimeIndex() {
        return runtimeIndex;
    }

    public List<Map<String, Object>> getEmojiConcepts() {
        return emojiConcepts;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> getWordIndex() {
        return (Map<String, List<Map<String, Object>>>) runtimeIndex.getOrDefault("word", Map.of());
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> getPinyinExactIndex() {
        return (Map<String, List<Map<String, Object>>>) runtimeIndex.getOrDefault("pinyin_exact", Map.of());
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> getPinyinSyllableIndex() {
        return (Map<String, List<Map<String, Object>>>) runtimeIndex.getOrDefault("pinyin_syllable", Map.of());
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> getPinyinFuzzyIndex() {
        return (Map<String, List<Map<String, Object>>>) runtimeIndex.getOrDefault("pinyin_fuzzy", Map.of());
    }

    public void saveToFile(Object data, String filename) throws IOException {
        File file = new File(dataPath + File.separator + filename);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        log.debug("已保存文件: {}", filename);
    }

    @SuppressWarnings("unchecked")
    public <T> T loadFromFile(String filename, TypeReference<T> typeRef) throws IOException {
        File file = new File(dataPath + File.separator + filename);
        if (!file.exists()) {
            return null;
        }
        return objectMapper.readValue(file, typeRef);
    }
}
