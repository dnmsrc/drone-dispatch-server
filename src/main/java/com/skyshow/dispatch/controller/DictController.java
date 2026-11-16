package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.common.Result;
import com.skyshow.dispatch.dto.dict.DictItemSaveRequest;
import com.skyshow.dispatch.dto.dict.DictTypeCreateRequest;
import com.skyshow.dispatch.dto.dict.DictTypeDetailVO;
import com.skyshow.dispatch.dto.dict.DictTypeSummaryVO;
import com.skyshow.dispatch.dto.dict.DictTypeUpdateRequest;
import com.skyshow.dispatch.service.DictService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口：字典类型 + 字典项两级 CRUD，共 8 个端点。
 * 读接口登录即可，写接口（POST/PUT/DELETE）在 Service 层限 ADMIN 角色。
 */
@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /* ───────────── 字典类型 ───────────── */

    /** ① 类型列表（汇总条目数/启用数） */
    @GetMapping("/types")
    public Result<List<DictTypeSummaryVO>> listTypes() {
        return Result.success(dictService.listTypes());
    }

    /** ② 新增自定义类型 */
    @PostMapping("/types")
    public Result<Void> createType(@RequestBody DictTypeCreateRequest request) {
        dictService.createType(request);
        return Result.success(null);
    }

    /** ③ 修改类型名/备注（dict_code 不可改） */
    @PutMapping("/types/{dictCode}")
    public Result<Void> updateType(@PathVariable String dictCode,
                                   @RequestBody DictTypeUpdateRequest request) {
        dictService.updateType(dictCode, request);
        return Result.success(null);
    }

    /** ④ 物理删除整组（内置类型 400 拦截，非内置级联删字典项） */
    @DeleteMapping("/types/{dictCode}")
    public Result<Void> deleteType(@PathVariable String dictCode) {
        dictService.deleteType(dictCode);
        return Result.success(null);
    }

    /* ───────────── 字典项 ───────────── */

    /** ⑤ 某类型详情 + 全部字典项 */
    @GetMapping("/{dictCode}/items")
    public Result<DictTypeDetailVO> typeDetail(@PathVariable String dictCode) {
        return Result.success(dictService.getTypeDetail(dictCode));
    }

    /** ⑥ 新增字典项 */
    @PostMapping("/{dictCode}/items")
    public Result<Void> createItem(@PathVariable String dictCode,
                                   @RequestBody DictItemSaveRequest request) {
        dictService.createItem(dictCode, request);
        return Result.success(null);
    }

    /** ⑦ 修改字典项（含启停，开关仅传 status 也走此端点） */
    @PutMapping("/{dictCode}/items/{id}")
    public Result<Void> updateItem(@PathVariable String dictCode,
                                   @PathVariable Long id,
                                   @RequestBody DictItemSaveRequest request) {
        dictService.updateItem(dictCode, id, request);
        return Result.success(null);
    }

    /** ⑧ 物理删除单个字典项 */
    @DeleteMapping("/{dictCode}/items/{id}")
    public Result<Void> deleteItem(@PathVariable String dictCode,
                                   @PathVariable Long id) {
        dictService.deleteItem(dictCode, id);
        return Result.success(null);
    }
}
