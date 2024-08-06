package com.zjz.zoj.codebox.impl;

import com.zjz.zoj.annotation.CodeBoxLog;
import com.zjz.zoj.codebox.CodeBox;
import com.zjz.zoj.codebox.model.ExecuteRequest;
import com.zjz.zoj.codebox.model.ExecuteResponse;
import com.zjz.zoj.model.enums.JudgeStatusEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 示例代码沙箱 用于跑通业务流程
 */
@Component
public class SampleCodeBox implements CodeBox {
    @Override
    @CodeBoxLog
    public ExecuteResponse executeCode(ExecuteRequest executeRequest) {
        // TODO 示例代码沙箱
        System.out.println("示例代码沙箱");
        return ExecuteResponse.builder()
                .outputs(executeRequest.getInputs())
                .message(JudgeStatusEnum.SUCCESS.getText())
                .executeInfos(new ArrayList<>())
                .build();
    }

    @Override
    public String getStatus() {
        // TODO 示例代码沙箱状态
        System.out.println("示例代码沙箱状态");
        return null;
    }
}
