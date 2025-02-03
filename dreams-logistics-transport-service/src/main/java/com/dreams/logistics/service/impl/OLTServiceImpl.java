package com.dreams.logistics.service.impl;

import com.dreams.logistics.entity.node.OLTEntity;
import com.dreams.logistics.repository.OLTRepository;
import com.dreams.logistics.service.OLTService;
import org.springframework.stereotype.Service;

@Service
public class OLTServiceImpl extends ServiceImpl<OLTRepository, OLTEntity>
        implements OLTService {
}
