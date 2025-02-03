package com.dreams.logistics.service;


import com.dreams.logistics.model.dto.line.Organ;

import java.util.List;

public interface OrganService {

    /**
     * 无需指定type，根据id查询
     *
     * @param bid 机构id
     * @return 机构信息
     */
    Organ findByBid(Long bid);

    /**
     * 无需指定type，根据ids查询
     *
     * @param bids 机构ids
     * @return 机构信息
     */
    List<Organ> findByBids(List<Long> bids);

    /**
     * 查询所有的机构，如果name不为空的按照name模糊查询
     *
     * @param name 机构名称
     * @return 机构列表
     */
    List<Organ> findAll(String name);

    /**
     * 查询机构树
     * @return 机构树
     */
    String findAllTree();
}
