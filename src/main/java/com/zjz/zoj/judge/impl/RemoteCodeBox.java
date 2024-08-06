package com.zjz.zoj.judge.impl;

import com.zjz.zoj.judge.CodeBox;
import com.zjz.zoj.judge.model.ExecuteRequest;
import com.zjz.zoj.judge.model.ExecuteResponse;

/**
 * 远程代码沙箱 调用已经实现的 OJ 系统
 */
public class RemoteCodeBox implements CodeBox {
    @Override
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
