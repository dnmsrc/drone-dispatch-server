package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.common.Result;
import com.skyshow.dispatch.dto.payment.PaymentConfigUpdateRequest;
import com.skyshow.dispatch.dto.payment.PaymentConfigVO;
import com.skyshow.dispatch.service.PaymentConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付配置接口：本期仅支付时限。路径 /api/payment-config，不加 admin 前缀，
 * 写操作的 ADMIN 校验在 Service 层。渠道开关、收款账户待正式支付模块。
 */
@RestController
@RequestMapping("/api/payment-config")
public class PaymentConfigController {

    private final PaymentConfigService paymentConfigService;

    public PaymentConfigController(PaymentConfigService paymentConfigService) {
        this.paymentConfigService = paymentConfigService;
    }

    /** 读支付配置（登录即可） */
    @GetMapping
    public Result<PaymentConfigVO> getConfig() {
        return Result.success(paymentConfigService.getConfig());
    }

    /** 保存支付时限（仅 ADMIN） */
    @PutMapping
    public Result<Void> updateConfig(@RequestBody PaymentConfigUpdateRequest request) {
        paymentConfigService.updateConfig(request);
        return Result.success(null);
    }
}
