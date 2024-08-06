package com.zjz.zoj.judge.model;

import com.zjz.zoj.model.dto.question.QuestionJudgeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteResponse {

    /**
     * 执行结果数组
     */
    private List<String> outputs;

    /**
     * 各个测试用例执行结果
     */
    private List<QuestionJudgeInfo> judgeInfos;

    /**
     * 接口信息
     */
    private String message;



}
