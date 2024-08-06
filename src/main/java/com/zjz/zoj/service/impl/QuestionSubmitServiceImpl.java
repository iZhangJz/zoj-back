package com.zjz.zoj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjz.zoj.common.ErrorCode;
import com.zjz.zoj.constant.CommonConstant;
import com.zjz.zoj.exception.ThrowUtils;
import com.zjz.zoj.mapper.QuestionSubmitMapper;
import com.zjz.zoj.model.dto.submit.QuestionSubmitQueryRequest;
import com.zjz.zoj.model.entity.Question;
import com.zjz.zoj.model.entity.QuestionSubmit;
import com.zjz.zoj.model.entity.User;
import com.zjz.zoj.model.enums.LanguageEnum;
import com.zjz.zoj.model.vo.QuestionSubmitVO;
import com.zjz.zoj.model.vo.UserVO;
import com.zjz.zoj.service.QuestionService;
import com.zjz.zoj.service.QuestionSubmitService;
import com.zjz.zoj.service.UserService;
import com.zjz.zoj.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目提交服务实现
 *
 */
@Service
@Slf4j
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    /**
     * 校验数据
     *
     * @param questionSubmit
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionSubmit(QuestionSubmit questionSubmit, boolean add) {
        ThrowUtils.throwIf(questionSubmit == null, ErrorCode.PARAMS_ERROR);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        Integer status = questionSubmit.getStatus();
        Long questionId = questionSubmit.getQuestionId();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(language), ErrorCode.PARAMS_ERROR,"编程语言不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(code), ErrorCode.PARAMS_ERROR,"提交代码不能为空");
            ThrowUtils.throwIf(ObjectUtils.isEmpty(questionId), ErrorCode.PARAMS_ERROR,"题目不存在");
        }
        // 修改数据时，有参数则校验
        if (ObjectUtils.isNotEmpty(questionId)){
            Question success = questionService.getById(questionId);
            ThrowUtils.throwIf(success == null, ErrorCode.PARAMS_ERROR,"题目不存在");
        }
        if (StringUtils.isNotBlank(language)) {
            ThrowUtils.throwIf(LanguageEnum.getEnumByValue(language) == null,
                    ErrorCode.PARAMS_ERROR, "编程语言不合法");
        }
        if (ObjectUtils.isNotEmpty(status)) {
            ThrowUtils.throwIf(status < 0 || status > 3, ErrorCode.PARAMS_ERROR, "状态值不合法");
        }

    }

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionSubmitQueryRequest.getId();
        Long notId = questionSubmitQueryRequest.getNotId();
        String searchText = questionSubmitQueryRequest.getSearchText();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        Long userId = questionSubmitQueryRequest.getUserId();
        String language = questionSubmitQueryRequest.getLanguage();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(language),"language",language);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目提交封装
     *
     * @param questionSubmit
     * @param request
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, HttpServletRequest request) {
        // 对象转封装类
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);

        // region 可选
        // 关联查询用户信息
        Long userId = questionSubmit.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionSubmitVO.setUser(userVO);

        return questionSubmitVO;
    }

    /**
     * 分页获取题目提交封装
     *
     * @param questionSubmitPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage,
                                                          HttpServletRequest request) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage
                = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionSubmitVO> questionSubmitVOList
                = questionSubmitList.stream().map(QuestionSubmitVO::objToVo).collect(Collectors.toList());

        // region 可选
        // 关联查询用户信息
        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionSubmitVOList.forEach(questionSubmitVO -> {
            Long userId = questionSubmitVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionSubmitVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

}
