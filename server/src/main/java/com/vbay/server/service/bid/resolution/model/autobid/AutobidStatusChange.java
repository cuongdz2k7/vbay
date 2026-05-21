package com.vbay.server.service.bid.resolution.model.autobid;

import com.vbay.server.service.bid.autobid.enums.AutobidStatus;

public class AutobidStatusChange {
    private final long autobidId;
    private final AutobidStatus status;

    public AutobidStatusChange(long autobidId, AutobidStatus status) {
        this.autobidId = autobidId;
        this.status = status;
    }

    public long getAutobidId() { return autobidId; }
    public AutobidStatus getStatus() { return status; }
}