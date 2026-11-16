package com.skyshow.dispatch.service;

import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.common.ConfigKeys;
import com.skyshow.dispatch.dto.payment.PaymentConfigUpdateRequest;
import com.skyshow.dispatch.dto.payment.PaymentConfigVO;
import com.skyshow.dispatch.repository.SysConfigRepository;
import com.skyshow.dispatch.security.LoginUser;
import com.skyshow.dispatch.security.LoginUserHolder;
import org.springframework.stereotype.Service;

/**
 * 支付配置服务：本期只管「支付时限」一个标量。
 *
 * <p>边界：不含任何计价字段；在线支付渠道开关、对公收款账户待正式支付模块再开放。
 * 权限：读登录即可，写仅 ADMIN。
 */
@Service
public class PaymentConfigService {

    private static final String ROLE_ADMIN = "ADMIN";

    /** 支付时限约束（对齐原型）与缺省值 */
    private static final int MIN_EXPIRE = 1;
    private static final int MAX_EXPIRE = 120;
    private static final int DEFAULT_EXPIRE = 15;

    private final SysConfigRepository configRepository;

    public PaymentConfigService(SysConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    /** 读支付时限（sys_config 缺失或非法时给默认值 15 分钟） */
    public PaymentConfigVO getConfig() {
        return new PaymentConfigVO(readExpire());
    }

    /** 保存支付时限，仅 ADMIN；范围 1-120 */
    public void updateConfig(PaymentConfigUpdateRequest request) {
        requireAdmin();
        if (request == null || request.payExpireMinutes() == null) {
            throw new BizException(400, "支付时限不能为空");
        }
        int minutes = request.payExpireMinutes();
        if (minutes < MIN_EXPIRE || minutes > MAX_EXPIRE) {
            throw new BizException(400, "支付时限需在 1-120 分钟之间");
        }
        configRepository.upsertValue(ConfigKeys.PAY_EXPIRE_MINUTES, String.valueOf(minutes));
    }

    /** 供订单等其它服务读取当前支付时限（造单计算 expire_at 用），不暴露给 Controller 之外的写能力 */
    public int currentPayExpireMinutes() {
        return readExpire();
    }

    private int readExpire() {
        return configRepository.findValue(ConfigKeys.PAY_EXPIRE_MINUTES)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .orElse(DEFAULT_EXPIRE);
    }

    /** 写操作角色闸门：非 ADMIN 一律 403 */
    private void requireAdmin() {
        LoginUser user = LoginUserHolder.get();
        if (user == null) {
            throw new BizException(401, "未登录或登录已失效，请重新登录");
        }
        if (!ROLE_ADMIN.equals(user.getRole())) {
            throw new BizException(403, "无权限：仅管理员可修改支付配置");
        }
    }
}
