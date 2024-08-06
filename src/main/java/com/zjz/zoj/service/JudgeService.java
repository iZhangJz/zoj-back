package com.zjz.zoj.service;

import com.zjz.zoj.model.dto.judge.JudgeResultResponse;
import com.zjz.zoj.model.entity.QuestionSubmit;

public interface JudgeService {

    JudgeResultResponse doJudge(QuestionSubmit questionSubmit);
}
