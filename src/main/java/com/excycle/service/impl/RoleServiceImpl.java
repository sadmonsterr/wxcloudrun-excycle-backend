package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.dto.RoleDTO;
import com.excycle.entity.Role;
import com.excycle.mapper.RoleMapper;
import com.excycle.service.RoleService;
import com.excycle.utils.UUIDUtils;
import com.excycle.vo.RoleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private Map<String, RoleVO> roleMap = new HashMap<>();

    @PostConstruct
    public void init() {
        roleMap = getRolePage(1, 100, "").getRecords().stream()
                .collect(Collectors.toMap(RoleVO::getRoleId, Function.identity()));
    }

    @Override
    public Page<RoleVO> getRolePage(int page, int size, String name) {
        Page<Role> rolePage = new Page<>(page, size);
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .like(StringUtils.isNotBlank(name), Role::getName, name)
                .orderByDesc(Role::getId);

        Page<Role> resultPage = page(rolePage, queryWrapper);

        Page<RoleVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage);
        voPage.setRecords(resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList()));

        return voPage;
    }

    @Override
    public RoleVO getRoleById(String id) {
        return roleMap.get(id);
    }

    @Override
    public RoleVO createRole(RoleDTO roleDTO) {
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        if (role.getRoleId() == null) {
            role.setRoleId(UUIDUtils.nextBase62SnowflakeId());
        }
        role.setDeleted(0L);
        save(role);
        return convertToVO(role);
    }

    @Override
    public RoleVO updateRole(String id, RoleDTO roleDTO) {
        Role role = getById(id);
        if (role == null) {
            return null;
        }
        BeanUtils.copyProperties(roleDTO, role);
        updateById(role);
        return convertToVO(role);
    }

    @Override
    public boolean deleteRole(String id) {
        Role role = getById(id);
        if (role != null) {
            role.setDeleted(1L);
            return updateById(role);
        }
        return false;
    }

    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}