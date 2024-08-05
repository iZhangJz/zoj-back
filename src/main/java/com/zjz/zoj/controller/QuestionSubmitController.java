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
import com.zjz.zoj.model.dto.submit.QuestionSubmitAddRequest;
import com.zjz.zoj.model.dto.submit.QuestionSubmitEditRequest;
import com.zjz.zoj.model.dto.submit.QuestionSubmitQueryRequest;
import com.zjz.zoj.model.dto.submit.QuestionSubmitUpdateRequest;
import com.zjz.zoj.model.entity.QuestionSubmit;
import com.zjz.zoj.model.entity.User;
import com.zjz.zoj.model.vo.QuestionSubmitVO;
import com.zjz.zoj.service.QuestionService;
import com.zjz.zoj.service.QuestionSubmitService;
import com.zjz.zoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

/**
 * 题目提交接口
 *
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;


    // 公共的分页和查询逻辑
    private <T> BaseResponse<Page<T>> handleQuestionSubmitQuery(QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                HttpServletRequest request,
                                                                boolean isAdmin,
                                                                boolean setUserId,
                                                                Function<Page<QuestionSubmit>, Page<T>> mapper) {
        if (questionSubmitQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();

        // 限制爬虫
        if (!isAdmin) {
            ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        }

        // 如果需要设置用户ID
        if (setUserId) {
            User loginUser = userService.getLoginUser(request);
            questionSubmitQueryRequest.setUserId(loginUser.getId());
        }

        // 查询数据库
        Page<QuestionSubmit> questionPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(mapper.apply(questionPage));
    }


    // region 增删改查

    /**
     * 创建题目提交
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionSubmit(
            @RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionSubmitAddRequest == null, ErrorCode.PARAMS_ERROR);
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitAddRequest, questionSubmit);
        // 数据校验
        questionSubmitService.validQuestionSubmit(questionSubmit, true);
        User loginUser = userService.getLoginUser(request);
        questionSubmit.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionSubmitService.save(questionSubmit);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 将改题目的提交次数加一
        questionService.addSubmitCount(questionSubmit.getQuestionId());
        // 返回新写入的数据 id
        long newQuestionSubmitId = questionSubmit.getId();
        return ResultUtils.success(newQuestionSubmitId);
    }

    /**
     * 删除题目提交
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionSubmit(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionSubmit oldQuestionSubmit = questionSubmitService.getById(id);
        ThrowUtils.throwIf(oldQuestionSubmit == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionSubmit.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionSubmitService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目提交（仅管理员可用）
     *
     * @param questionSubmitUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionSubmit(@RequestBody QuestionSubmitUpdateRequest questionSubmitUpdateRequest) {
        if (questionSubmitUpdateRequest == null || questionSubmitUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitUpdateRequest, questionSubmit);
        // 数据校验
        questionSubmitService.validQuestionSubmit(questionSubmit, false);
        // 判断是否存在
        long id = questionSubmitUpdateRequest.getId();
        QuestionSubmit oldQuestionSubmit = questionSubmitService.getById(id);
        ThrowUtils.throwIf(oldQuestionSubmit == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionSubmitService.updateById(questionSubmit);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目提交（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionSubmitVO> getQuestionSubmitVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QuestionSubmit questionSubmit = questionSubmitService.getById(id);
        ThrowUtils.throwIf(questionSubmit == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVO(questionSubmit, request));
    }


    /**
     * 分页获取题目提交列表（仅管理员可用）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSubmit>> listQuestionSubmitByPage(
            @RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        return handleQuestionSubmitQuery(questionSubmitQueryRequest, null, true, false, page -> page);
    }

    /**
     * 分页获取题目提交列表（封装类）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitVOByPage(
            @RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        return handleQuestionSubmitQuery(questionSubmitQueryRequest, request, false, false,
                page -> questionSubmitService.getQuestionSubmitVOPage(page, request));
    }

    /**
     * 分页获取当前登录用户创建的题目提交列表
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionSubmitVO>> listMyQuestionSubmitVOByPage(
            @RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        return handleQuestionSubmitQuery(questionSubmitQueryRequest, request, false, true,
                page -> questionSubmitService.getQuestionSubmitVOPage(page, request));
    }

    /**
     * 编辑题目提交（给用户使用）
     *
     * @param questionSubmitEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestionSubmit(
            @RequestBody QuestionSubmitEditRequest questionSubmitEditRequest, HttpServletRequest request) {
        if (questionSubmitEditRequest == null || questionSubmitEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitEditRequest, questionSubmit);
        // 数据校验
        questionSubmitService.validQuestionSubmit(questionSubmit, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionSubmitEditRequest.getId();
        QuestionSubmit oldQuestionSubmit = questionSubmitService.getById(id);
        ThrowUtils.throwIf(oldQuestionSubmit == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestionSubmit.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionSubmitService.updateById(questionSubmit);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
