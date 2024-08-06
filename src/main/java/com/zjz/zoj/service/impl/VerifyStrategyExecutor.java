package com.zjz.zoj.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zjz.zoj.annotation.VerifyStrategyCheck;
import com.zjz.zoj.common.ErrorCode;
import com.zjz.zoj.exception.BusinessException;
import com.zjz.zoj.model.dto.judge.VerifyContext;
import com.zjz.zoj.model.dto.judge.JudgeResultResponse;
import com.zjz.zoj.service.VerifyStrategyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 策略执行器
 */
@Service
public class VerifyStrategyExecutor {

    /**
     * 策略列表
     */
    @Resource
    private List<VerifyStrategyService> strategies;

    /**
     * 执行策略分配
     * @param language 语言
     */
    public JudgeResultResponse doDispatch(String language, VerifyContext verifyContext){
        if (StrUtil.isBlank(language)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "language is null");
        }
        for (VerifyStrategyService strategy : strategies) {
            // 先确认该对象是否有注解
            if(strategy.getClass().isAnnotationPresent(VerifyStrategyCheck.class)){
                // 有注解
                VerifyStrategyCheck annotation = strategy.getClass().getAnnotation(VerifyStrategyCheck.class);
                if (annotation != null && Objects.equals(annotation.language(), "")) {
                    throw new IllegalArgumentException("The 'language' attribute must be set in @VerifyStrategyCheck annotation.");
                }
                assert annotation != null;
                if (Objects.equals(annotation.language(), language)) {
                    return strategy.doVerify(verifyContext);
                }
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "language is not support");
    }
}
