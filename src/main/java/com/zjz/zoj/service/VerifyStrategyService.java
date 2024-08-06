package com.zjz.zoj.service;

import com.zjz.zoj.model.dto.judge.VerifyContext;
import com.zjz.zoj.model.dto.judge.JudgeResultResponse;

/**
 * 判题策略接口
 */
public interface VerifyStrategyService {

    /**
     * 执行判题
     *
     * @param verifyContext 判题上下文
     * @return
     */
    JudgeResultResponse doVerify(VerifyContext verifyContext);
}
