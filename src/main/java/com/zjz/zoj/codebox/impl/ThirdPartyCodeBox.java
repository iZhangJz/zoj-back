package com.zjz.zoj.codebox.impl;

import com.zjz.zoj.annotation.CodeBoxLog;
import com.zjz.zoj.codebox.CodeBox;
import com.zjz.zoj.codebox.model.ExecuteRequest;
import com.zjz.zoj.codebox.model.ExecuteResponse;
import org.springframework.stereotype.Component;

/**
 * 第三方代码沙箱 调用网上已经实现的代码沙箱
 */
@Component
public class ThirdPartyCodeBox implements CodeBox {

    @Override
    @CodeBoxLog
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
