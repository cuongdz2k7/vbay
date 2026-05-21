package com.vbay.server.service.bid.resolution.model.bid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vbay.server.service.bid.resolution.model.BalanceChange;
import com.vbay.server.service.bid.resolution.model.PaymentCreate;
import com.vbay.server.service.bid.resolution.model.auction.AuctionChange;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidCreate;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidMaxBidUpdate;
import com.vbay.server.service.bid.resolution.model.autobid.AutobidStatusChange;
import com.vbay.shared.enums.bid.BidSource;

public class BidResolution {
    private final boolean accepted;
    private final String message;
    private final long auctionId;

    private final List<BalanceChange> balanceChanges;

    private final List<BidCreate> bidCreates;
    private final List<BidStatusUpdate> bidStatusUpdates;
    private final List<BidFinalization> bidFinalizations;

    private final List<AutobidCreate> autobidCreates;
    private final List<AutobidStatusChange> autobidStatusChanges;
    private final List<AutobidMaxBidUpdate> autobidMaxBidUpdates;

    private final List<AuctionChange> auctionChanges;
    private final List<PaymentCreate> paymentCreates;

    private BidResolution(Builder builder) {
        this.accepted = builder.accepted;
        this.message = builder.message;
        this.auctionId = builder.auctionId;
        
        this.balanceChanges = List.copyOf(builder.balanceChanges);

        this.bidCreates = List.copyOf(builder.bidCreates);
        this.bidStatusUpdates = List.copyOf(builder.bidStatusUpdates);
        this.bidFinalizations = List.copyOf(builder.bidFinalizations);

        this.autobidCreates = List.copyOf(builder.autobidCreates);
        this.autobidStatusChanges = List.copyOf(builder.autobidStatusChanges);
        this.autobidMaxBidUpdates = List.copyOf(builder.autobidMaxBidUpdates);

        this.auctionChanges = List.copyOf(builder.auctionChanges);
        this.paymentCreates = List.copyOf(builder.paymentCreates);
    }

    public static Builder accepted(long auctionId) {
        return new Builder(true, null, auctionId);
    }

    public static Builder rejected(long auctionId, String message) {
        return new Builder(false, message, auctionId);
    }

    public boolean isAccepted() { return accepted; }
    public String getMessage() { return message; }
    public long getAuctionId() { return auctionId; }

    public List<BalanceChange> getBalanceChanges() { return balanceChanges; }

    public List<BidCreate> getBidCreates() { return bidCreates; }
    public List<BidStatusUpdate> getBidStatusUpdates() { return bidStatusUpdates; }
    public List<BidFinalization> getBidFinalizations() { return bidFinalizations; }

    public List<AutobidCreate> getAutobidCreates() { return autobidCreates; }
    public List<AutobidStatusChange> getAutobidStatusChanges() { return autobidStatusChanges; }
    public List<AutobidMaxBidUpdate> getAutobidMaxBidUpdates() { return autobidMaxBidUpdates; }

    public List<AuctionChange> getAuctionChanges() { return auctionChanges; }
    public List<PaymentCreate> getPaymentCreates() { return paymentCreates; }

    public Optional<BidCreate> findBidCreateBySource(BidSource source) {
        return bidCreates.stream()
            .filter(bidCreate -> bidCreate.getSource() == source)
            .findFirst();
    }

    public static class Builder {
        private final boolean accepted;
        private final String message;
        private final long auctionId;

        private final List<BalanceChange> balanceChanges = new ArrayList<>();

        private final List<BidCreate> bidCreates = new ArrayList<>();
        private final List<BidStatusUpdate> bidStatusUpdates = new ArrayList<>();
        private final List<BidFinalization> bidFinalizations = new ArrayList<>();

        private final List<AutobidCreate> autobidCreates = new ArrayList<>();
        private final List<AutobidStatusChange> autobidStatusChanges = new ArrayList<>();
        private final List<AutobidMaxBidUpdate> autobidMaxBidUpdates = new ArrayList<>();

        private final List<AuctionChange> auctionChanges = new ArrayList<>();
        private final List<PaymentCreate> paymentCreates = new ArrayList<>();

        private Builder(boolean accepted, String message, long auctionId) {
            this.accepted = accepted;
            this.message = message;
            this.auctionId = auctionId;
        }

        public Builder addBalanceChange(BalanceChange balanceChange) {
            this.balanceChanges.add(balanceChange);
            return this;
        }

        public Builder addBalanceChanges(List<BalanceChange> balanceChanges) {
            this.balanceChanges.addAll(balanceChanges);
            return this;
        }

        public Builder addBidCreate(BidCreate bidCreate) {
            this.bidCreates.add(bidCreate);
            return this;
        }

        public Builder addBidCreates(List<BidCreate> bidCreates) {
            this.bidCreates.addAll(bidCreates);
            return this;
        }

        public Builder addBidStatusUpdate(BidStatusUpdate bidStatusUpdate) {
            this.bidStatusUpdates.add(bidStatusUpdate);
            return this;
        }

        public Builder addBidStatusUpdates(List<BidStatusUpdate> bidStatusUpdates) {
            this.bidStatusUpdates.addAll(bidStatusUpdates);
            return this;
        }

        public Builder addBidFinalization(BidFinalization bidFinalization) {
            this.bidFinalizations.add(bidFinalization);
            return this;
        }

        public Builder addBidFinalizations(List<BidFinalization> bidFinalizations) {
            this.bidFinalizations.addAll(bidFinalizations);
            return this;
        }

        public Builder addAutobidCreate(AutobidCreate autobidCreate) {
            this.autobidCreates.add(autobidCreate);
            return this;
        }

        public Builder addAutobidCreates(List<AutobidCreate> autobidCreates) {
            this.autobidCreates.addAll(autobidCreates);
            return this;
        }

        public Builder addAutobidStatusChange(AutobidStatusChange autobidStatusChange) {
            this.autobidStatusChanges.add(autobidStatusChange);
            return this;
        }

        public Builder addAutobidStatusChanges(List<AutobidStatusChange> autobidStatusChanges) {
            this.autobidStatusChanges.addAll(autobidStatusChanges);
            return this;
        }

        public Builder addAutobidMaxBidUpdate(AutobidMaxBidUpdate autobidMaxBidUpdate) {
            this.autobidMaxBidUpdates.add(autobidMaxBidUpdate);
            return this;
        }

        public Builder addAutobidMaxBidUpdates(List<AutobidMaxBidUpdate> autobidMaxBidUpdates) {
            this.autobidMaxBidUpdates.addAll(autobidMaxBidUpdates);
            return this;
        }

        public Builder addAuctionChange(AuctionChange auctionChange) {
            this.auctionChanges.add(auctionChange);
            return this;
        }

        public Builder addAuctionChanges(List<AuctionChange> auctionChanges) {
            this.auctionChanges.addAll(auctionChanges);
            return this;
        }

        public Builder addPaymentCreate(PaymentCreate paymentCreate) {
            this.paymentCreates.add(paymentCreate);
            return this;
        }

        public Builder addPaymentCreates(List<PaymentCreate> paymentCreates) {
            this.paymentCreates.addAll(paymentCreates);
            return this;
        }

        public BidResolution build() {
            return new BidResolution(this);
        }
    }
}
