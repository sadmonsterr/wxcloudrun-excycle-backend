package com.excycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.excycle.context.UserContext;
import com.excycle.entity.UserWallet;
import com.excycle.entity.WithdrawalRequest;
import com.excycle.mapper.UserWalletMapper;
import com.excycle.mapper.WithdrawalRequestMapper;
import com.excycle.service.FinanceService;
import com.excycle.utils.UUIDUtils;
import com.excycle.utils.WXPayUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FinanceServiceImpl implements FinanceService {

    private final UserWalletMapper walletMapper;

    private final WithdrawalRequestMapper withdrawalRequestMapper;

    private static final String mchId = System.getenv("MCH_ID");

    private static final String apiClientCertificateSerialNo = System.getenv("API_CLIENT_SERIAL_NO");

    private static final String apiClientPrivateKeyString = System.getenv("API_CLIENT_PRIVATE_KEY_STRING");

    private static final String wechatPayPublicKeyId = System.getenv("WECHAT_PAY_PUBLIC_KEY_ID");

    private static final String wechatPayPublicKeyString = System.getenv("WECHAT_PAY_PUBLIC_KEY_STRING");

    static {
        log.info("mchId {}", mchId);

        log.info("apiClientCertificateSerialNo {}", apiClientCertificateSerialNo);

        log.info("apiClientPrivateKeyString {}", apiClientPrivateKeyString);

        log.info("wechatPayPublicKeyId {}", wechatPayPublicKeyId);

        log.info("wechatPayPublicKeyString {}", wechatPayPublicKeyString);
    }

    private static final TransferToUser client = new TransferToUser(
            mchId,                    // 商户号，是由微信支付系统生成并分配给每个商户的唯一标识符，商户号获取方式参考 https://pay.weixin.qq.com/doc/v3/merchant/4013070756
            apiClientCertificateSerialNo,         // 商户API证书序列号，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013053053
            WXPayUtility.loadPrivateKeyFromString(apiClientPrivateKeyString),     // 商户API证书私钥文件路径，本地文件路径
            wechatPayPublicKeyId,      // 微信支付公钥ID，如何获取请参考 https://pay.weixin.qq.com/doc/v3/merchant/4013038816
            WXPayUtility.loadPublicKeyFromString(wechatPayPublicKeyString)           // 微信支付公钥文件路径，本地文件路径
    );

    public FinanceServiceImpl(UserWalletMapper walletMapper, WithdrawalRequestMapper withdrawalRequestMapper) {
        this.walletMapper = walletMapper;
        this.withdrawalRequestMapper = withdrawalRequestMapper;
    }

    @Override
    public UserWallet getUserWallet(String openId) {
        return getOrCreateUserWallet(openId);
    }

    private UserWallet getOrCreateUserWallet(String openId) {
        LambdaQueryWrapper<UserWallet> queryWrapper = new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getOpenId, openId);
        UserWallet wallet = walletMapper.selectOne(queryWrapper);
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenBalance(BigDecimal.ZERO);
            wallet.setUserId(UserContext.getCurrentUserId());
            wallet.setVersion(0);
            wallet.setOpenId(openId);
            wallet.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            wallet.setUpdatedAt(wallet.getCreatedAt());
            walletMapper.insert(wallet);
        }
        return wallet;
    }

    @Override
    @Transactional
    public void transfer(String userId, String openId, Double amount) {
        LambdaQueryWrapper<UserWallet> queryWrapper = new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId);
        UserWallet wallet = walletMapper.selectOne(queryWrapper);
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.valueOf(amount));
            wallet.setFrozenBalance(BigDecimal.ZERO);
            wallet.setOpenId(openId);
            wallet.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            wallet.setUpdatedAt(wallet.getCreatedAt());
            walletMapper.insert(wallet);
        } else {
            LambdaQueryWrapper<UserWallet> updateQueryWrapper = new LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId)
                    .eq(UserWallet::getVersion, wallet.getVersion());
            wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(amount)));
            wallet.setVersion(wallet.getVersion() + 1);
            wallet.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            int updated = walletMapper.update(wallet, updateQueryWrapper);
            if (updated == 0) {
                throw new RuntimeException("更新失败, 请稍后再试");
            }
        }
    }

    @Override
    @Transactional
    public Map<String, Object> withdraw(String userId, Double amount) {
        UserWallet wallet = getOrCreateUserWallet(UserContext.getCurrentOpenId());
        if (wallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new RuntimeException("余额不足");
        }
        LambdaQueryWrapper<UserWallet> updateQueryWrapper = new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .eq(UserWallet::getVersion, wallet.getVersion());
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(amount)));
        wallet.setFrozenBalance(wallet.getFrozenBalance().add(BigDecimal.valueOf(amount)));
        wallet.setVersion(wallet.getVersion() + 1);
        wallet.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        int updated = walletMapper.update(wallet, updateQueryWrapper);
        if (updated == 0) {
            throw new RuntimeException("余额有更新, 请刷新后重试");
        }
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest()
                .setRequestId(UUIDUtils.nextBase62SnowflakeId())
                .setUserId(userId)
                .setAmount(BigDecimal.valueOf(amount))
                .setStatus(WithdrawalRequest.Status.PENDING.getCode())
                .setOpenId(UserContext.getCurrentOpenId())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());
        withdrawalRequestMapper.insert(withdrawalRequest);

        TransferToUser.TransferToUserResponse response = client.transfer(BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(100)).longValue(),
                UserContext.getCurrentOpenId(), withdrawalRequest.getRequestId());

        withdrawalRequest.setThirdPartyOrderNo(response.getTransferBillNo());
        withdrawalRequestMapper.updateById(withdrawalRequest);
        Map<String, Object> result = new HashMap<>();
        result.put("packageInfo", response.getPackageInfo());
        return result;
    }
}
