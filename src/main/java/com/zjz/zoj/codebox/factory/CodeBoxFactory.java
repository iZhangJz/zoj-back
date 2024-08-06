package com.zjz.zoj.codebox.factory;

import com.zjz.zoj.codebox.CodeBox;
import com.zjz.zoj.codebox.impl.RemoteCodeBox;
import com.zjz.zoj.codebox.impl.SampleCodeBox;
import com.zjz.zoj.codebox.impl.ThirdPartyCodeBox;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 代码沙箱服务类，相当于简单工厂
 */
@Service
public class CodeBoxFactory {

    @Resource
    private ThirdPartyCodeBox thirdPartyCodeBox;

    @Resource
    private RemoteCodeBox remoteCodeBox;

    @Resource
    private SampleCodeBox sampleCodeBox;

    public CodeBox getCodeBox(String type) {
        switch (type) {
            case "sample":
                return sampleCodeBox;
            case "remote":
                return remoteCodeBox;
            case "third":
                return thirdPartyCodeBox;
            default:
                throw new IllegalArgumentException("type not support");
        }
    }
}
