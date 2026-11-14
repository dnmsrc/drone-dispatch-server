package com.skyshow.dispatch.service;

import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.dto.venue.VenueCreateRequest;
import com.skyshow.dispatch.entity.Venue;
import com.skyshow.dispatch.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** 场地业务服务：目前只透传查询，后续业务规则在此扩展。 */
@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    /** 查询启用且未删除的场地列表 */
    public List<Venue> listEnabledVenues() {
        return venueRepository.findEnabledVenues();
    }

    /** 新增场地：校验入参 → DTO 转实体 → 交给 Repository 落库 */
    public void createVenue(VenueCreateRequest request) {
        // 1. 业务校验：仅场地名称必填；地址为选填，经纬度由前端地图选点保证
        if (request.name() == null || request.name().isBlank()) {
            throw new BizException(400, "场地名称不能为空");
        }

        // 2. DTO（4 格申请表）→ Venue 实体（10 格档案）
        Venue venue = new Venue();
        venue.setName(request.name());
        // address 选填：null 或纯空白统一归一空串。venues.address 列仍为 NOT NULL，
        // 用空串落库既满足约束，又表达「未填写地址」，避免为可空语义去做表结构迁移
        String address = request.address();
        venue.setAddress(address == null || address.isBlank() ? "" : address.trim());
        venue.setLng(request.lng());
        venue.setLat(request.lat());
        venue.setEnabled(1); // 业务规则：新增默认启用，放在 Service 而不是 SQL

        // 3. 交给数据访问层落库
        venueRepository.insert(venue);
    }
}