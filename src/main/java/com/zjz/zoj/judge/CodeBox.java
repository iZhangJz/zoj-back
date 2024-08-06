package com.zjz.zoj.judge;

import com.zjz.zoj.judge.model.ExecuteRequest;
import com.zjz.zoj.judge.model.ExecuteResponse;

public interface CodeBox {
    /**
     * 执行代码接口
     */
    ExecuteResponse executeCode(ExecuteRequest executeRequest);

    /**
     * 查看代码沙箱状态
     */
    String getStatus();
}
