package com.skyshow.dispatch.service;

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
}