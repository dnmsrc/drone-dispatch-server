package com.skyshow.dispatch.dto.pricing;

import java.math.BigDecimal;

/**
 * 定制收费标量出参（定制文字价、LOGO 价），存于 sys_config。
 */
public record CustomFeeVO(
        BigDecimal customTextPrice,
        BigDecimal customLogoPrice) {
}
