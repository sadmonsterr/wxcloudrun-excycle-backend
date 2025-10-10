package com.excycle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.entity.User;
import com.excycle.service.UserService;
import com.excycle.common.Result;
import com.excycle.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public String listUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           User user, Model model) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<UserVO> userPage = userService.getUserPage(page, user);

        model.addAttribute("userPage", userPage);
        model.addAttribute("user", user);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("activeMenu", "user");

        return "user/list";
    }

    @GetMapping("/add")
    public String addUserPage() {
        return "user/add";
    }

    @GetMapping("/edit/{id}")
    public String editUserPage(@PathVariable String id, Model model) {
        UserVO user = userService.getById(id);
        model.addAttribute("user", user);
        return "user/edit";
    }

    @PostMapping("/add")
    @ResponseBody
    public Result<User> addUser(@RequestBody User user) {
        boolean success = userService.addUser(user);
        if (success) {
            return Result.success("用户添加成功", user);
        } else {
            return Result.error("用户添加失败");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public Result<User> updateUser(@RequestBody User user) {
        boolean success = userService.updateUser(user);
        if (success) {
            return Result.success("用户更新成功", user);
        } else {
            return Result.error("用户更新失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        if (success) {
            return Result.success("用户删除成功");
        } else {
            return Result.error("用户删除失败");
        }
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("用户不存在");
        }
    }

    @GetMapping("/batchDelete")
    @ResponseBody
    public Result<String> batchDelete(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        List<Long> idList = Arrays.asList(idArray).stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        boolean success = userService.removeByIds(idList);
        if (success) {
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }
}