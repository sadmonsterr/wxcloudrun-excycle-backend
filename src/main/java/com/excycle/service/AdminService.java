package com.excycle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.entity.Admin;

public interface AdminService extends IService<Admin> {

    Admin login(String username, String password);

    Admin findByUsername(String username);
}