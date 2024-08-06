package com.zjz.zoj.codebox.enums;

import org.apache.commons.lang3.ObjectUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CodeBoxTypeEnum {

    SAMPLE("示例沙箱", "sample"),
    REMOTE("远程沙箱", "remote"),
    THIRD_PARTY("第三方", "third");

    private final String text;

    private final String value;

    CodeBoxTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     */
    public static CodeBoxTypeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (CodeBoxTypeEnum anEnum : CodeBoxTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
