package com.aifleetops.service.impl;

import com.aifleetops.entity.OperationReview;
import com.aifleetops.mapper.OperationReviewMapper;
import com.aifleetops.service.OperationReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OperationReviewServiceImpl extends ServiceImpl<OperationReviewMapper, OperationReview> implements OperationReviewService {
}
