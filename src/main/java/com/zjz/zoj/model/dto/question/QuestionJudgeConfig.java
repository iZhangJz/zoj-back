package com.zjz.zoj.model.dto.question;

import lombok.Data;

/**
 * 判题标准
 */
@Data
public class QuestionJudgeConfig {

    /**
     * 时间限制（时间复杂度） 单位 ms
     */
    private Long timeLimit;

    /**
     * 内存限制（空间复杂度）单位 kb
     */
    private Long memoryLimit;
}
