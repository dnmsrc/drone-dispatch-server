package com.skyshow.dispatch.service;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.common.ConfigKeys;
import com.skyshow.dispatch.common.SlotRules;
import com.skyshow.dispatch.dto.schedule.DayClosedRequest;
import com.skyshow.dispatch.dto.schedule.DaySlotsRequest;
import com.skyshow.dispatch.dto.schedule.GlobalScheduleUpdateRequest;
import com.skyshow.dispatch.dto.schedule.GlobalScheduleVO;
import com.skyshow.dispatch.dto.schedule.ScheduleCellVO;
import com.skyshow.dispatch.dto.schedule.ScheduleMonthVO;
import com.skyshow.dispatch.entity.ScheduleDay;
import com.skyshow.dispatch.repository.ScheduleDayRepository;
import com.skyshow.dispatch.repository.SysConfigRepository;
import com.skyshow.dispatch.security.LoginUser;
import com.skyshow.dispatch.security.LoginUserHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 档期业务服务：全局默认时段 / 预约窗口、按月日历、单日覆盖、openSlots 服务端派生。
 *
 * <p>两层模型：全局默认存 sys_config（schedule.*），单日覆盖存 schedule_days；
 * 某日无覆盖行则回退全局默认。容量 = 时段数，已订满时段在 openSlots 中过滤。
 * 权限：读登录即可，写仅 ADMIN。
 */
