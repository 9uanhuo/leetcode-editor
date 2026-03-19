package com.shuzijun.leetcode.plugin.utils;

import com.github.promeg.pinyinhelper.Pinyin;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * Provide static tool class, StringUtils document reference <a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html">doc</a><br>
 * 提供的静态工具类，StringUtils文档参考<a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringUtils.html">doc</a>
 *
 * @author shuzijun
 */
public class VelocityTool extends StringUtils {

    private static String[] numsAry = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

    /**
     * Fill 0 on the left to reach a fixed length <br>
     * 在左侧填充0达到固定长度length
     *
     * @param s
     * @param length
     * @return
     */
    public static String leftPadZeros(String s, int length) {
        if (s.length() >= length) {
            return s;
        }
        int nPads = length - s.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nPads; ++i) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Adds a specified number of spaces as left padding to the beginning of each line in the input string.
     *
     * @param s      the input string to be padded; if null or empty, the method returns the input as is
     * @param length the number of spaces to pad at the beginning of each line; if less than or equal to 0, no padding is applied
     * @return a new string with the specified padding added to each line, or the original string if no padding was applied
     */
    public static String leftPadSpacesPerRow(String s, int length) {
        if (s == null || s.isEmpty() || length <= 0) {
            return s;
        }
        String padStr = " ".repeat(length);
        StringBuilder sb = new StringBuilder(s.length() + (length * 2));
        boolean lineStart = true;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (lineStart && ch != '\n' && ch != '\r') {
                sb.append(padStr);
                lineStart = false;
            }
            sb.append(ch);
            if (ch == '\n' || ch == '\r') {
                lineStart = true;
            }
        }
        return sb.toString();
    }

    /**
     * Convert characters to camel case (initial letter capitalized) <br>
     * 转换字符为驼峰样式（开头字母大写）
     *
     * @param underscoreName
     * @return
     */
    public static String camelCaseName(String underscoreName) {

        if (isNotBlank(underscoreName)) {
            underscoreName = underscoreName.replace(" ", "_");
            StringBuilder result = new StringBuilder();
            if (isNumeric(underscoreName.substring(0, 1))) {
                underscoreName = numsAry[Integer.valueOf(underscoreName.substring(0, 1))] + "-" + underscoreName.substring(1);
            }
            boolean first = true;
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ('_' == ch || '-' == ch) {
                    flag = true;
                } else {
                    if (flag || first) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                        first = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
            return result.toString();
        } else {
            return underscoreName;
        }
    }

    /**
     * Convert characters to camel case (lower case at the beginning) <br>
     * 转换字符为小驼峰样式（开头字母小写）
     *
     * @param underscoreName
     * @return
     */
    public static String smallCamelCaseName(String underscoreName) {

        if (isNotBlank(underscoreName)) {
            underscoreName = underscoreName.replace(" ", "_");
            StringBuilder result = new StringBuilder();
            if (isNumeric(underscoreName.substring(0, 1))) {
                underscoreName = numsAry[Integer.valueOf(underscoreName.substring(0, 1))] + "-" + underscoreName.substring(1);
            }
            boolean first = false;
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ('_' == ch || '-' == ch) {
                    flag = true;
                } else {
                    if (flag || first) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                        first = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
            return result.toString();
        } else {
            return underscoreName;
        }
    }

    /**
     * Convert characters to snake style <br>
     * 转换字符为蛇形样式
     *
     * @param underscoreName
     * @return
     */

    public static String snakeCaseName(String underscoreName) {

        if (isNotBlank(underscoreName)) {
            underscoreName = underscoreName.replace(" ", "_");
            StringBuilder result = new StringBuilder();
            for (int i = 0, j = underscoreName.length(); i < j; i++) {
                char ch = underscoreName.charAt(i);
                if ('_' == ch || '-' == ch) {
                    if (i + 1 < j) {
                        result.append("_").append(Character.toLowerCase(underscoreName.charAt(i + 1)));
                        i = i + 1;
                    }
                } else if (Character.isUpperCase(ch)) {
                    result.append("_").append(Character.toLowerCase(underscoreName.charAt(i)));
                } else {
                    result.append(ch);
                }
            }
            return result.toString();
        } else {
            return underscoreName;
        }
    }

    /**
     * Get the current time. <br>
     * 获取当前时间
     *
     * @return
     */
    public static String date() {
        return date("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Get the current time. <br>
     * 获取当前时间
     *
     * @return
     */
    public static String date(String format) {
        return DateFormatUtils.format(new Date(), format);
    }

    /**
     * Get start tag <br>
     * 获取开始标记
     *
     * @return
     */
    public static String SUBMIT_REGION_BEGIN() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        return codeTypeEnum.getComment() + Constant.SUBMIT_REGION_BEGIN;
    }

    /**
     * Get eng tag <br>
     * 获取结束标记
     *
     * @return
     */
    public static String SUBMIT_REGION_END() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        return codeTypeEnum.getComment() + Constant.SUBMIT_REGION_END;
    }

    /**
     * Convert Chinese characters to Pinyin and remove all spaces<br>
     * 将汉字转为为拼音并去除所有空格
     *
     * @param str
     * @return
     */
    public static String toPinyinAndTrims(String str) {
        if (isBlank(str)) {
            return "";
        }
        str = replace(str, " ", "");
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Pinyin.isChinese(c)) {
                String pinYin = Pinyin.toPinyin(c);
                sb.append(camelCaseName(pinYin.toLowerCase()));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
