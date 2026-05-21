package com.vbay.server.realtime.mapper;

import com.vbay.server.realtime.domain.AuctionClosedDomainEvent;
import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.AuctionStartedDomainEvent;
import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.realtime.domain.enums.AuctionCloseReason;
import com.vbay.server.service.result.AuctionClosedResult;
import com.vbay.server.service.result.AuctionListItemResult;
import com.vbay.server.service.result.BidUpdateResult;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.UserMyBidListItemResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionStatePayload;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserBalanceUpdatedPayload;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.bid.BidStatus;
import com.vbay.shared.enums.realtime.AuctionStateChangeReason;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.RealtimeEvent;

public class RealtimeEventMapper {
    public RealtimeEvent<AuctionStatePayload> toAuctionStateEvent(BidUpdatedDomainEvent event) {
        BidUpdateResult result = event.getResult();
        AuctionStatePayload payload = new AuctionStatePayload();
        payload.setAuctionId(result.getAuctionId());
        payload.setAuctionVersion(result.getAuctionVersion());
        payload.setCurrentPrice(result.getCurrentPrice());
        payload.setReserveMet(result.getReserveMet());
        payload.setWinnerUserId(result.getBidderId());
        payload.setUpdatedAt(result.getBidTime());
        payload.setStateChangeReason(AuctionStateChangeReason.BID_PLACED);

        RealtimeEvent<AuctionStatePayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_STATE_UPDATED,
            auctionRoom(result.getAuctionId()),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<BidHistoryItemPayload> toBidHistoryItemAddedEvent(BidUpdatedDomainEvent event) {
        BidUpdateResult result = event.getResult();
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
    public RealtimeEvent<MyBidListItemPayload> toMyBidListItemUpdatedEvent(UserMyBidListItemResult result) {
        MyBidListItemPayload payload = new MyBidListItemPayload(
            result.getBidId(),
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getAuctionTitle(),
            result.getThumbnailUrl(),
            result.getCurrentPrice(),
            result.getAuctionStatus().name(),
            result.getMyBidAmount(),
            result.getBidStatus(),
            result.getBidSource(),
            result.getBidTime(),
            result.getStartingTime(),
            result.getEndingTime(),
            result.getUpdatedAt()
        );

        RealtimeEvent<MyBidListItemPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.MY_BID_LIST_ITEM_UPDATED,
            userRoom(result.getUserId()),
            payload
        );  
        realtimeEvent.setOccurredAt(result.getUpdatedAt());
        return realtimeEvent;
    }

    public RealtimeEvent<AuctionListItemPayload> toAuctionListItemUpdatedEvent(AuctionListItemUpdatedDomainEvent event) {
        RealtimeEvent<AuctionListItemPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_LIST_ITEM_UPDATED,
            auctionListRoom(),
            ResultMapper.toAuctionListItemPayload(event.getAuctionListItem())
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<AuctionStatePayload> toAuctionStateEvent(AuctionStartedDomainEvent event) {
        AuctionListItemResult result = event.getAuction();
        AuctionStatePayload payload = new AuctionStatePayload();
        payload.setAuctionId(result.getAuctionId());
        payload.setAuctionVersion(result.getAuctionVersion());
        payload.setStatus(result.getStatus());
        payload.setCurrentPrice(result.getCurrentPrice());
        payload.setReserveMet(result.getReserveMet());
        payload.setWinnerUserId(result.getWinnerUserId());
        payload.setStartingTime(result.getStartingTime());
        payload.setEndingTime(result.getEndingTime());
        payload.setUpdatedAt(result.getUpdatedAt());
        payload.setStateChangeReason(AuctionStateChangeReason.STARTED);

        RealtimeEvent<AuctionStatePayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_STATE_UPDATED,
            auctionRoom(result.getAuctionId()),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<AuctionStatePayload> toAuctionStateEvent(AuctionClosedDomainEvent event) {
        AuctionClosedResult result = event.getResult();
        AuctionStatePayload payload = new AuctionStatePayload();
        payload.setAuctionId(result.getAuctionId());
        payload.setAuctionVersion(result.getAuctionVersion());
        payload.setStatus(result.getAuctionStatus().name());
        payload.setCurrentPrice(result.getCurrentPrice());
        payload.setReserveMet(result.getReserveMet());
        payload.setWinnerUserId(result.getWinnerUserId());
        payload.setStartingTime(result.getStartingTime());
        payload.setEndingTime(result.getEndingTime());
        payload.setUpdatedAt(result.getClosedAt());
        payload.setEndedAt(result.getClosedAt());
        payload.setStateChangeReason(toAuctionStateChangeReason(result.getReason()));

        RealtimeEvent<AuctionStatePayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_STATE_UPDATED,
            auctionRoom(result.getAuctionId()),
            payload
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    public RealtimeEvent<AuctionStatePayload> toAuctionStateEvent(BuyNowDomainEvent event) {
        BuyNowResult result = event.getResult();
        AuctionStatePayload payload = new AuctionStatePayload();
        payload.setAuctionId(result.getAuctionId());
        payload.setAuctionVersion(result.getAuctionVersion());
        payload.setStatus("ENDED");
        payload.setCurrentPrice(result.getFinalPrice());
        payload.setWinnerUserId(result.getBuyerId());
        payload.setUpdatedAt(result.getBoughtAt());
        payload.setEndedAt(result.getBoughtAt());
        payload.setStateChangeReason(AuctionStateChangeReason.BUY_NOW);
        payload.setReserveMet(true);

        RealtimeEvent<AuctionStatePayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.AUCTION_STATE_UPDATED,
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
            BidSource.BUY_NOW.name(),
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

    public RealtimeEvent<UserBalanceUpdatedPayload> toUserBalanceUpdatedEvent(UserBalanceUpdatedDomainEvent event) {
        UserBalanceResult result = event.getResult();
        RealtimeEvent<UserBalanceUpdatedPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.USER_BALANCE_UPDATED,
            userRoom(result.getUserId()),
            ResultMapper.toUserBalanceUpdatedPayload(result)
        );
        realtimeEvent.setOccurredAt(event.occurredAt());
        return realtimeEvent;
    }

    private AuctionStateChangeReason toAuctionStateChangeReason(AuctionCloseReason reason) {
        return switch (reason) {
            case TIME_EXPIRED_ENDED -> AuctionStateChangeReason.TIME_EXPIRED_ENDED;
            case TIME_EXPIRED_FAILED -> AuctionStateChangeReason.TIME_EXPIRED_FAILED;
        };
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

    private Room userRoom(long userId) {
        Room room = new Room();
        room.setType(RoomType.USER);
        room.setTargetId(userId);
        return room;
    }
}
