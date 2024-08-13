package com.zjz.zoj.codebox.model;

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

    /**
     * 远程代码沙箱地址
     */
    private String remoteUrl;

    /**
     * 权限请求头
     */
    private String authHeader;

    /**
     * 密钥
     */
    private String secretKey;
}
