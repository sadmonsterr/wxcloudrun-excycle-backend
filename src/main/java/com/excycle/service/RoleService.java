package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.dto.RoleDTO;
import com.excycle.entity.Role;
import com.excycle.vo.RoleVO;

public interface RoleService extends IService<Role> {
    Page<RoleVO> getRolePage(int page, int size, String name);

    RoleVO getRoleById(String id);

    RoleVO createRole(RoleDTO roleDTO);

    RoleVO updateRole(String id, RoleDTO roleDTO);

    boolean deleteRole(String id);
}