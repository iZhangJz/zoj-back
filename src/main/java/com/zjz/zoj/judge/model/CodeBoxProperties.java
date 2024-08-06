package com.zjz.zoj.judge.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "codebox")
public class CodeBoxProperties {

    /**
     * CodeBox类型
     */
    private String type;
}
