package com.zjz.zoj.codebox.impl;

import com.zjz.zoj.annotation.CodeBoxLog;
import com.zjz.zoj.codebox.CodeBox;
import com.zjz.zoj.codebox.model.ExecuteRequest;
import com.zjz.zoj.codebox.model.ExecuteResponse;
import org.springframework.stereotype.Component;

/**
 * 远程代码沙箱 调用已经实现的 OJ 系统
 */
@Component
public class RemoteCodeBox implements CodeBox {

    @Override
    @CodeBoxLog
    public ExecuteResponse executeCode(ExecuteRequest executeRequest) {
        // TODO 远程代码沙箱 调用已经实现的 OJ 系统
        System.out.println("远程代码沙箱 调用已经实现的 OJ 系统");
        return null;
    }

    @Override
    public String getStatus() {
        // TODO 远程代码沙箱状态
        System.out.println("远程代码沙箱 调用已经实现的 OJ 系统状态");
        return null;
    }
}
