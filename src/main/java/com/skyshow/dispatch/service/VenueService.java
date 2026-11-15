package com.skyshow.dispatch.service;

import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.dto.venue.VenueCreateRequest;
import com.skyshow.dispatch.dto.venue.VenueUpdateRequest;
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
        // address 选填：归一化规则抽在 normalizeAddress，新增/修改共用，避免两处不一致
        venue.setAddress(normalizeAddress(request.address()));
        venue.setLng(request.lng());
        venue.setLat(request.lat());
        // enabled 随新增表单的开关提交；不传时兜底为启用（对齐前端开关默认打开）
        boolean enabledOnCreate = request.enabled() == null || request.enabled();
        venue.setEnabled(toEnabledFlag(enabledOnCreate));

        // 3. 交给数据访问层落库
        venueRepository.insert(venue);
    }

    /** 查询单个场地详情；不存在或已删除统一按 404 抛出 */
    public Venue getVenueDetail(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "场地不存在或已删除"));
    }

    /** 修改场地：先确认存在，再校验入参并把 DTO 搬上已受管的实体，最后落库 */
    public void updateVenue(Long id, VenueUpdateRequest request) {
        // 1. 存在性校验：id 不存在 / 已软删都拦下，避免静默更新 0 行
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "场地不存在或已删除"));

        // 2. 业务校验，规则与新增保持一致
        if (request.name() == null || request.name().isBlank()) {
            throw new BizException(400, "场地名称不能为空");
        }
        // 编辑表单的启用开关必传：缺失即视为非法请求，避免把启用态静默置空
        if (request.enabled() == null) {
            throw new BizException(400, "启用状态不能为空");
        }

        // 3. 在查出来的实体上改字段；id、时间戳等保持原值不动，enabled 随表单开关更新
        venue.setName(request.name());
        venue.setAddress(normalizeAddress(request.address()));
        venue.setLng(request.lng());
        venue.setLat(request.lat());
        venue.setEnabled(toEnabledFlag(request.enabled()));

        // 4. 落库
        venueRepository.update(venue);
    }

    /** 软删除场地：不存在 / 已删除返回 404，成功无返回体 */
    public void deleteVenue(Long id) {
        venueRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "场地不存在或已删除"));
        venueRepository.softDeleteById(id);
    }

    /** 地址选填：null 或纯空白统一归一空串（address 列为 NOT NULL，空串表达「未填写」） */
    private String normalizeAddress(String address) {
        return address == null || address.isBlank() ? "" : address.trim();
    }

    /** 前端布尔开关 → 数据库 TINYINT 启用标记：true=1 启用 / false=0 停用，转换规则全类收口于此 */
    private Integer toEnabledFlag(boolean enabled) {
        return enabled ? 1 : 0;
    }
}