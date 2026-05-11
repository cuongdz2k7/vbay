package com.vbay.server.realtime.mapper;

import com.vbay.server.realtime.domain.AuctionCreatedDomainEvent;
import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.CreateAuctionResult;
import com.vbay.server.service.result.PlaceBidResult;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionEndedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionStateUpdatedPayload;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import com.vbay.shared.enums.auction.BidStatus;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.RealtimeEvent;

public class RealtimeEventMapper {
    public RealtimeEvent<AuctionStateUpdatedPayload> toAuctionStateUpdatedEvent(BidUpdatedDomainEvent event) {
        PlaceBidResult result = event.getResult();
        AuctionStateUpdatedPayload payload = new AuctionStateUpdatedPayload();
        payload.setAuctionId(result.getAuctionId());
        payload.setAuctionVersion(result.getAuctionVersion());
        payload.setCurrentPrice(result.getCurrentPrice());
        payload.setNextMinimumBid(result.getNextMinimumBid());
        payload.setWinnerUserId(result.getBidderId());
        payload.setUpdatedAt(result.getBidTime());

        RealtimeEvent<AuctionStateUpdatedPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_STATE_UPDATED,
            auctionRoom(result.getAuctionId()),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<BidHistoryItemPayload> toBidHistoryItemAddedEvent(BidUpdatedDomainEvent event) {
        PlaceBidResult result = event.getResult();
        BidHistoryItemPayload payload = new BidHistoryItemPayload(
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getBidId(),
            result.getBidderId(),
            null,
            result.getBidAmount(),
            result.getBidStatus().name(),
            result.getBidSource().name(),
            result.getBidTime()
        );

        RealtimeEvent<BidHistoryItemPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.BID_HISTORY_ITEM_ADDED,
            auctionRoom(result.getAuctionId()),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<AuctionListItemPayload> toAuctionCreatedListItemEvent(AuctionCreatedDomainEvent event) {
        return toAuctionListItemUpdatedEvent(event);
    }

    public RealtimeEvent<AuctionListItemPayload> toAuctionListItemUpdatedEvent(AuctionCreatedDomainEvent event) {
        CreateAuctionResult result = event.getResult();
        AuctionListItemPayload payload = new AuctionListItemPayload(
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getSellerId(),
            result.getTitle(),
            result.getCategoryId(),
            result.getStatus().name(),
            result.getCurrentPrice(),
            null,
            result.getStartingTime(),
            result.getEndingTime(),
            event.occurredAt()
        );

        RealtimeEvent<AuctionListItemPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_LIST_ITEM_UPDATED,
            auctionListRoom(),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<AuctionEndedPayload> toAuctionEndedEvent(BuyNowDomainEvent event) {
        BuyNowResult result = event.getResult();
        AuctionEndedPayload payload = new AuctionEndedPayload(
            result.getAuctionId(),
            result.getAuctionVersion(),
            "ENDED",
            result.getFinalPrice(),
            result.getBuyerId(),
            result.getBoughtAt(),
            "BUY_NOW"
        );

        RealtimeEvent<AuctionEndedPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_ENDED,
            auctionRoom(result.getAuctionId()),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<BidHistoryItemPayload> toBuyNowHistoryItemEvent(BuyNowDomainEvent event) {
        BuyNowResult result = event.getResult();
        BidHistoryItemPayload payload = new BidHistoryItemPayload(
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getBidId(),
            result.getBuyerId(),
            null,
            result.getFinalPrice(),
            BidStatus.WON.name(),
            BidSource.USER_BID.name(),
            result.getBoughtAt()
        );

        RealtimeEvent<BidHistoryItemPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.BID_HISTORY_ITEM_ADDED,
            auctionRoom(result.getAuctionId()),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    private Room auctionRoom(long auctionId) {
        Room room = new Room();
        room.setType(RoomType.AUCTION);
        room.setTargetId(auctionId);
        return room;
    }

    private Room auctionListRoom() {
        Room room = new Room();
        room.setType(RoomType.AUCTION_LIST);
        return room;
    }
}
