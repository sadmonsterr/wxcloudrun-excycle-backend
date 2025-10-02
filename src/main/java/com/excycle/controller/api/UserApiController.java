package com.excycle.controller.api;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.context.UserContext;
import com.excycle.dto.WithdrawDTO;
import com.excycle.entity.User;
import com.excycle.entity.UserWallet;
import com.excycle.service.FinanceService;
import com.excycle.vo.AddressVO;
import com.excycle.vo.UserVO;
import com.excycle.service.UserService;
import com.excycle.common.Result;
import com.excycle.dto.UserQueryRequest;
import com.excycle.dto.UserRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理RESTful API
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private FinanceService financeService;

    /**
     * 获取用户列表
     * POST /api/v1/users/list
     */
    @PostMapping("/list")
    public Result<Page<UserVO>> getUsers(@RequestBody UserQueryRequest request) {

        User queryUser = new User();
        if (request.getName() != null) queryUser.setName(request.getName());
        if (request.getPhone() != null) queryUser.setPhone(request.getPhone());
        if (request.getRole() != null) queryUser.setRole(request.getRole());

        Page<User> userPage = userService.getUserPage(new Page<>(request.getPage(), request.getSize()), queryUser);
        // 转换为VO
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setCurrent(userPage.getCurrent());
        userVOPage.setSize(userPage.getSize());
        userVOPage.setTotal(userPage.getTotal());
        userVOPage.setRecords(userPage.getRecords().stream().map(this::convertToUserVO).collect(Collectors.toList()));
        return Result.success(userVOPage);
    }

    @GetMapping("driver")
    public Result<Page<UserVO>> getDriverUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String search) {
        Page<User> userPage = userService.getDriverUserPage(new Page<>(page, size), search);
        // 转换为VO
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setCurrent(userPage.getCurrent());
        userVOPage.setSize(userPage.getSize());
        userVOPage.setTotal(userPage.getTotal());
        userVOPage.setRecords(userPage.getRecords().stream().map(this::convertToSimpleDriverUserVO).collect(Collectors.toList()));
        return Result.success(userVOPage);
    }

    private UserVO convertToSimpleDriverUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setName(String.format("%s(%s)", user.getName(), user.getPhone()));
        return userVO;
    }
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setPhone(user.getPhone());
        userVO.setName(user.getName());
        userVO.setCompany(user.getCompany());
        userVO.setShop(user.getShop());
        userVO.setRole(user.getRole());
        userVO.setBusinessLicense(user.getBusinessLicense());
        userVO.setCreatedAt(user.getCreatedAt());
        userVO.setUpdatedAt(user.getUpdatedAt());
        return userVO;
    }

    /**
     * 根据ID获取用户
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable String id) {
        User user = userService.getById(id);
        if (user != null) {
            return Result.success(convertToUserVO(user));
        } else {
            return Result.error("用户不存在");
        }
    }

    /**
     * 根据OpenID获取用户
     * GET /api/v1/users/{id}
     */
    @GetMapping("/open")
    public Result<UserVO> getUserById() {
        String openId = UserContext.getCurrentOpenId();
        UserVO user = userService.getByOpenId(openId);
        return Result.success(user);
    }

    /**
     * 根据OpenID获取用户地址
     * GET /api/v1/users/{id}
     */
    @GetMapping("/address")
    public Result<Page<AddressVO>> queryAddresses() {
        String openId = UserContext.getCurrentOpenId();
        return Result.success(userService.queryAddresses(openId));
    }

    @GetMapping("/wallet")
    public Result<UserWallet> getUserWallet() {
        Assert.notNull(UserContext.getCurrentUserId(), "用户未注册");
        String openId = UserContext.getCurrentOpenId();
        return Result.success(userService.getUserWallet(openId));
    }

    @PostMapping("/withdraw")
    public Result<Map<String, Object>> withdraw(@RequestBody @Validated WithdrawDTO withdrawDTO) {
        Assert.notNull(UserContext.getCurrentUserId(), "用户未注册");
        Map<String, Object> result = financeService.withdraw(UserContext.getCurrentUserId(), withdrawDTO.getAmount());
        return Result.success(result);
    }

    /**
     * 用户注册
     * POST /api/v1/users/register
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody UserRegisterRequest registerRequest) {
        User user = userService.registerWithAddress(registerRequest);
        return Result.success("注册成功", user.getId());
    }

    /**
     * 创建用户
     * POST /api/v1/users
     */
    @PostMapping
    public Result<String> createUser(@RequestBody User user) {
        boolean success = userService.addUser(user);
        if (success) {
            return Result.success("用户创建成功", user.getId());
        } else {
            return Result.error("用户创建失败");
        }
    }

    /**
     * 更新用户
     * POST /api/v1/users/{id}
     */
    @PostMapping("/{id}")
    public Result<String> updateUser(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        boolean success = userService.updateUser(user);
        if (success) {
            return Result.success("用户更新成功", id);
        } else {
            return Result.error("用户更新失败");
        }
    }

    /**
     * 删除用户
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        if (success) {
            return Result.success("用户删除成功");
        } else {
            return Result.error("用户删除失败");
        }
    }

    /**
     * 批量删除用户
     * DELETE /api/v1/users?ids=1,2,3
     */
    @DeleteMapping
    public Result<String> batchDeleteUsers(@RequestParam List<Long> ids) {
        boolean success = userService.removeByIds(ids);
        if (success) {
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }

    /**
     * 获取用户总数
     * GET /api/v1/users/count
     */
    @GetMapping("/count")
    public Result<Long> getUserCount() {
        long count = userService.count();
        return Result.success(count);
    }
}