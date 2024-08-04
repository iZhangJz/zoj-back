package com.zjz.zoj.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.zoj.annotation.AuthCheck;
import com.zjz.zoj.common.BaseResponse;
import com.zjz.zoj.common.DeleteRequest;
import com.zjz.zoj.common.ErrorCode;
import com.zjz.zoj.common.ResultUtils;
import com.zjz.zoj.constant.UserConstant;
import com.zjz.zoj.exception.BusinessException;
import com.zjz.zoj.exception.ThrowUtils;
import com.zjz.zoj.model.dto.question.*;
import com.zjz.zoj.model.entity.Question;
import com.zjz.zoj.model.entity.User;
import com.zjz.zoj.model.vo.QuestionVO;
import com.zjz.zoj.service.QuestionService;
import com.zjz.zoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 题目接口
 *
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    /**
     * 查询题目列表
     * @param questionQueryRequest 查询请求
     * @param request Http 请求
     * @param isAdmin 是否为管理员
     * @param setUserId 是否设置用户ID
     * @param mapper 查询结果转换器
     * @return 题目分页
     */
    private <T> BaseResponse<Page<T>> handleQuestionQuery(QuestionQueryRequest questionQueryRequest,
                                                          HttpServletRequest request,
                                                          boolean isAdmin,
                                                          boolean setUserId,
                                                          Function<Page<Question>, Page<T>> mapper) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();

        // 限制爬虫
        if (!isAdmin) {
            ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        }

        // 如果需要设置用户ID
        if (setUserId) {
            User loginUser = userService.getLoginUser(request);
            questionQueryRequest.setUserId(loginUser.getId());
        }

        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(mapper.apply(questionPage));
    }

    /**
     * 设置题目部分属性
     * @param question 题目
     * @param request 请求
     */
    private void setQuestionProperty(Question question, Object request) {
        List<String> tags = getTags(request);
        if (tags == null || tags.isEmpty()){
            tags = new ArrayList<>();
        }
        question.setTags(JSONUtil.toJsonStr(tags));

        List<QuestionJudgeCase> judgeCase = getJudgeCase(request);
        if (judgeCase == null || judgeCase.isEmpty()){
            judgeCase = new ArrayList<>();
        }
        question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));

        QuestionJudgeConfig judgeConfig = getJudgeConfig(request);
        if (judgeConfig == null){
            judgeConfig = new QuestionJudgeConfig();
        }
        question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
    }

    /**
     * 获取请求中的标签属性
     * @param request 请求
     * @return 标签列表
     */
    private List<String> getTags(Object request) {
        if (request instanceof QuestionAddRequest) {
            return ((QuestionAddRequest) request).getTags();
        } else if (request instanceof QuestionUpdateRequest) {
            return ((QuestionUpdateRequest) request).getTags();
        } else if (request instanceof QuestionEditRequest){
            return ((QuestionEditRequest) request).getTags();
        }
        return null;
    }

    /**
     * 获取请求中的判题用例属性
     * @param request 请求
     * @return 判题用例列表
     */
    private List<QuestionJudgeCase> getJudgeCase(Object request) {
        if (request instanceof QuestionAddRequest) {
            return ((QuestionAddRequest) request).getJudgeCase();
        } else if (request instanceof QuestionUpdateRequest) {
            return ((QuestionUpdateRequest) request).getJudgeCase();
        } else if (request instanceof QuestionEditRequest){
            return ((QuestionEditRequest) request).getJudgeCase();
        }
        return null;
    }

    /**
     * 获取请求中的判题配置属性
     * @param request 请求
     * @return 判题配置
     */
    private QuestionJudgeConfig getJudgeConfig(Object request) {
        if (request instanceof QuestionAddRequest) {
            return ((QuestionAddRequest) request).getJudgeConfig();
        } else if (request instanceof QuestionUpdateRequest) {
            return ((QuestionUpdateRequest) request).getJudgeConfig();
        } else if (request instanceof QuestionEditRequest){
            return ((QuestionEditRequest) request).getJudgeConfig();
        }
        return null;
    }


    // region 增删改查

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        setQuestionProperty(question,questionAddRequest);
        // 数据校验
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);

        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        setQuestionProperty(question,questionUpdateRequest);

        // 数据校验
        questionService.validQuestion(question, false);

        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question Question = questionService.getById(id);
        ThrowUtils.throwIf(Question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(Question, request));
    }



    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        return handleQuestionQuery(questionQueryRequest, null, true, false, page -> page);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        return handleQuestionQuery(questionQueryRequest, request, false, false,
                page -> questionService.getQuestionVOPage(page, request));
    }

    /**
     * 分页获取当前登录用户创建的题目列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        return handleQuestionQuery(questionQueryRequest, request, false, true,
                page -> questionService.getQuestionVOPage(page, request));
    }

    /**
     * 编辑题目（给用户使用）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        setQuestionProperty(question,questionEditRequest);

        // 数据校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
