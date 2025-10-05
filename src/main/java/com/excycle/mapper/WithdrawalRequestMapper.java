package com.excycle.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.excycle.entity.WithdrawalRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WithdrawalRequestMapper extends BaseMapper<WithdrawalRequest> {

    default WithdrawalRequest getByRequestId(String requestId) {
        return selectOne(new LambdaQueryWrapper<WithdrawalRequest>().eq(WithdrawalRequest::getRequestId, requestId));
    }

    default WithdrawalRequest getByThirdPartyOrderNo(String thirdPartyOrderNo) {
        return selectOne(new LambdaQueryWrapper<WithdrawalRequest>().eq(WithdrawalRequest::getThirdPartyOrderNo, thirdPartyOrderNo));
    }
}
