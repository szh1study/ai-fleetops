package com.aifleetops.service.impl;

import com.aifleetops.entity.Order;
import com.aifleetops.mapper.OrderMapper;
import com.aifleetops.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
