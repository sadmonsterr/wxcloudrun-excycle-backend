package com.excycle.service;


import com.excycle.entity.UserWallet;

import java.util.Map;

public interface FinanceService {

    UserWallet getUserWallet(String openId);

    void transfer(String userId, String openId, Double amount);

    Map<String, Object> withdraw(String userId, Double amount);

}
