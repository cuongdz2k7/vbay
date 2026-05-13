package com.vbay.shared.dto.auctionDTO;

public class AuctionListRequest {
    private String status;
    private Long categoryId;
    private Long sellerId;
    private Integer limit;

    public AuctionListRequest() {
    }

    public AuctionListRequest(String status, Long categoryId, Long sellerId, Integer limit) {
        this.status = status;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.limit = limit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
