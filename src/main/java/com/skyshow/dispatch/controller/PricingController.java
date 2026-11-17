package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.common.Result;
import com.skyshow.dispatch.dto.pricing.CustomFeeRequest;
import com.skyshow.dispatch.dto.pricing.CustomFeeVO;
import com.skyshow.dispatch.dto.pricing.GroupStatusVO;
import com.skyshow.dispatch.dto.pricing.GroupSwitchUpdateRequest;
import com.skyshow.dispatch.dto.pricing.PricingGroupSaveRequest;
import com.skyshow.dispatch.dto.pricing.PricingGroupVO;
import com.skyshow.dispatch.dto.pricing.PricingPublicVO;
import com.skyshow.dispatch.dto.pricing.PricingRuleUpdateRequest;
import com.skyshow.dispatch.dto.pricing.RefundRuleVO;
import com.skyshow.dispatch.service.PricingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 计价接口：管理端维护 5 组计价规则与定制标量，H5 只读启用项。
 *
 * <p>读接口登录即可；写接口（PUT）在 Service 层限 ADMIN 角色。路径不加 /admin 前缀，
 * 与 dict/venue 风格保持一致，权限靠登录角色而非 URL 前缀区分。
 */
@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /* ═══════════════════ 管理端：分组规则 ═══════════════════ */

    /** ① 全部分组视图（含停用项，管理端计价页用） */
    @GetMapping("/groups")
    public Result<List<PricingGroupVO>> listGroups() {
        return Result.success(pricingService.listAllGroups());
    }

    /** ② 单个分组视图（含停用项） */
    @GetMapping("/groups/{group}")
    public Result<PricingGroupVO> getGroup(@PathVariable String group) {
        return Result.success(pricingService.getGroup(group));
    }

    /** ③ 整组保存（清空重建，sort 按列表顺序）；仅 ADMIN */
    @PutMapping("/groups/{group}")
    public Result<Void> saveGroup(@PathVariable String group,
                                  @RequestBody PricingGroupSaveRequest request) {
        pricingService.replaceGroup(group, request);
        return Result.success(null);
    }

    /** ④ 单项编辑 / 启停（开关仅传 status 即可）；仅 ADMIN */
    @PutMapping("/rules/{id}")
    public Result<Void> updateRule(@PathVariable Long id,
                                   @RequestBody PricingRuleUpdateRequest request) {
        pricingService.updateRule(id, request);
        return Result.success(null);
    }

    /** ④.1 分组总闸启停（整组在 H5 是否可选，不改组内各行 status）；仅 ADMIN */
    @PutMapping("/groups/{group}/enabled")
    public Result<Void> updateGroupSwitch(@PathVariable String group,
                                          @RequestBody GroupSwitchUpdateRequest request) {
        pricingService.updateGroupSwitch(group, request);
        return Result.success(null);
    }

    /* ═══════════════════ 管理端：定制收费标量 ═══════════════════ */

    /** ⑤ 读定制文字价 / LOGO 价 */
    @GetMapping("/custom")
    public Result<CustomFeeVO> getCustomFees() {
        return Result.success(pricingService.getCustomFees());
    }

    /** ⑥ 保存定制标量（传哪个改哪个）；仅 ADMIN */
    @PutMapping("/custom")
    public Result<Void> saveCustomFees(@RequestBody CustomFeeRequest request) {
        pricingService.saveCustomFees(request);
        return Result.success(null);
    }

    /* ═══════════════════ H5：只读启用视图 ═══════════════════ */

    /** ⑦.0 H5 分组总闸状态：H5 先拉本接口，仅对 enabled=true 的组再请求计价数据（匿名可访问） */
    @GetMapping("/group-status")
    public Result<List<GroupStatusVO>> groupStatus() {
        return Result.success(pricingService.listGroupStatus());
    }

    /** ⑦ H5 下单计价规则（仅启用项，字段对齐原型 priceRules，不含时段） */
    @GetMapping("/public")
    public Result<PricingPublicVO> publicPricing() {
        return Result.success(pricingService.getPublicPricing());
    }

    /** ⑧ H5 退款阶梯（仅启用项，按距服务小时数降序） */
    @GetMapping("/refund-rules")
    public Result<List<RefundRuleVO>> refundRules() {
        return Result.success(pricingService.getRefundRules());
    }
}
