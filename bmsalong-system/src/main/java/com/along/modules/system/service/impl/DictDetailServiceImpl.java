/*
 *  Copyright 2023-2024 along
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.along.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import com.along.modules.system.domain.Dict;
import com.along.modules.system.domain.DictDetail;
import com.along.modules.system.mapper.DictMapper;
import com.along.modules.system.domain.vo.DictDetailQueryCriteria;
import com.along.utils.*;
import com.along.modules.system.mapper.DictDetailMapper;
import com.along.modules.system.service.DictDetailService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
* @author along
* @date 2019-04-10
*/
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictDetailServiceImpl extends ServiceImpl<DictDetailMapper, DictDetail> implements DictDetailService {

    private final DictMapper dictMapper;
    private final DictDetailMapper dictDetailMapper;
    private final RedisUtils redisUtils;

    @Override
    public PageResult<DictDetail> queryAll(DictDetailQueryCriteria criteria, Page<Object> page) {
        return PageUtil.toPage(dictDetailMapper.findAll(criteria, page));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DictDetail resources) {
        resources.setDictId(resources.getDict().getId());
        save(resources);
        // 清理缓存
        delCaches(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictDetail resources) {
        DictDetail dictDetail = getById(resources.getId());
        resources.setId(dictDetail.getId());
        // 更新数据
        saveOrUpdate(resources);
        // 清理缓存
        delCaches(dictDetail);
    }

    @Override
    @Cacheable(key = "'name:' + #p0")
    public List<DictDetail> getDictByName(String name) {
        return dictDetailMapper.findByDictName(name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DictDetail dictDetail = getById(id);
        removeById(id);
        // 清理缓存
        delCaches(dictDetail);
    }

    public void delCaches(DictDetail dictDetail){
        Dict dict = dictMapper.selectById(dictDetail.getDictId());
        redisUtils.del(CacheKey.DICT_NAME + dict.getName());
    }
}