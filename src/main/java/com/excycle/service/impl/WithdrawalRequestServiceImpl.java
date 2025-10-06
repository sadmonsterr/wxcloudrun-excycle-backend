package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.dto.WithdrawalRequestQueryRequest;
import com.excycle.entity.WithdrawalRequest;
import com.excycle.mapper.UserMapper;
import com.excycle.mapper.WithdrawalRequestMapper;
import com.excycle.service.WithdrawalRequestService;
import com.excycle.vo.WithdrawalRequestVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class WithdrawalRequestServiceImpl extends ServiceImpl<WithdrawalRequestMapper, WithdrawalRequest> implements WithdrawalRequestService {

    private final UserMapper userMapper;

    public WithdrawalRequestServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Page<WithdrawalRequestVO> getWithdrawalRequestPage(WithdrawalRequestQueryRequest queryRequest) {
        Page<WithdrawalRequest> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());

        LambdaQueryWrapper<WithdrawalRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(isNotBlank(queryRequest.getUserId()), WithdrawalRequest::getUserId, queryRequest.getUserId());
        queryWrapper.eq(isNotBlank(queryRequest.getOpenId()), WithdrawalRequest::getOpenId, queryRequest.getOpenId());
        queryWrapper.eq(isNotBlank(queryRequest.getStatus()), WithdrawalRequest::getStatus, queryRequest.getStatus());
        queryWrapper.eq(isNotBlank(queryRequest.getRequestId()), WithdrawalRequest::getRequestId, queryRequest.getRequestId());
        queryWrapper.eq(isNotBlank(queryRequest.getThirdPartyOrderNo()), WithdrawalRequest::getThirdPartyOrderNo, queryRequest.getThirdPartyOrderNo());

        // 排序
        if (isNotBlank(queryRequest.getOrderBy())) {
            if (Boolean.TRUE.equals(queryRequest.getIsAsc())) {
                queryWrapper.orderByAsc(WithdrawalRequest::getCreatedAt);
            } else {
                queryWrapper.orderByDesc(WithdrawalRequest::getCreatedAt);
            }
        } else {
            queryWrapper.orderByDesc(WithdrawalRequest::getCreatedAt);
        }

        Page<WithdrawalRequest> withdrawalRequestPage = page(page, queryWrapper);

        // 转换为VO
        Page<WithdrawalRequestVO> voPage = new Page<>();
        BeanUtils.copyProperties(withdrawalRequestPage, voPage);
        voPage.setRecords(withdrawalRequestPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        updateUsernames(voPage.getRecords());
        return voPage;
    }

    @Override
    public WithdrawalRequestVO getWithdrawalRequestDetail(Long id) {
        WithdrawalRequest withdrawalRequest = getById(id);
        if (withdrawalRequest == null) {
            return null;
        }
        WithdrawalRequestVO vo = convertToVO(withdrawalRequest);
        updateUsernames(Collections.singletonList(vo));
        return vo;
    }

    private void updateUsernames(List<WithdrawalRequestVO> withdrawalRequestVOs) {
        List<String> openIds = withdrawalRequestVOs.stream()
                .map(WithdrawalRequestVO::getOpenId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> userNames = userMapper.queryUsernameByOpenIds(openIds);
        withdrawalRequestVOs.forEach(vo -> vo.setUsername(userNames.getOrDefault(vo.getOpenId(), vo.getOpenId())));
    }

    private WithdrawalRequestVO convertToVO(WithdrawalRequest withdrawalRequest) {
        WithdrawalRequestVO vo = new WithdrawalRequestVO();
        BeanUtils.copyProperties(withdrawalRequest, vo);

        // 设置状态描述
        if (withdrawalRequest.getStatus() != null) {
            WithdrawalRequest.Status statusEnum = WithdrawalRequest.Status.valueOf(withdrawalRequest.getStatus());
            vo.setStatusDescription(statusEnum.getDescription());
        }

        // 转换时间戳
        if (withdrawalRequest.getCreatedAt() != null) {
            vo.setCreatedAt(withdrawalRequest.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (withdrawalRequest.getUpdatedAt() != null) {
            vo.setUpdatedAt(withdrawalRequest.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (withdrawalRequest.getCompletedAt() != null) {
            vo.setCompletedAt(withdrawalRequest.getCompletedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        return vo;
    }
}