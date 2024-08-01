package com.zjz.zoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.zoj.annotation.AuthCheck;
import com.zjz.zoj.common.BaseResponse;
import com.zjz.zoj.common.DeleteRequest;
import com.zjz.zoj.common.ErrorCode;
import com.zjz.zoj.common.ResultUtils;
import com.zjz.zoj.constant.UserConstant;
import com.zjz.zoj.exception.BusinessException;
import com.zjz.zoj.exception.ThrowUtils;
import com.zjz.zoj.model.dto.question.QuestionAddRequest;
import com.zjz.zoj.model.dto.question.QuestionEditRequest;
import com.zjz.zoj.model.dto.question.QuestionQueryRequest;
import com.zjz.zoj.model.dto.question.QuestionUpdateRequest;
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

    // region 增删改查

    /**
     * 创建题目
     *
     * @param QuestionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest QuestionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionAddRequest == null, ErrorCode.PARAMS_ERROR);
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionAddRequest, Question);
        // 数据校验
        questionService.validQuestion(Question, true);
        User loginUser = userService.getLoginUser(request);
        Question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(Question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = Question.getId();
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
     * @param QuestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest QuestionUpdateRequest) {
        if (QuestionUpdateRequest == null || QuestionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionUpdateRequest, Question);
        // 数据校验
        questionService.validQuestion(Question, false);
        // 判断是否存在
        long id = QuestionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.updateById(Question);
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

    // 公共的分页和查询逻辑
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
     * @param QuestionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest QuestionEditRequest, HttpServletRequest request) {
        if (QuestionEditRequest == null || QuestionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionEditRequest, Question);
        // 数据校验
        questionService.validQuestion(Question, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = QuestionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.updateById(Question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
