package com.zjz.zoj.service.impl;

import cn.hutool.json.JSONUtil;
import com.zjz.zoj.codebox.enums.CodeBoxExecuteEnum;
import com.zjz.zoj.common.ErrorCode;
import com.zjz.zoj.exception.BusinessException;
import com.zjz.zoj.codebox.CodeBox;
import com.zjz.zoj.codebox.factory.CodeBoxFactory;
import com.zjz.zoj.codebox.model.CodeBoxProperties;
import com.zjz.zoj.codebox.model.ExecuteInfo;
import com.zjz.zoj.codebox.model.ExecuteRequest;
import com.zjz.zoj.codebox.model.ExecuteResponse;
import com.zjz.zoj.model.dto.judge.JudgeResultResponse;
import com.zjz.zoj.model.dto.question.QuestionJudgeCase;
import com.zjz.zoj.model.dto.question.QuestionJudgeConfig;
import com.zjz.zoj.model.entity.Question;
import com.zjz.zoj.model.entity.QuestionSubmit;
import com.zjz.zoj.model.enums.JudgeInfoEnum;
import com.zjz.zoj.model.enums.JudgeStatusEnum;
import com.zjz.zoj.service.JudgeService;
import com.zjz.zoj.service.QuestionService;
import com.zjz.zoj.service.QuestionSubmitService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private CodeBoxFactory codeBoxFactory;

    @Resource
    private CodeBoxProperties codeBoxProperties;


    @Override
    public JudgeResultResponse doJudge(QuestionSubmit questionSubmit) {
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        Integer status = questionSubmit.getStatus();
        Long questionId = questionSubmit.getQuestionId();
        // 1.查看题目的状态
        if (!Objects.equals(status, JudgeStatusEnum.WAIT.getValue())) {
            // 当前提交的答案信息的状态不是待判状态，直接返回
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交的信息状态错误");
        }
        // 2.查看题目是否存在
        Question question = questionService.getById(questionId);
        if (Objects.isNull(question)) {
            // 题目不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 3.可以调用代码沙箱执行代码
        // 3.1 执行前设置状态为执行中 并更新数据库
        questionSubmit.setStatus(JudgeStatusEnum.JUDGING.getValue());
        questionSubmit.setJudgeInfo(JudgeInfoEnum.WAITING.getValue());
        questionSubmitService.updateById(questionSubmit);
        // 3.2 构建请求
        List<QuestionJudgeCase> judgeCases = JSONUtil.toList(question.getJudgeCase(), QuestionJudgeCase.class);
        List<String> inputs = judgeCases.stream().map(QuestionJudgeCase::getInput).collect(Collectors.toList());
        ExecuteRequest executeRequest = ExecuteRequest.builder()
                .inputs(inputs)
                .code(code)
                .language(language)
                .build();
        // 3.3 调用代码沙箱进行执行
        CodeBox codeBox = codeBoxFactory.getCodeBox(codeBoxProperties.getType());
        ExecuteResponse executeResponse = codeBox.executeCode(executeRequest);
        // 4.通过执行结果进行判题
        if (Objects.equals(executeResponse.getMessage(), CodeBoxExecuteEnum.FAILED.getValue())){
            // 代码沙箱出现错误
            return JudgeResultResponse.builder()
                    .message(JudgeInfoEnum.SYSTEM_ERROR.getText())
                    .build();
        }

        // 代码执行成功 可以进行进一步判题
        List<String> outputs = executeResponse.getOutputs();
        List<ExecuteInfo> executeInfos = executeResponse.getExecuteInfos();

        if (outputs.size() != inputs.size()) {
            questionSubmit.setStatus(JudgeStatusEnum.SUCCESS.getValue());
            questionSubmit.setJudgeInfo(JudgeInfoEnum.WRONG_ANSWER.getValue());
            questionSubmitService.updateById(questionSubmit);
            // 输入输出数量不一致
            return JudgeResultResponse.builder()
                    .message(JudgeInfoEnum.WRONG_ANSWER.getText() + ": 输入输出数量不一致")
                    .errorOutput("实际结果输出数量: " + outputs.size())
                    .errorInput(null)
                    .correctOutput("预期结果输出数量: " + inputs.size())
                    .build();
        }
        String judgeConfig = question.getJudgeConfig();
        List<String> desiredOutputs
                = judgeCases.stream().map(QuestionJudgeCase::getOutput).collect(Collectors.toList());
        QuestionJudgeConfig config = JSONUtil.toBean(judgeConfig, QuestionJudgeConfig.class);
        for (int i = 0; i < executeInfos.size(); i++) {
            // 4.1 先查看当前测试用例是否成功执行并且有输出
            ExecuteInfo executeInfo = executeInfos.get(i);
            if (Objects.equals(executeInfo.getMessage(), "success")) {
                // 4.2 查看是否超时或者超内存
                Long executeTime = executeInfo.getTime();
                Long executeMemory = executeInfo.getMemory();
                if (executeTime > config.getTimeLimit()) {
                    questionSubmit.setStatus(JudgeStatusEnum.SUCCESS.getValue());
                    questionSubmit.setJudgeInfo(JudgeInfoEnum.TIME_LIMIT_EXCEEDED.getValue());
                    questionSubmitService.updateById(questionSubmit);
                    // 执行超时
                    return JudgeResultResponse.builder()
                            .message(JudgeInfoEnum.TIME_LIMIT_EXCEEDED.getText())
                            .errorOutput(outputs.get(i))
                            .errorInput(inputs.get(i))
                            .build();
                }
                if (executeMemory > config.getMemoryLimit()) {
                    questionSubmit.setStatus(JudgeStatusEnum.SUCCESS.getValue());
                    questionSubmit.setJudgeInfo(JudgeInfoEnum.MEMORY_LIMIT_EXCEEDED.getValue());
                    questionSubmitService.updateById(questionSubmit);
                    // 执行超内存
                    return JudgeResultResponse.builder()
                            .message(JudgeInfoEnum.MEMORY_LIMIT_EXCEEDED.getText())
                            .errorOutput(outputs.get(i))
                            .errorInput(inputs.get(i))
                            .build();
                }
                String output = outputs.get(i);
                String desiredOutput = desiredOutputs.get(i);
                if (!Objects.equals(output, desiredOutput)) {
                    // 实际的输出结果与预期的输出结果不一致
                    questionSubmit.setStatus(JudgeStatusEnum.SUCCESS.getValue());
                    questionSubmit.setJudgeInfo(JudgeInfoEnum.WRONG_ANSWER.getValue());
                    questionSubmitService.updateById(questionSubmit);
                    return JudgeResultResponse.builder()
                            .message(JudgeInfoEnum.WRONG_ANSWER.getText())
                            .errorOutput(output)
                            .errorInput(inputs.get(i))
                            .correctOutput(desiredOutput)
                            .build();
                }
            }
        }
        // 没有发现错误
        questionSubmit.setStatus(JudgeStatusEnum.SUCCESS.getValue());
        questionSubmit.setJudgeInfo(JudgeInfoEnum.ACCEPTED.getValue());
        questionSubmitService.updateById(questionSubmit);
        return JudgeResultResponse.builder()
                .message(JudgeInfoEnum.ACCEPTED.getText())
                .build();
    }
}
