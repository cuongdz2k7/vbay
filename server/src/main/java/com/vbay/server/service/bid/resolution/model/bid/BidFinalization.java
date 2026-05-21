package com.vbay.server.service.bid.resolution.model.bid;
import java.sql.SQLException;

import com.vbay.server.repository.BidRepository;

public interface BidFinalization {
    void apply(BidRepository bidRepository, BidResolution resolution) throws SQLException;
}