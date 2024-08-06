package com.zjz.zoj.service.impl;

import cn.hutool.json.JSONUtil;
import com.zjz.zoj.codebox.CodeBox;
import com.zjz.zoj.codebox.enums.CodeBoxExecuteEnum;
import com.zjz.zoj.codebox.factory.CodeBoxFactory;
import com.zjz.zoj.codebox.model.CodeBoxProperties;
import com.zjz.zoj.codebox.model.ExecuteRequest;
import com.zjz.zoj.codebox.model.ExecuteResponse;
import com.zjz.zoj.common.ErrorCode;
import com.zjz.zoj.exception.BusinessException;
import com.zjz.zoj.model.dto.judge.JudgeResultResponse;
import com.zjz.zoj.model.dto.judge.VerifyContext;
import com.zjz.zoj.model.dto.question.QuestionJudgeCase;
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

    @Resource
    private VerifyStrategyExecutor verifyStrategyExecutor;


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
        VerifyContext context = VerifyContext.builder()
                .inputs(inputs)
                .question(question)
                .executeResponse(executeResponse)
                .desiredOutputs(judgeCases.stream().map(QuestionJudgeCase::getOutput).collect(Collectors.toList()))
                .build();
        // 4.1 调用判题器进行判题
        JudgeResultResponse response = null;

        try {
            response = verifyStrategyExecutor.doDispatch(language, context);
            questionSubmit.setJudgeInfo(response.getMessage());
            questionSubmit.setStatus(JudgeStatusEnum.SUCCESS.getValue());
        } catch (BusinessException | IllegalArgumentException e) {
            questionSubmit.setJudgeInfo(JudgeInfoEnum.SYSTEM_ERROR.getValue());
            questionSubmit.setStatus(JudgeStatusEnum.FAIL.getValue());
            throw e;
        } finally {
            questionSubmitService.updateById(questionSubmit);
        }

        return response;

    }
}
