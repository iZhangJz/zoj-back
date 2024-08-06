package com.zjz.zoj.judge.impl;

import com.zjz.zoj.judge.CodeBox;
import com.zjz.zoj.judge.model.ExecuteRequest;
import com.zjz.zoj.judge.model.ExecuteResponse;

/**
 * 示例代码沙箱 用于跑通业务流程
 */
public class SampleCodeBox implements CodeBox {
    @Override
    public ExecuteResponse executeCode(ExecuteRequest executeRequest) {
        // TODO 示例代码沙箱
        System.out.println("示例代码沙箱");
        return null;
    }

    @Override
    public String getStatus() {
        // TODO 示例代码沙箱状态
        System.out.println("示例代码沙箱状态");
        return null;
    }
}
