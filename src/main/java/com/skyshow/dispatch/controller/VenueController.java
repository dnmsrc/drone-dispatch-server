package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.common.Result;
import com.skyshow.dispatch.dto.venue.VenueCreateRequest;
import com.skyshow.dispatch.entity.Venue;
import com.skyshow.dispatch.service.VenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 场地接口：对外提供启用场地列表。 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /** 查询启用且未删除的场地列表 */
    @GetMapping
    public Result<List<Venue>> list() {
        return Result.success(venueService.listEnabledVenues());
    }

    /** 新增场地 */
    @PostMapping
    public Result<Void> create(@RequestBody VenueCreateRequest request) {
        venueService.createVenue(request);
        return Result.success(null);
    }

}
