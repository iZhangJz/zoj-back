package com.zjz.zoj.model.dto.judge;

import com.zjz.zoj.codebox.model.ExecuteResponse;
import com.zjz.zoj.model.entity.Question;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 判题逻辑上下文
 */
@Data
@Builder
public class VerifyContext {
    /**
     * 代码沙箱执行响应
     */
    private ExecuteResponse executeResponse;

    /**
     * 题目信息
     */
    private Question question;

    /**
     * 期望的输出
     */
    private List<String> desiredOutputs;

    /**
     * 用例输入
     */
    private List<String> inputs;


}
