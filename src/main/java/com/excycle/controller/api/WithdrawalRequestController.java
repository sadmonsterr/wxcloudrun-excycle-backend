package com.excycle.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.context.UserContext;
import com.excycle.dto.WithdrawalRequestQueryRequest;
import com.excycle.service.WithdrawalRequestService;
import com.excycle.vo.WithdrawalRequestVO;
import com.excycle.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/withdrawal-requests")
public class WithdrawalRequestController {

    @Autowired
    private WithdrawalRequestService withdrawalRequestService;

    /**
     * 获取提现请求列表
     * POST /api/v1/withdrawal-requests/list
     */
    @PostMapping("/list")
    public Result<Page<WithdrawalRequestVO>> getWithdrawalRequestList(@RequestBody WithdrawalRequestQueryRequest queryRequest) {
        // 如果是普通用户，只能查看自己的提现记录
        String currentUserId = UserContext.getCurrentUserId();
        String currentOpenId = UserContext.getCurrentOpenId();

        if (currentUserId != null && !currentUserId.isEmpty()) {
            queryRequest.setUserId(currentUserId);
        } else if (currentOpenId != null && !currentOpenId.isEmpty()) {
            queryRequest.setOpenId(currentOpenId);
        }

        Page<WithdrawalRequestVO> page = withdrawalRequestService.getWithdrawalRequestPage(queryRequest);
        return Result.success(page);
    }

    /**
     * 获取提现请求详情
     * GET /api/v1/withdrawal-requests/{id}
     */
    @GetMapping("/{id}")
    public Result<WithdrawalRequestVO> getWithdrawalRequestDetail(@PathVariable Long id) {
        WithdrawalRequestVO detail = withdrawalRequestService.getWithdrawalRequestDetail(id);
        if (detail == null) {
            return Result.error("提现请求不存在");
        }

        // 权限检查：普通用户只能查看自己的提现记录
        String currentUserId = UserContext.getCurrentUserId();
        String currentOpenId = UserContext.getCurrentOpenId();

        if (currentUserId != null && !currentUserId.isEmpty() &&
                !currentUserId.equals(detail.getUserId())) {
            return Result.error("无权限查看此提现请求");
        }

        if (currentOpenId != null && !currentOpenId.isEmpty() &&
                !currentOpenId.equals(detail.getOpenId())) {
            return Result.error("无权限查看此提现请求");
        }

        return Result.success(detail);
    }

    /**
     * 获取当前用户的提现请求列表
     * GET /api/v1/withdrawal-requests/my
     */
    @GetMapping("/my")
    public Result<Page<WithdrawalRequestVO>> getMyWithdrawalRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            WithdrawalRequestQueryRequest queryRequest = new WithdrawalRequestQueryRequest();
            queryRequest.setPage(page);
            queryRequest.setSize(size);
            queryRequest.setStatus(status);

            String currentUserId = UserContext.getCurrentUserId();
            String currentOpenId = UserContext.getCurrentOpenId();

            if (currentUserId != null && !currentUserId.isEmpty()) {
                queryRequest.setUserId(currentUserId);
            } else if (currentOpenId != null && !currentOpenId.isEmpty()) {
                queryRequest.setOpenId(currentOpenId);
            } else {
                return Result.error("用户未登录");
            }

            Page<WithdrawalRequestVO> result = withdrawalRequestService.getWithdrawalRequestPage(queryRequest);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取我的提现请求列表失败", e);
            return Result.error("获取我的提现请求列表失败：" + e.getMessage());
        }
    }
}