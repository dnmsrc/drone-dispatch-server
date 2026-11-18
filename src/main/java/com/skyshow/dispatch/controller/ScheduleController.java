package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.common.Result;
import com.skyshow.dispatch.dto.schedule.DayClosedRequest;
import com.skyshow.dispatch.dto.schedule.DaySlotsRequest;
import com.skyshow.dispatch.dto.schedule.GlobalScheduleUpdateRequest;
import com.skyshow.dispatch.dto.schedule.GlobalScheduleVO;
import com.skyshow.dispatch.dto.schedule.ScheduleCellVO;
import com.skyshow.dispatch.dto.schedule.ScheduleMonthVO;
import com.skyshow.dispatch.dto.schedule.SaveSlotsResult;
import com.skyshow.dispatch.service.ScheduleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 档期接口：全局默认设置 + 月历/单日视图 + 单日覆盖。
 *
 * <p>管理端写操作在 Service 层限 ADMIN；H5 匿名只读走 {@code /public/**}（拦截器白名单）。
 * 路径不加 admin 前缀，与 dict/venue/pricing 风格一致。
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /* ═══════════════════ 管理端：全局默认设置 ═══════════════════ */

    /** ① 读全局默认时段 + 预约窗口（登录即可） */
    @GetMapping("/global")
    public Result<GlobalScheduleVO> getGlobal() {
        return Result.success(scheduleService.getGlobal());
    }

    /** ② 保存全局设置（仅 ADMIN，字段传哪个改哪个） */
    @PutMapping("/global")
    public Result<Void> updateGlobal(@RequestBody GlobalScheduleUpdateRequest request) {
        scheduleService.updateGlobal(request);
        return Result.success(null);
    }

    /* ═══════════════════ 管理端：月历与单日配置 ═══════════════════ */

    /** ③ 按月（或区间）查日历，含 openSlots 派生，登录即可 */
    @GetMapping("/month")
    public Result<ScheduleMonthVO> month(@RequestParam int year, @RequestParam int month) {
        // 非法年月直接 400；否则 LocalDate.of 抛 DateTimeException 会被兜底成 500「系统繁忙」
        if (month < 1 || month > 12) {
            throw new BizException(400, "月份需在 1-12 之间");
        }
        if (year < 1970 || year > 2200) {
            throw new BizException(400, "年份需在 1970-2200 之间");
        }
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return Result.success(scheduleService.getMonth(from, to));
    }

    /** ④ 单日详情（管理端） */
    @GetMapping("/days/{date}")
    public Result<ScheduleCellVO> dayDetail(@PathVariable String date) {
        return Result.success(scheduleService.getDay(ScheduleService.parseDate(date)));
    }

    /** ⑤ 配置单日时段（仅 ADMIN；slots 传 null 恢复默认、空数组=不接单）；
     *  已订时段会被强制保留并在 blocked 中回报，重叠则 400 */
    @PutMapping("/days/{date}/slots")
    public Result<SaveSlotsResult> saveDaySlots(@PathVariable String date,
                                                @RequestBody DaySlotsRequest request) {
        return Result.success(scheduleService.saveDaySlots(ScheduleService.parseDate(date), request));
    }

    /** ⑥ 设置 / 取消休整日（仅 ADMIN） */
    @PutMapping("/days/{date}/closed")
    public Result<Void> setClosed(@PathVariable String date,
                                  @RequestBody DayClosedRequest request) {
        scheduleService.setClosed(ScheduleService.parseDate(date), request);
        return Result.success(null);
    }

    /* ═══════════════════ H5：匿名只读 ═══════════════════ */

    /** ⑦ H5 拉取区间档期（from/to 为 YYYY-MM-DD），只暴露派生后的可预约信息，限今天起 + 预约窗口内 */
    @GetMapping("/public")
    public Result<ScheduleMonthVO> publicMonth(@RequestParam String from,
                                               @RequestParam String to) {
        return Result.success(scheduleService.getPublicMonth(
                ScheduleService.parseDate(from), ScheduleService.parseDate(to)));
    }
}
