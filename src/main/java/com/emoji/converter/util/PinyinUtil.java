package com.emoji.converter.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Component
public class PinyinUtil {

    private final HanyuPinyinOutputFormat format;
    private final Pattern chinesePattern = Pattern.compile("[\\u4e00-\\u9fa5]");

    public PinyinUtil() {
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public List<String> toPinyin(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                try {
                    String[] py = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (py != null && py.length > 0) {
                        result.add(py[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination ignored) {
                }
            }
        }
        return result;
    }

    public String toPinyinString(String text) {
        return String.join(" ", toPinyin(text));
    }

    private boolean isChinese(char c) {
        return chinesePattern.matcher(String.valueOf(c)).matches();
    }
}
