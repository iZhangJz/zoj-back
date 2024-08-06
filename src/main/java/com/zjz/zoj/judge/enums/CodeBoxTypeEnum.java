package com.zjz.zoj.judge.enums;

import org.apache.commons.lang3.ObjectUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CodeBoxTypeEnum {

    SAMPLE("sample", "sample"),
    REMOTE("remote", "remote"),
    THIRD_PARTY("third_party", "third");

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
