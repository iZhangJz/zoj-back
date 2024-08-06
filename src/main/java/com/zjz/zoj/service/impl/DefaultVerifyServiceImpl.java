package com.zjz.zoj.service.impl;

import cn.hutool.json.JSONUtil;
import com.zjz.zoj.annotation.VerifyStrategyCheck;
import com.zjz.zoj.codebox.model.ExecuteInfo;
import com.zjz.zoj.codebox.model.ExecuteResponse;
import com.zjz.zoj.model.dto.judge.VerifyContext;
import com.zjz.zoj.model.dto.judge.JudgeResultResponse;
import com.zjz.zoj.model.dto.question.QuestionJudgeConfig;
import com.zjz.zoj.model.entity.Question;
import com.zjz.zoj.model.enums.JudgeInfoEnum;
import com.zjz.zoj.service.VerifyStrategyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@VerifyStrategyCheck(language = "java")
public class DefaultVerifyServiceImpl implements VerifyStrategyService {


    @Override
    public JudgeResultResponse doVerify(VerifyContext verifyContext) {
        ExecuteResponse executeResponse = verifyContext.getExecuteResponse();
        Question question = verifyContext.getQuestion();
        List<String> desiredOutputs = verifyContext.getDesiredOutputs();
        List<String> inputs = verifyContext.getInputs();


        // 代码执行成功 可以进行进一步判题
        List<String> outputs = executeResponse.getOutputs();
        List<ExecuteInfo> executeInfos = executeResponse.getExecuteInfos();

        if (outputs.size() != inputs.size()) {
            // 输入输出数量不一致
            return JudgeResultResponse.builder()
                    .message(JudgeInfoEnum.WRONG_ANSWER.getValue())
                    .errorOutput("实际结果输出数量: " + outputs.size())
                    .errorInput(null)
                    .correctOutput("预期结果输出数量: " + inputs.size())
                    .build();
        }
        String judgeConfig = question.getJudgeConfig();
        QuestionJudgeConfig config = JSONUtil.toBean(judgeConfig, QuestionJudgeConfig.class);
        for (int i = 0; i < executeInfos.size(); i++) {
            // 4.1 先查看当前测试用例是否成功执行并且有输出
            ExecuteInfo executeInfo = executeInfos.get(i);
            if (Objects.equals(executeInfo.getMessage(), "success")) {
                // 4.2 查看是否超时或者超内存
                Long executeTime = executeInfo.getTime();
                Long executeMemory = executeInfo.getMemory();
                if (executeTime > config.getTimeLimit()) {
                    // 执行超时
                    return JudgeResultResponse.builder()
                            .message(JudgeInfoEnum.TIME_LIMIT_EXCEEDED.getValue())
                            .errorOutput(outputs.get(i))
                            .errorInput(inputs.get(i))
                            .build();
                }
                if (executeMemory > config.getMemoryLimit()) {
                    // 执行超内存
                    return JudgeResultResponse.builder()
                            .message(JudgeInfoEnum.MEMORY_LIMIT_EXCEEDED.getValue())
                            .errorOutput(outputs.get(i))
                            .errorInput(inputs.get(i))
                            .build();
                }
                String output = outputs.get(i);
                String desiredOutput = desiredOutputs.get(i);
                if (!Objects.equals(output, desiredOutput)) {
                    // 实际的输出结果与预期的输出结果不一致
                    return JudgeResultResponse.builder()
                            .message(JudgeInfoEnum.WRONG_ANSWER.getValue())
                            .errorOutput(output)
                            .errorInput(inputs.get(i))
                            .correctOutput(desiredOutput)
                            .build();
                }
            }
        }
        // 没有发现错误
        return JudgeResultResponse.builder()
                .message(JudgeInfoEnum.ACCEPTED.getValue())
                .build();
    }
}
