package com.zjz.zoj.judge.impl;

import com.zjz.zoj.judge.CodeBox;
import com.zjz.zoj.judge.model.ExecuteRequest;
import com.zjz.zoj.judge.model.ExecuteResponse;

/**
 * 第三方代码沙箱 调用网上已经实现的代码沙箱
 */
public class ThirdPartyCodeBox implements CodeBox {
    @Override
    public ExecuteResponse executeCode(ExecuteRequest executeRequest) {
        // TODO 调用第三方代码沙箱
        System.out.println("调用第三方代码沙箱");
        return null;
    }

    @Override
    public String getStatus() {
        // TODO 查看第三方代码沙箱状态
        System.out.println("查看第三方代码沙箱状态");
        return null;
    }
}
