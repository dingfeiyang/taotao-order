package com.taotao.order.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.mapper.TbOrderItemMapper;
import com.taotao.mapper.TbOrderMapper;
import com.taotao.mapper.TbOrderShippingMapper;
import com.taotao.order.dao.JedisClient;
import com.taotao.order.service.OrderService;
import com.taotao.pojo.TbOrder;
import com.taotao.pojo.TbOrderItem;
import com.taotao.pojo.TbOrderShipping;
/**
 * 
 * 订单管理Service
 * 
 * Create by dingfeiyang
 *
 * 2018年12月3日
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	@Autowired
	private TbOrderShippingMapper orderShippingMapper;
	
	@Value("${ORDER_GEN_KEY}")
	private String ORDER_GEN_KEY;
	
	@Value("${ORDER_INIT_KEY}")
	private String ORDER_INIT_KEY;
	
	@Value("${ORDER_DETAIL_GEN_KEY}")
	private String ORDER_DETAIL_GEN_KEY;
	
	@Autowired
	private JedisClient jedisClient;
	
	
	@Override
	public TaotaoResult createOrder(TbOrder order,
			List<TbOrderItem> tbOrderItems, TbOrderShipping orderShipping) {
		//插入订单表
		//生成订单号
		String string = jedisClient.get(ORDER_GEN_KEY);
		if(StringUtils.isBlank(string)){
			jedisClient.set(ORDER_GEN_KEY, ORDER_INIT_KEY);
		}
		long orderId = jedisClient.incr(ORDER_GEN_KEY);
		//补全订单表
		order.setOrderId(orderId+"");
		//状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭
		order.setStatus(1);
		Date date = new Date();
		order.setCreateTime(date);
		order.setUpdateTime(date);
		//买家是否已经评价  0：未评价    1：已评价
		order.setBuyerRate(0);
		orderMapper.insert(order);
		
		//插入订单明细
		for (TbOrderItem tbOrderItem : tbOrderItems) {
			//生成订单明细id
			long orderDetailId = jedisClient.incr(ORDER_DETAIL_GEN_KEY);
			tbOrderItem.setId(orderDetailId+"");
			//插入订单号
			tbOrderItem.setOrderId(orderId+"");
			orderItemMapper.insert(tbOrderItem);
		}
		
		//插入物流信息表
		//补全信息
		orderShipping.setOrderId(orderId+"");
		orderShipping.setCreated(date);
		orderShipping.setUpdated(date);
		orderShippingMapper.insert(orderShipping);
		
		return TaotaoResult.ok(orderId);
	}

}
