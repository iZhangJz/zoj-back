package com.zjz.zoj.sandbox;

import com.zjz.zoj.judge.CodeBox;
import com.zjz.zoj.judge.factory.CodeBoxFactory;
import com.zjz.zoj.judge.model.CodeBoxProperties;
import com.zjz.zoj.judge.model.ExecuteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class CodeSandBoxTest {

    @Resource
    private CodeBoxProperties codeBoxProperties;

    @Test
    public void sandBoxtest() {
        CodeBox codeBox = CodeBoxFactory.createCodeBox(codeBoxProperties.getType());
        codeBox.executeCode(new ExecuteRequest());
    }
}
