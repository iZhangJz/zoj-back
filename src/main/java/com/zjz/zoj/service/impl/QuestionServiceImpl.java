package com.zjz.zoj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjz.zoj.common.ErrorCode;
import com.zjz.zoj.constant.CommonConstant;
import com.zjz.zoj.exception.ThrowUtils;
import com.zjz.zoj.mapper.QuestionMapper;
import com.zjz.zoj.model.dto.question.QuestionQueryRequest;
import com.zjz.zoj.model.entity.Question;
import com.zjz.zoj.model.entity.User;
import com.zjz.zoj.model.vo.QuestionVO;
import com.zjz.zoj.model.vo.UserVO;
import com.zjz.zoj.service.QuestionService;
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
 * 题目服务实现
 *
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        String title = question.getTitle();
        String content = question.getContent();
        Integer submitNum = question.getSubmitNum();
        Integer acceptedNum = question.getAcceptedNum();
        Integer thumbNum = question.getThumbNum();
        Integer favourNum = question.getFavourNum();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR,"标题不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR,"题目内容不能为空");
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (ObjectUtils.isNotEmpty(submitNum)){
            ThrowUtils.throwIf(submitNum < 0, ErrorCode.PARAMS_ERROR, "提交数不能小于0");
        }
        if (ObjectUtils.isNotEmpty(acceptedNum)){
            ThrowUtils.throwIf(acceptedNum < 0, ErrorCode.PARAMS_ERROR, "通过数不能小于0");
        }
        if (ObjectUtils.isNotEmpty(thumbNum)){
            ThrowUtils.throwIf(thumbNum < 0, ErrorCode.PARAMS_ERROR, "点赞数不能小于0");
        }
        if (ObjectUtils.isNotEmpty(favourNum)){
            ThrowUtils.throwIf(favourNum < 0, ErrorCode.PARAMS_ERROR, "收藏数不能小于0");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        Long userId = questionQueryRequest.getUserId();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封装
     *
     * @param Question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question Question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(Question);

        // region 可选
        // 关联查询用户信息
        Long userId = Question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // endregion

        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param QuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> QuestionPage, HttpServletRequest request) {
        List<Question> QuestionList = QuestionPage.getRecords();
        Page<QuestionVO> QuestionVOPage = new Page<>(QuestionPage.getCurrent(), QuestionPage.getSize(), QuestionPage.getTotal());
        if (CollUtil.isEmpty(QuestionList)) {
            return QuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> QuestionVOList = QuestionList.stream().map(QuestionVO::objToVo).collect(Collectors.toList());

        // region 可选
        // 关联查询用户信息
        Set<Long> userIdSet = QuestionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        QuestionVOList.forEach(QuestionVO -> {
            Long userId = QuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            QuestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        QuestionVOPage.setRecords(QuestionVOList);
        return QuestionVOPage;
    }

    /**
     * 提交数加 1
     * @param questionId id
     */
    @Override
    public void addSubmitCount(Long questionId) {
        if (questionId != null && questionId > 0) {
            lambdaUpdate().setSql("submitNum = submitNum + 1").eq(Question::getId, questionId).update();
        }
    }
}