@Service
public class ScheduleService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** 预约窗口约束与缺省值 */
    private static final int MIN_WINDOW = 7;
    private static final int MAX_WINDOW = 365;
    private static final int DEFAULT_WINDOW = 60;
    /** 单次查询区间跨度上限：月视图 31 天够用，季/年视图由调用方分片，同时挡住超量查询 */
    private static final int MAX_RANGE_DAYS = 62;
    /** 全局默认时段缺省值（sys_config 未配置时兜底） */
    private static final List<String> DEFAULT_SLOTS = List.of("19:00-20:00", "20:00-21:00", "21:00-22:00");

    private final ScheduleDayRepository dayRepository;
    private final SysConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    public ScheduleService(ScheduleDayRepository dayRepository,
                           SysConfigRepository configRepository,
                           ObjectMapper objectMapper) {
        this.dayRepository = dayRepository;
        this.configRepository = configRepository;
        this.objectMapper = objectMapper;
    }

    /* ═══════════════════ 全局默认设置 ═══════════════════ */

    /** 读全局档期设置（默认时段 + 预约窗口，缺失给兜底） */
    public GlobalScheduleVO getGlobal() {
        return new GlobalScheduleVO(readDefaultSlots(), readBookingWindow());
    }

    /** 保存全局设置，传哪个改哪个；仅 ADMIN */
    public void updateGlobal(GlobalScheduleUpdateRequest request) {
        requireAdmin();
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (request.defaultSlots() != null) {
            // 复用统一时段规则：整点、去重、排序、非空、≤12、不可重叠
            List<String> slots = SlotRules.normalizeList(request.defaultSlots());
            configRepository.upsertValue(ConfigKeys.SCHEDULE_DEFAULT_SLOTS, toJson(slots));
        }
        if (request.bookingWindowDays() != null) {
            int window = request.bookingWindowDays();
            if (window < MIN_WINDOW || window > MAX_WINDOW) {
                throw new BizException(400, "预约窗口需在 7-365 天之间");
            }
            configRepository.upsertValue(ConfigKeys.SCHEDULE_BOOKING_WINDOW, String.valueOf(window));
        }
    }

    /* ═══════════════════ 月历 / 单日视图 ═══════════════════ */

    /** 装配 [from, to] 每天的档期单元格（含 openSlots 派生），区间跨度受限 */
    public ScheduleMonthVO getMonth(LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<String> defaultSlots = readDefaultSlots();
        Map<LocalDate, ScheduleDay> overrides = new LinkedHashMap<>();
        for (ScheduleDay day : dayRepository.findBetween(from, to)) {
            overrides.put(day.getScheduleDate(), day);
        }
        // 一次聚合拿到区间内全部已订数，避免逐日逐时段 N+1 查询（31 天 × 3 段 = 93 条 SQL）
        Map<LocalDate, Map<String, Integer>> bookedByDate = new HashMap<>();
        for (ScheduleDayRepository.BookedSlotCount c : dayRepository.listBookedCounts(from, to)) {
            bookedByDate.computeIfAbsent(c.date(), k -> new LinkedHashMap<>()).put(c.slot(), c.count());
        }

        List<ScheduleCellVO> cells = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            cells.add(buildCell(d, overrides.get(d), defaultSlots,
                    bookedByDate.getOrDefault(d, Map.of())));
        }
        return new ScheduleMonthVO(cells, defaultSlots, readBookingWindow());
    }

    /**
     * H5 匿名只读区间：只允许今天起、预约窗口内的档期。
     * 防止匿名调用方拉超长区间（@public 无鉴权，不限区间等于公开的数据库放大器）。
     */
    public ScheduleMonthVO getPublicMonth(LocalDate from, LocalDate to) {
        validateRange(from, to);
        LocalDate today = LocalDate.now();
        int window = readBookingWindow();
        if (from.isBefore(today)) {
            throw new BizException(400, "只能查询今天及以后的档期");
        }
        if (to.isAfter(today.plusDays(window))) {
            throw new BizException(400, "只能查询未来 " + window + " 天内的档期");
        }
        return getMonth(from, to);
    }

    /** 单日视图（H5 下单选时段用），含 openSlots */
    public ScheduleCellVO getDay(LocalDate date) {
        List<String> defaultSlots = readDefaultSlots();
        ScheduleDay override = dayRepository.findByDate(date).orElse(null);
        Map<String, Integer> booked = new HashMap<>();
        for (ScheduleDayRepository.BookedSlotCount c : dayRepository.listBookedCounts(date, date)) {
            booked.put(c.slot(), c.count());
        }
        return buildCell(date, override, defaultSlots, booked);
    }

    /* ═══════════════════ 单日写操作（仅 ADMIN）═══════════════════ */

    /** 配置单日时段；slots=null 恢复默认，空列表=当日不接单 */
    public void saveDaySlots(LocalDate date, DaySlotsRequest request) {
        requireAdmin();
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }
        ScheduleDay existing = dayRepository.findByDate(date).orElseGet(() -> newDay(date));
        if (request.slots() == null) {
            // 恢复默认：删除覆盖行（若该日还有休整标记则保留行、仅清 slots）
            if (existing.getId() == null) {
                return;
            }
            if (existing.getClosed() != null && existing.getClosed() == 1) {
                dayRepository.upsertDay(date, 1, null, trimToEmpty(request.note(), existing.getNote()));
            } else {
                dayRepository.deleteByDate(date);
            }
            return;
        }
        // 显式时段：空数组合法（不接单）；非空则走统一规则校验
        String slotsJson;
        if (request.slots().isEmpty()) {
            slotsJson = toJson(List.of());
        } else {
            slotsJson = toJson(SlotRules.normalizeList(request.slots()));
        }
        // 配时段的日子若仍是休整日，自动恢复营业，避免语义冲突
        dayRepository.upsertDay(date, 0, slotsJson, trimToEmpty(request.note(), existing.getNote()));
    }

    /** 设置 / 取消休整日 */
    public void setClosed(LocalDate date, DayClosedRequest request) {
        requireAdmin();
        if (request == null || request.closed() == null) {
            throw new BizException(400, "休整标记不能为空");
        }
        ScheduleDay existing = dayRepository.findByDate(date).orElseGet(() -> newDay(date));
        int closed = request.closed() ? 1 : 0;
        String note = request.note() != null ? request.note().trim() : existing.getNote();
        dayRepository.upsertDay(date, closed, existing.getSlots(), note);
    }

    /* ═══════════════════ 内部装配 ═══════════════════ */

    /** 区间合法性：顺序正确且跨度不超过上限（超长区间 = 超量查询，直接 400 挡在入口） */
    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BizException(400, "日期区间非法");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new BizException(400, "查询区间跨度过长，最多 " + MAX_RANGE_DAYS + " 天");
        }
    }

    /** 组装单日单元格：决定 slots、closed，再服务端派生 bookedSlots / openSlots / limit */
    private ScheduleCellVO buildCell(LocalDate date, ScheduleDay override, List<String> defaultSlots,
                                     Map<String, Integer> bookedCounts) {
        boolean closed = override != null && override.getClosed() != null && override.getClosed() == 1;
        boolean slotsOverridden = override != null && override.getSlots() != null;

        List<String> slots;
        if (closed) {
            // 休整日：不开放任何时段
            slots = List.of();
        } else if (override != null && override.getSlots() != null) {
            // 显式覆盖：null 才回退默认，空数组即不接单
            slots = parseSlots(override.getSlots());
        } else {
            slots = defaultSlots;
        }

        // 已订数由调用方一次聚合传入（不再逐时段查库），派生出可预约时段（订满 ≥1 即过滤，一个时段一个名额）
        Map<String, Integer> bookedSlots = new LinkedHashMap<>();
        List<String> openSlots = new ArrayList<>();
        for (String slot : slots) {
            int booked = bookedCounts.getOrDefault(slot, 0);
            bookedSlots.put(slot, booked);
            if (booked < 1) {
                openSlots.add(slot);
            }
        }

        String note = override != null ? trimToEmpty(override.getNote(), "") : "";
        return new ScheduleCellVO(
                date.format(DATE_FMT),
                slots,
                openSlots,
                bookedSlots,
                slots.size(),
                closed,
                slotsOverridden,
                note);
    }

    private ScheduleDay newDay(LocalDate date) {
        ScheduleDay day = new ScheduleDay();
        day.setScheduleDate(date);
        day.setClosed(0);
        return day;
    }

    /* ═══════════════════ sys_config 读写 ═══════════════════ */

    private List<String> readDefaultSlots() {
        return configRepository.findValue(ConfigKeys.SCHEDULE_DEFAULT_SLOTS)
                .map(this::parseSlots)
                .orElse(DEFAULT_SLOTS);
    }

    private int readBookingWindow() {
        return configRepository.findValue(ConfigKeys.SCHEDULE_BOOKING_WINDOW)
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .orElse(DEFAULT_WINDOW);
    }

    private List<String> parseSlots(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> list = objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
            return list == null ? List.of() : list;
        } catch (StreamReadException e) {
            throw new BizException(500, "档期时段配置数据格式错误");
        }
    }

    private String toJson(List<String> slots) {
        try {
            return objectMapper.writeValueAsString(slots);
        } catch (StreamWriteException e) {
            throw new BizException(500, "档期时段序列化失败");
        }
    }

    /* ═══════════════════ 工具 ═══════════════════ */

    /** 解析 YYYY-MM-DD，非法统一 400 */
    public static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DATE_FMT);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new BizException(400, "日期格式错误，应为 YYYY-MM-DD");
        }
    }

    private String trimToEmpty(String s, String fallback) {
        if (s != null) {
            return s.trim();
        }
        return fallback == null ? "" : fallback;
    }

    private void requireAdmin() {
        LoginUser user = LoginUserHolder.get();
        if (user == null) {
            throw new BizException(401, "未登录或登录已失效，请重新登录");
        }
        if (!ROLE_ADMIN.equals(user.getRole())) {
            throw new BizException(403, "无权限：仅管理员可修改档期配置");
        }
    }
}
