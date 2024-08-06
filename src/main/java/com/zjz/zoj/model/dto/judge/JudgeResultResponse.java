package com.zjz.zoj.model.dto.judge;

import lombok.Builder;
import lombok.Data;

/**
 * 判题服务返回值
 */
@Data
@Builder
public class JudgeResultResponse {

    /**
     * 判题 Id
     */
    private Long Id;


    /**
     * 判题结果信息
     */
    private String message;

    /**
     * 出现错误的输入
     */
    private String errorInput;

    /**
     * 出现错误的输出
     */
    private String errorOutput;

    /**
     * 正确的输出
     */
    private String correctOutput;
}
