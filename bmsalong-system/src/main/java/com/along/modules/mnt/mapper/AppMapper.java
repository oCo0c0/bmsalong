/*
 *  Copyright 2023-2023 along
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
package com.along.modules.mnt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.along.modules.mnt.domain.App;
import com.along.modules.mnt.domain.vo.AppQueryCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author along
 * @description
 * @date 2023-06-12
 **/
@Mapper
public interface AppMapper extends BaseMapper<App> {

    IPage<App> queryAll(@Param("criteria") AppQueryCriteria criteria, Page<Object> page);

    List<App> queryAll(@Param("criteria") AppQueryCriteria criteria);
}
