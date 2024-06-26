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
package com.along.modules.system.rest;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import com.along.annotation.Log;
import com.along.modules.system.domain.Menu;
import com.along.exception.BadRequestException;
import com.along.modules.system.domain.vo.MenuVo;
import com.along.modules.system.service.MenuService;
import com.along.modules.system.domain.vo.MenuQueryCriteria;
import com.along.utils.PageResult;
import com.along.utils.PageUtil;
import com.along.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author along
 * @date 2023-12-03
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：菜单管理")
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;
    private static final String ENTITY_NAME = "menu";

    @ApiOperation("导出菜单数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@bms.check('menu:list')")
    public void exportMenu(HttpServletResponse response, MenuQueryCriteria criteria) throws Exception {
        menuService.download(menuService.queryAll(criteria, false), response);
    }

    @GetMapping(value = "/build")
    @ApiOperation("获取前端所需菜单")
    public ResponseEntity<List<MenuVo>> buildMenus(){
        List<Menu> menuList = menuService.findByUser(SecurityUtils.getCurrentUserId());
        List<Menu> menus = menuService.buildTree(menuList);
        return new ResponseEntity<>(menuService.buildMenus(menus),HttpStatus.OK);
    }

    @ApiOperation("返回全部的菜单")
    @GetMapping(value = "/lazy")
    @PreAuthorize("@bms.check('menu:list','roles:list')")
    public ResponseEntity<List<Menu>> queryAllMenu(@RequestParam Long pid){
        return new ResponseEntity<>(menuService.getMenus(pid),HttpStatus.OK);
    }

    @ApiOperation("根据菜单ID返回所有子节点ID，包含自身ID")
    @GetMapping(value = "/child")
    @PreAuthorize("@bms.check('menu:list','roles:list')")
    public ResponseEntity<Object> childMenu(@RequestParam Long id){
        Set<Menu> menuSet = new HashSet<>();
        List<Menu> menuList = menuService.getMenus(id);
        menuSet.add(menuService.getById(id));
        menuSet = menuService.getChildMenus(menuList, menuSet);
        Set<Long> ids = menuSet.stream().map(Menu::getId).collect(Collectors.toSet());
        return new ResponseEntity<>(ids,HttpStatus.OK);
    }

    @GetMapping
    @ApiOperation("查询菜单")
    @PreAuthorize("@bms.check('menu:list')")
    public ResponseEntity<PageResult<Menu>> queryMenu(MenuQueryCriteria criteria) throws Exception {
        List<Menu> menuList = menuService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(menuList),HttpStatus.OK);
    }

    @ApiOperation("查询菜单:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@bms.check('menu:list')")
    public ResponseEntity<List<Menu>> getMenuSuperior(@RequestBody List<Long> ids) {
        Set<Menu> menus = new LinkedHashSet<>();
        if(CollectionUtil.isNotEmpty(ids)){
            for (Long id : ids) {
                Menu menu = menuService.findById(id);
                List<Menu> menuList = menuService.getSuperior(menu, new ArrayList<>());
                for (Menu data : menuList) {
                    if(data.getId().equals(menu.getPid())) {
                        data.setSubCount(data.getSubCount() - 1);
                    }
                }
                menus.addAll(menuList);
            }
            // 编辑菜单时不显示自己以及自己下级的数据，避免出现PID数据环形问题
            menus = menus.stream().filter(i -> !ids.contains(i.getId())).collect(Collectors.toSet());
            return new ResponseEntity<>(menuService.buildTree(new ArrayList<>(menus)),HttpStatus.OK);
        }
        return new ResponseEntity<>(menuService.getMenus(null),HttpStatus.OK);
    }

    @Log("新增菜单")
    @ApiOperation("新增菜单")
    @PostMapping
    @PreAuthorize("@bms.check('menu:add')")
    public ResponseEntity<Object> createMenu(@Validated @RequestBody Menu resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        menuService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改菜单")
    @ApiOperation("修改菜单")
    @PutMapping
    @PreAuthorize("@bms.check('menu:edit')")
    public ResponseEntity<Object> updateMenu(@Validated(Menu.Update.class) @RequestBody Menu resources){
        menuService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除菜单")
    @ApiOperation("删除菜单")
    @DeleteMapping
    @PreAuthorize("@bms.check('menu:del')")
    public ResponseEntity<Object> deleteMenu(@RequestBody Set<Long> ids){
        Set<Menu> menuSet = new HashSet<>();
        for (Long id : ids) {
            List<Menu> menuList = menuService.getMenus(id);
            menuSet.add(menuService.getById(id));
            menuSet = menuService.getChildMenus(menuList, menuSet);
        }
        menuService.delete(menuSet);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
