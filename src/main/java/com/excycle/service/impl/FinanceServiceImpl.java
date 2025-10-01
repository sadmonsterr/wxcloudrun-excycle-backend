package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.excycle.entity.UserWallet;
import com.excycle.mapper.UserWalletMapper;
import com.excycle.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class FinanceServiceImpl implements FinanceService {

    private final UserWalletMapper walletMapper;

    public FinanceServiceImpl(UserWalletMapper walletMapper) {
        this.walletMapper = walletMapper;
    }

    @Override
    @Transactional
    public void transfer(String userId, Double amount) {
        LambdaQueryWrapper<UserWallet> queryWrapper = new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId);
        UserWallet wallet = walletMapper.selectOne(queryWrapper);
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.valueOf(amount));
            wallet.setFrozenBalance(BigDecimal.ZERO);
            walletMapper.insert(wallet);
        } else {
            LambdaQueryWrapper<UserWallet> updateQueryWrapper = new LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId)
                    .eq(UserWallet::getVersion, wallet.getVersion());
            wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(amount)));
            wallet.setVersion(wallet.getVersion() + 1);
            int updated = walletMapper.update(wallet, updateQueryWrapper);
            if (updated == 0) {
                throw new RuntimeException("更新失败, 请稍后再试");
            }
        }
    }
}
