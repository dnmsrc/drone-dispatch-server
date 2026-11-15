package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.common.Result;
import com.skyshow.dispatch.dto.venue.VenueCreateRequest;
import com.skyshow.dispatch.dto.venue.VenueUpdateRequest;
import com.skyshow.dispatch.entity.Venue;
import com.skyshow.dispatch.service.VenueService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 场地接口：对外提供未删除场地列表（含启用/停用）。 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /** 查询全部未删除的场地列表（含启用/停用，停用态前端以 enabled 标记区分） */
    @GetMapping
    public Result<List<Venue>> list() {
        return Result.success(venueService.listVenues());
    }

    /** 新增场地 */
    @PostMapping
    public Result<Void> create(@RequestBody VenueCreateRequest request) {
        venueService.createVenue(request);
        return Result.success(null);
    }

    /** 查询单个场地详情；{id} 是路径变量，由 @PathVariable 取出 */
    @GetMapping("/{id}")
    public Result<Venue> detail(@PathVariable Long id) {
        return Result.success(venueService.getVenueDetail(id));
    }

    /** 修改场地：PUT /api/venues/5，id 走路径，新值走请求体 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody VenueUpdateRequest request) {
        venueService.updateVenue(id, request);
        return Result.success(null);
    }

    /** 软删除场地：数据保留，仅把 deleted 置 1 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return Result.success(null);
    }

}
