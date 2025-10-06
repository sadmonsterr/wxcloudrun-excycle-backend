package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.dto.WithdrawalRequestQueryRequest;
import com.excycle.entity.WithdrawalRequest;
import com.excycle.vo.WithdrawalRequestVO;

public interface WithdrawalRequestService extends IService<WithdrawalRequest> {

    Page<WithdrawalRequestVO> getWithdrawalRequestPage(WithdrawalRequestQueryRequest queryRequest);

    WithdrawalRequestVO getWithdrawalRequestDetail(Long id);
}