package com.zjz.zoj.model.dto.submit;

import com.zjz.zoj.model.dto.question.QuestionJudgeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 编辑题目提交请求
 */
@Data
public class QuestionSubmitEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息（json 对象）
     */
    private QuestionJudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;



    private static final long serialVersionUID = 1L;
}