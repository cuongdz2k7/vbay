package com.vbay.shared.dto.auctionDTO;

/*
sẽ ra sao nếu bằng 1 cách nào đấy hacker bypass đưuọc client rule và gửi request ? 
*/
public class BuyNowRequest {
    private long auctionId;

    public BuyNowRequest(long auctionId) {
        this.auctionId = auctionId;
    }

    public long getAuctionId() {
        return auctionId;
    }
}
