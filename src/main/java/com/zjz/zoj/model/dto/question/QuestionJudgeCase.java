package com.zjz.zoj.model.dto.question;

import lombok.Data;

/**
 * 题目用例
 */
@Data
public class QuestionJudgeCase {

    /**
     * 输入用例
     */
    private String input;

    /**
     * 输出用例
     */
    private String output;
}