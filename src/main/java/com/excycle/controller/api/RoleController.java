package com.excycle.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.common.Result;
import com.excycle.dto.RoleDTO;
import com.excycle.service.RoleService;
import com.excycle.vo.RoleVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Data
    public static class RoleQueryRequest {
        private int page = 1;
        private int size = 10;
        private String name;
    }

    /**
     * 获取角色列表
     * POST /api/v1/roles/list
     */
    @PostMapping("/list")
    public Result<Page<RoleVO>> getRoles(@RequestBody RoleQueryRequest queryRequest) {
        return Result.success(roleService.getRolePage(queryRequest.getPage(), queryRequest.getSize(), queryRequest.getName()));
    }

    /**
     * 根据ID获取角色
     * GET /api/v1/roles/{id}
     */
    @GetMapping("/{id}")
    public Result<RoleVO> getRole(@PathVariable String id) {
        return Result.success(roleService.getRoleById(id));
    }

    /**
     * 创建角色
     * POST /api/v1/roles
     */
    @PostMapping
    public Result<RoleVO> createRole(@Validated @RequestBody RoleDTO roleDTO) {
        return Result.success("角色创建成功", roleService.createRole(roleDTO));
    }

    /**
     * 更新角色
     * POST /api/v1/roles/{id}
     */
    @PostMapping("/{id}")
    public Result<RoleVO> updateRole(@PathVariable String id, @Validated @RequestBody RoleDTO roleDTO) {
        return Result.success("角色更新成功", roleService.updateRole(id, roleDTO));
    }

    /**
     * 删除角色
     * DELETE /api/v1/roles/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return Result.success("角色删除成功");
    }

    /**
     * 批量删除角色
     * DELETE /api/v1/roles
     */
    @DeleteMapping
    public Result<String> batchDeleteRoles(@RequestParam List<String> ids) {
        // TODO: 实现批量删除逻辑
        return Result.success("批量删除成功");
    }
}