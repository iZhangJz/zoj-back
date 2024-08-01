package com.zjz.zoj.model.dto.question;

import lombok.Data;

/**
 * 判题结果信息
 */
@Data
public class QuestionJudgeInfo {

    /**
     * 执行信息
     */
    private String message;

    /**
     * 执行花费时间 ms
     */
    private Long time;

    /**
     * 执行使用内存大小 kb
     */
    private Long memory;

}
