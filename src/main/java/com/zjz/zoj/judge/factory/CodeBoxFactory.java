package com.zjz.zoj.judge.factory;

import com.zjz.zoj.judge.CodeBox;
import com.zjz.zoj.judge.impl.RemoteCodeBox;
import com.zjz.zoj.judge.impl.SampleCodeBox;
import com.zjz.zoj.judge.impl.ThirdPartyCodeBox;

/**
 * 简单工厂方法 生成不同的代码沙箱实例
 */
public class CodeBoxFactory {

    public static CodeBox createCodeBox(String type){
        CodeBox codeBox = null;
        switch (type){
            case "example":
                codeBox = new SampleCodeBox();
                break;
            case "remote":
                codeBox = new RemoteCodeBox();
                break;
            case "third":
                codeBox = new ThirdPartyCodeBox();
                break;
            default:
                codeBox = new SampleCodeBox();
                break;
        }
        return codeBox;
    }
}
