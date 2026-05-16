package com.carbon.platform.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class SensitiveWordService {

    private Map<Character, Object> wordMap;
    private static final String REPLACEMENT = "***";

    // 默认敏感词库
    private static final String[] DEFAULT_WORDS = {
        // 政治敏感
        "法轮功", "政治颠覆", "分裂国家", "暴恐",
        // 色情低俗
        "色情", "赌博", "吸毒", "嫖娼", "卖淫",
        // 侮辱谩骂
        "傻逼", "草泥马", "卧槽", "他妈的", "你妈", "狗日", "王八蛋", "混蛋",
        // 诈骗广告
        "代开发票", "办证", "贷款套现", "刷单", "兼职日结",
        // 违禁品
        "枪支", "弹药", "毒品", "假币", "窃听器",
        // 虚假宣传
        "包过", "保赢", "稳赚不赔", "零风险",
        // 其他不当内容
        "自杀", "自残", "传销", "邪教"
    };

    @PostConstruct
    public void init() {
        wordMap = new HashMap<>();
        for (String word : DEFAULT_WORDS) {
            addWord(word);
        }
    }

    /**
     * 添加敏感词到词库
     */
    public void addWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        Map<Character, Object> currentMap = wordMap;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object node = currentMap.get(c);
            if (node == null) {
                currentMap.put(c, new HashMap<Character, Object>());
                node = currentMap.get(c);
            }
            currentMap = (Map<Character, Object>) node;
        }
        currentMap.put('\0', null); // 结束标记
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (checkSensitiveWord(text, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文本中的所有敏感词
     */
    public Set<String> getSensitiveWords(String text) {
        Set<String> words = new HashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return words;
        }
        for (int i = 0; i < text.length(); i++) {
            int length = getSensitiveWordLength(text, i);
            if (length > 0) {
                words.add(text.substring(i, i + length));
            }
        }
        return words;
    }

    /**
     * 替换文本中的敏感词
     */
    public String replaceSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text);
        int i = 0;
        while (i < result.length()) {
            int length = getSensitiveWordLength(result.toString(), i);
            if (length > 0) {
                result.replace(i, i + length, REPLACEMENT);
                i += REPLACEMENT.length();
            } else {
                i++;
            }
        }
        return result.toString();
    }

    private boolean checkSensitiveWord(String text, int startIndex) {
        Map<Character, Object> currentMap = wordMap;
        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            Object node = currentMap.get(c);
            if (node == null) {
                return false;
            }
            currentMap = (Map<Character, Object>) node;
            if (currentMap.containsKey('\0')) {
                return true;
            }
        }
        return false;
    }

    private int getSensitiveWordLength(String text, int startIndex) {
        Map<Character, Object> currentMap = wordMap;
        int length = 0;
        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            Object node = currentMap.get(c);
            if (node == null) {
                return length;
            }
            currentMap = (Map<Character, Object>) node;
            length++;
            if (currentMap.containsKey('\0')) {
                return length;
            }
        }
        return 0;
    }

    /**
     * 批量添加敏感词
     */
    public void addWords(List<String> words) {
        for (String word : words) {
            addWord(word);
        }
    }

    /**
     * 获取当前敏感词数量
     */
    public int getWordCount() {
        return countWords(wordMap);
    }

    private int countWords(Map<Character, Object> map) {
        int count = 0;
        for (Object value : map.values()) {
            if (value instanceof Map) {
                count += countWords((Map<Character, Object>) value);
            } else {
                count++;
            }
        }
        return count;
    }
}
