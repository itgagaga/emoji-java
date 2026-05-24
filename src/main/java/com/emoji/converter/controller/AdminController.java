package com.emoji.converter.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/api/custom")
public class AdminController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CUSTOM_DIR = "custom";

    // ==================== 优先级配置 ====================

    @GetMapping("/priorities")
    public ResponseEntity<List<Map<String, Object>>> getPriorities() {
        try {
            List<Map<String, Object>> data = loadJsonList(CUSTOM_DIR + "/priorities/priorities.json");
            return ResponseEntity.ok(data != null ? data : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取优先级配置失败", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/priorities")
    public ResponseEntity<Map<String, Object>> addPriority(@RequestBody Map<String, Object> item) {
        try {
            List<Map<String, Object>> data = loadOrCreateJsonList(CUSTOM_DIR + "/priorities/priorities.json");
            data.add(item);
            saveJsonList(CUSTOM_DIR + "/priorities/priorities.json", data);

            return okResponse("优先级规则已添加");
        } catch (Exception e) {
            log.error("添加优先级规则失败", e);
            return badRequestError("添加失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/priorities/{index}")
    public ResponseEntity<Map<String, Object>> deletePriority(@PathVariable int index) {
        try {
            List<Map<String, Object>> data = loadJsonList(CUSTOM_DIR + "/priorities/priorities.json");

            if (data == null || index < 0 || index >= data.size()) {
                return notFoundError("规则不存在");
            }

            data.remove(index);
            saveJsonList(CUSTOM_DIR + "/priorities/priorities.json", data);

            return okResponse("已删除");
        } catch (Exception e) {
            log.error("删除优先级规则失败", e);
            return badRequestError("删除失败: " + e.getMessage());
        }
    }

    // ==================== 自定义 Emoji ====================

    @GetMapping("/emojis")
    public ResponseEntity<List<Map<String, Object>>> getCustomEmojis() {
        try {
            List<Map<String, Object>> data = loadJsonList(CUSTOM_DIR + "/mappings/emojis.json");
            return ResponseEntity.ok(data != null ? data : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取自定义Emoji列表失败", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/emojis")
    public ResponseEntity<Map<String, Object>> addCustomEmoji(@RequestBody Map<String, Object> item) {
        try {
            List<Map<String, Object>> data = loadOrCreateJsonList(CUSTOM_DIR + "/mappings/emojis.json");
            data.add(item);
            saveJsonList(CUSTOM_DIR + "/mappings/emojis.json", data);

            return okResponse("Emoji已添加");
        } catch (Exception e) {
            log.error("添加自定义Emoji失败", e);
            return badRequestError("添加失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/emojis/{index}")
    public ResponseEntity<Map<String, Object>> deleteCustomEmoji(@PathVariable int index) {
        try {
            List<Map<String, Object>> data = loadJsonList(CUSTOM_DIR + "/mappings/emojis.json");

            if (data == null || index < 0 || index >= data.size()) {
                return notFoundError("Emoji不存在");
            }

            data.remove(index);
            saveJsonList(CUSTOM_DIR + "/mappings/emojis.json", data);

            return okResponse("已删除");
        } catch (Exception e) {
            log.error("删除自定义Emoji失败", e);
            return badRequestError("删除失败: " + e.getMessage());
        }
    }

    // ==================== 词语映射 ====================

    @GetMapping("/words")
    public ResponseEntity<Map<String, Object>> getWordMappings() {
        try {
            Path path = Paths.get(CUSTOM_DIR + "/mappings/words.json");
            if (Files.exists(path)) {
                String content = Files.readString(path);
                Map<String, Object> data = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
                return ResponseEntity.ok(data != null ? data : new HashMap<>());
            }
            return ResponseEntity.ok(new HashMap<>());
        } catch (Exception e) {
            log.error("获取词语映射失败", e);
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    @PostMapping("/words")
    public ResponseEntity<Map<String, Object>> addWordMapping(@RequestBody Map<String, Object> item) {
        try {
            String word = (String) item.get("word");
            if (word == null || word.isEmpty()) {
                return badRequestError("缺少word字段");
            }

            Path path = Paths.get(CUSTOM_DIR + "/mappings/words.json");
            Map<String, Object> data;

            if (Files.exists(path)) {
                String content = Files.readString(path);
                data = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            } else {
                data = new HashMap<>();
            }

            Map<String, Object> wordData = new HashMap<>();
            wordData.put("emojis", item.getOrDefault("emojis", new ArrayList<>()));
            wordData.put("category", item.getOrDefault("category", ""));
            wordData.put("description", item.getOrDefault("description", ""));
            data.put(word, wordData);

            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);

            return okResponse("词语\"" + word + "\"的映射已添加");
        } catch (Exception e) {
            log.error("添加词语映射失败", e);
            return badRequestError("添加失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/words/{word}")
    public ResponseEntity<Map<String, Object>> deleteWordMapping(@PathVariable String word) {
        try {
            Path path = Paths.get(CUSTOM_DIR + "/mappings/words.json");

            if (!Files.exists(path)) {
                return notFoundError("词语不存在");
            }

            String content = Files.readString(path);
            Map<String, Object> data = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});

            if (!data.containsKey(word)) {
                return notFoundError("词语不存在");
            }

            data.remove(word);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);

            return okResponse("已删除\"" + word + "\"的映射");
        } catch (Exception e) {
            log.error("删除词语映射失败", e);
            return badRequestError("删除失败: " + e.getMessage());
        }
    }

    // ==================== 别名配置 ====================

    @GetMapping("/aliases")
    public ResponseEntity<Map<String, Object>> getAliases() {
        try {
            Path path = Paths.get(CUSTOM_DIR + "/aliases/aliases.json");
            if (Files.exists(path)) {
                String content = Files.readString(path);
                Map<String, Object> data = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
                return ResponseEntity.ok(data != null ? data : new HashMap<>());
            }
            return ResponseEntity.ok(new HashMap<>());
        } catch (Exception e) {
            log.error("获取别名配置失败", e);
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    @PostMapping("/aliases")
    public ResponseEntity<Map<String, Object>> addAlias(@RequestBody Map<String, Object> item) {
        try {
            String emoji = (String) item.get("emoji");
            List<String> newAliases = (List<String>) item.get("aliases");

            if (emoji == null || emoji.isEmpty() || newAliases == null || newAliases.isEmpty()) {
                return badRequestError("缺少emoji或aliases字段");
            }

            Path path = Paths.get(CUSTOM_DIR + "/aliases/aliases.json");
            Map<String, Object> data;

            if (Files.exists(path)) {
                String content = Files.readString(path);
                data = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            } else {
                data = new HashMap<>();
            }

            if (!data.containsKey(emoji)) {
                Map<String, Object> emojiData = new HashMap<>();
                emojiData.put("aliases", new ArrayList<>());
                emojiData.put("category", item.getOrDefault("category", ""));
                data.put(emoji, emojiData);
            }

            Map<String, Object> emojiData = (Map<String, Object>) data.get(emoji);
            List<String> existingAliases = (List<String>) emojiData.get("aliases");
            Set<String> aliasSet = new HashSet<>(existingAliases);

            for (String alias : newAliases) {
                if (!aliasSet.contains(alias)) {
                    existingAliases.add(alias);
                    aliasSet.add(alias);
                }
            }

            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);

            return okResponse("为" + emoji + "添加了" + newAliases.size() + "个别名");
        } catch (Exception e) {
            log.error("添加别名失败", e);
            return badRequestError("添加失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/aliases/{emoji}")
    public ResponseEntity<Map<String, Object>> deleteAlias(@PathVariable String emoji) {
        try {
            Path path = Paths.get(CUSTOM_DIR + "/aliases/aliases.json");

            if (!Files.exists(path)) {
                return notFoundError("Emoji不存在");
            }

            String content = Files.readString(path);
            Map<String, Object> data = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});

            if (!data.containsKey(emoji)) {
                return notFoundError("Emoji不存在");
            }

            data.remove(emoji);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);

            return okResponse("已删除" + emoji + "的所有别名");
        } catch (Exception e) {
            log.error("删除别名失败", e);
            return badRequestError("删除失败: " + e.getMessage());
        }
    }

    // ==================== 统计信息 ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCustomStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Map<String, Object>> priorities = loadJsonList(CUSTOM_DIR + "/priorities/priorities.json");
            stats.put("priorities", priorities != null ? priorities.size() : 0);
        } catch (Exception e) {
            stats.put("priorities", 0);
        }

        try {
            List<Map<String, Object>> emojis = loadJsonList(CUSTOM_DIR + "/mappings/emojis.json");
            stats.put("custom_emojis", emojis != null ? emojis.size() : 0);
        } catch (Exception e) {
            stats.put("custom_emojis", 0);
        }

        try {
            Path wordsPath = Paths.get(CUSTOM_DIR + "/mappings/words.json");
            if (Files.exists(wordsPath)) {
                String content = Files.readString(wordsPath);
                Map<String, Object> words = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
                stats.put("word_mappings", words != null ? words.size() : 0);
            } else {
                stats.put("word_mappings", 0);
            }
        } catch (Exception e) {
            stats.put("word_mappings", 0);
        }

        try {
            Path aliasesPath = Paths.get(CUSTOM_DIR + "/aliases/aliases.json");
            if (Files.exists(aliasesPath)) {
                String content = Files.readString(aliasesPath);
                Map<String, Object> aliases = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
                int totalAliases = 0;
                for (Object value : aliases.values()) {
                    if (value instanceof Map) {
                        Map<String, Object> aliasData = (Map<String, Object>) value;
                        List<String> aliasList = (List<String>) aliasData.get("aliases");
                        totalAliases += aliasList != null ? aliasList.size() : 0;
                    }
                }
                stats.put("alias_emojis", aliases != null ? aliases.size() : 0);
                stats.put("total_aliases", totalAliases);
            } else {
                stats.put("alias_emojis", 0);
                stats.put("total_aliases", 0);
            }
        } catch (Exception e) {
            stats.put("alias_emojis", 0);
            stats.put("total_aliases", 0);
        }

        return ResponseEntity.ok(stats);
    }

    // ==================== 工具方法 ====================

    private List<Map<String, Object>> loadJsonList(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        return objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
    }

    private List<Map<String, Object>> loadOrCreateJsonList(String filePath) throws IOException {
        List<Map<String, Object>> data = loadJsonList(filePath);
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    private void saveJsonList(String filePath, List<Map<String, Object>> data) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
    }

    private ResponseEntity<Map<String, Object>> okResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> badRequestError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<Map<String, Object>> notFoundError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
