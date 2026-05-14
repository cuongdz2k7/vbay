package com.vbay.server.realtime.mapper;

import com.vbay.server.realtime.domain.AuctionListItemUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BidUpdatedDomainEvent;
import com.vbay.server.realtime.domain.BuyNowDomainEvent;
import com.vbay.server.realtime.domain.UserBalanceUpdatedDomainEvent;
import com.vbay.server.service.result.BuyNowResult;
import com.vbay.server.service.result.PlaceBidResult;
import com.vbay.server.service.result.UserBalanceResult;
import com.vbay.server.service.result.mapper.ResultMapper;
import com.vbay.shared.dto.realtimeDTO.Room;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.AuctionStatePayload;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.MyBidListItemPayload;
import com.vbay.shared.dto.realtimeDTO.payload.UserBalanceUpdatedPayload;
import com.vbay.shared.enums.auction.BidStatus;
import com.vbay.shared.enums.bid.BidSource;
import com.vbay.shared.enums.realtime.AuctionStateChangeReason;
import com.vbay.shared.enums.realtime.RealtimeEventType;
import com.vbay.shared.enums.realtime.RoomType;
import com.vbay.shared.protocol.RealtimeEvent;

public class RealtimeEventMapper {
    public RealtimeEvent<AuctionStatePayload> toAuctionStateEvent(BidUpdatedDomainEvent event) {
        PlaceBidResult result = event.getResult();
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
    public RealtimeEvent<MyBidListItemPayload> toMyBidListItemUpdatedEvent(BidUpdatedDomainEvent event) {
        PlaceBidResult result = event.getResult();
        MyBidListItemPayload payload = new MyBidListItemPayload(
            result.getBidId(),
            result.getAuctionId(),
            result.getAuctionVersion(),
            result.getAuctionTitle(),
            null,
            result.getCurrentPrice(),
            result.getAuctionStatus(),
            result.getBidAmount(),
            result.getBidStatus(),
            result.getBidSource(),
            result.getBidTime(),
            result.getStartingTime(),
            result.getEndingTime(),
            result.getBidTime()
        );

        RealtimeEvent<MyBidListItemPayload> realtimeEvent = new RealtimeEvent<>(
            RealtimeEventType.MY_BID_LIST_ITEM_UPDATED,
            userRoom(result.getBidderId()),
            payload
        );  
        realtimeEvent.setOccurredAt(event.occurredAt());
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
