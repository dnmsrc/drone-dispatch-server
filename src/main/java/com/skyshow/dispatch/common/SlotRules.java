package com.skyshow.dispatch.common;

import com.skyshow.dispatch.common.BizException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 档期时段规则（与前端 slot 纯函数同口径），集中收口避免各处实现分叉。
 *
 * <p>一个时段 = 一个整点区间，形如 {@code "19:00-20:00"}，分钟固定 00，结束晚于开始；
 * 首尾相接（19-20 与 20-21）合法，部分相交 / 完全相同算重叠。单日最多 12 段。
 */
public final class SlotRules {

    /** 整点区间：HH:00-HH:00，结束允许到 24:00 */
    private static final Pattern SLOT_PATTERN =
            Pattern.compile("^([01]\\d|2[0-3]):00-(([01]\\d|2[0-3]|24):00)$");

    /** 单日时段上限（对齐原型 MAX_SLOTS_PER_DAY） */
    public static final int MAX_SLOTS_PER_DAY = 12;

    private SlotRules() {
    }

    /** 校验并规整单个时段；非法返回 null（不抛异常，交给调用方决定提示文案） */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (!SLOT_PATTERN.matcher(s).matches()) {
            return null;
        }
        // 结束必须晚于开始（按小时比较，分钟恒为 00）
        int start = Integer.parseInt(s.substring(0, 2));
        int end = Integer.parseInt(s.substring(6, 8));
        return end > start ? s : null;
    }

    /**
     * 规整一组时段：逐个校验 → 去重 → 字典序排序；任一非法抛 400。
     * 不允许空列表、不允许超过上限、不允许自重叠。
     */
    public static List<String> normalizeList(List<String> rawSlots) {
        if (rawSlots == null || rawSlots.isEmpty()) {
            throw new BizException(400, "默认接单时段不能为空");
        }
        Set<String> dedup = new LinkedHashSet<>();
        for (String raw : rawSlots) {
            String slot = normalize(raw);
            if (slot == null) {
                throw new BizException(400,
                        "时段「" + raw + "」格式不正确：时段为整点区间，如 19:00-20:00");
            }
            dedup.add(slot);
        }
        List<String> slots = new ArrayList<>(dedup);
        slots.sort(String::compareTo);

        if (slots.size() > MAX_SLOTS_PER_DAY) {
            throw new BizException(400, "默认接单时段最多 " + MAX_SLOTS_PER_DAY + " 段");
        }
        List<String> overlap = findOverlap(slots);
        if (overlap != null) {
            throw new BizException(400,
                    "默认时段「" + overlap.get(0) + "」与「" + overlap.get(1)
                            + "」重叠：同一时间只能接一单，请改成不重叠的区间");
        }
        return slots;
    }

    /**
     * 找出第一对重叠时段；无重叠返回 null。排序后只比相邻段即可。
     * 首尾相接（前序结束 = 后序开始）不算重叠。
     */
    public static List<String> findOverlap(List<String> sortedSlots) {
        for (int i = 1; i < sortedSlots.size(); i++) {
            int[] a = range(sortedSlots.get(i - 1));
            int[] b = range(sortedSlots.get(i));
            if (Math.max(a[0], b[0]) < Math.min(a[1], b[1])) {
                return List.of(sortedSlots.get(i - 1), sortedSlots.get(i));
            }
        }
        return null;
    }

    /** 时段 → [开始分钟, 结束分钟] */
    private static int[] range(String slot) {
        int start = Integer.parseInt(slot.substring(0, 2)) * 60;
        int end = Integer.parseInt(slot.substring(6, 8)) * 60;
        return new int[] {start, end};
    }
}
