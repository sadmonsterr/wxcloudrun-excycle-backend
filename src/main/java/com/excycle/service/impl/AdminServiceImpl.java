package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.entity.Admin;
import com.excycle.mapper.AdminMapper;
import com.excycle.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin login(String username, String password) {
        if (!username.equals("admin") || !password.equals("admin@123")) {
            return null;
        }
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername(username);
        return admin;
    }

    @Override
    public Admin findByUsername(String username) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }
}