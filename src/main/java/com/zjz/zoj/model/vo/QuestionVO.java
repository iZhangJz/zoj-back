package com.zjz.zoj.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjz.zoj.model.dto.question.QuestionJudgeCase;
import com.zjz.zoj.model.dto.question.QuestionJudgeConfig;
import com.zjz.zoj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目视图
 *
 */
@Data
public class QuestionVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 判题用例（json 数组）
     */
    private List<QuestionJudgeCase> judgeCase;

    /**
     * 判题配置（json 对象）
     */
    private QuestionJudgeConfig judgeConfig;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 封装类转对象
     *
     * @param QuestionVO
     * @return
     */
    public static Question voToObj(QuestionVO QuestionVO) {
        if (QuestionVO == null) {
            return null;
        }
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionVO, Question);
        List<String> questionVOTags = QuestionVO.getTags();
        if (questionVOTags != null) {
            Question.setTags(JSONUtil.toJsonStr(questionVOTags));
        }
        List<QuestionJudgeCase> questionVOJudgeCase = QuestionVO.getJudgeCase();
        if (questionVOJudgeCase != null) {
            Question.setJudgeCase(JSONUtil.toJsonStr(questionVOJudgeCase));
        }
        QuestionJudgeConfig questionVOJudgeConfig = QuestionVO.getJudgeConfig();
        if (questionVOJudgeConfig != null) {
            Question.setJudgeConfig(JSONUtil.toJsonStr(questionVOJudgeConfig));
        }
        return Question;
    }

    /**
     * 对象转封装类
     *
     * @param Question
     * @return
     */
    public static QuestionVO objToVo(Question Question) {
        if (Question == null) {
            return null;
        }
        QuestionVO QuestionVO = new QuestionVO();
        BeanUtils.copyProperties(Question, QuestionVO);
        String questionTags = Question.getTags();
        if (questionTags != null) {
            QuestionVO.setTags(JSONUtil.toList(questionTags, String.class));
        }
        String questionJudgeCase = Question.getJudgeCase();
        if (questionJudgeCase != null) {
            QuestionVO.setJudgeCase(JSONUtil.toList(questionJudgeCase, QuestionJudgeCase.class));
        }
        String questionJudgeConfig = Question.getJudgeConfig();
        if (questionJudgeConfig != null) {
            QuestionVO.setJudgeConfig(JSONUtil.toBean(questionJudgeConfig, QuestionJudgeConfig.class));
        }
        return QuestionVO;
    }
}
