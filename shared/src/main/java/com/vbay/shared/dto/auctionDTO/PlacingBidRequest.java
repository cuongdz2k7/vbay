package com.vbay.shared.dto.auctionDTO;

import java.math.BigDecimal;

public class PlacingBidRequest {
    private String auctionId;
    private String bidderId;
    private BigDecimal bidAmount;

    public PlacingBidRequest(String auctionId, String bidderId, BigDecimal bidAmount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    @Override
    public String toString() {
        return "PlacingBidRequest{" +
            "auctionId='" + this.getAuctionId() + '\'' +
            ", bidderId='" + this.getBidderId() + '\'' +
            ", bidAmount=" + this.getBidAmount() +
            '}';
    }
}
