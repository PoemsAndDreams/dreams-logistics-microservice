package com.dreams.logistics.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreams.logistics.common.ErrorCode;
import com.dreams.logistics.exception.BusinessException;
import com.dreams.logistics.model.entity.TransportInfo;
import com.dreams.logistics.model.entity.TransportInfoDetail;
import com.dreams.logistics.model.entity.TransportOrder;
import com.dreams.logistics.service.TransportInfoService;
import com.dreams.logistics.service.TransportOrderService;
import com.dreams.logistics.utils.ObjectUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TransportInfoServiceImpl implements TransportInfoService {

    @Resource
    private MongoTemplate mongoTemplate;


    @Resource
    private TransportOrderService transportOrderService;

    @Override
    public TransportInfo saveOrUpdate(String transportOrderId, TransportInfoDetail infoDetail) {
        //根据运单id查询
        Query query = Query.query(Criteria.where("transportOrderId").is(transportOrderId)); //构造查询条件
        TransportInfo transportInfoEntity = this.mongoTemplate.findOne(query, TransportInfo.class);
        if (ObjectUtil.isEmpty(transportInfoEntity)) {
            //运单信息不存在，新增数据
            transportInfoEntity = new TransportInfo();
            transportInfoEntity.setTransportOrderId(transportOrderId);
            transportInfoEntity.setInfoList(ListUtil.toList(infoDetail));
            transportInfoEntity.setCreated(System.currentTimeMillis());
        } else {
            //运单信息存在，只需要追加物流详情数据
            transportInfoEntity.getInfoList().add(infoDetail);
        }
        //无论新增还是更新都要设置更新时间
        transportInfoEntity.setUpdated(System.currentTimeMillis());
        //保存/更新到MongoDB
        return this.mongoTemplate.save(transportInfoEntity);
    }
    @Override
    public TransportInfo queryByTransportOrderId(String transportOrderId) {
        //根据运单id查询
        Query query = Query.query(Criteria.where("transportOrderId").is(transportOrderId)); //构造查询条件
        TransportInfo transportInfoEntity = this.mongoTemplate.findOne(query, TransportInfo.class);
        if (ObjectUtil.isNotEmpty(transportInfoEntity)) {
            return transportInfoEntity;
        }
        throw new BusinessException(ErrorCode.NOT_TransportInfo_FOUND);
    }


    @Override
    public TransportInfo queryByOrderId(String orderId) {

        LambdaQueryWrapper<TransportOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransportOrder::getOrderId,orderId);
        TransportOrder one = transportOrderService.getOne(wrapper);

        if (ObjectUtil.isEmpty(one)){
            return null;
        }

        //根据运单id查询
        Query query = Query.query(Criteria.where("transportOrderId").is(one.getId())); //构造查询条件
        TransportInfo transportInfoEntity = this.mongoTemplate.findOne(query, TransportInfo.class);
        if (ObjectUtil.isNotEmpty(transportInfoEntity)) {
            return transportInfoEntity;
        }
        throw new BusinessException(ErrorCode.NOT_TransportInfo_FOUND);
    }
}
