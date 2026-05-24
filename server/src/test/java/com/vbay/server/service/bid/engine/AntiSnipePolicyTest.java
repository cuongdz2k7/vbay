package com.vbay.server.service.bid.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.vbay.server.model.Auction;
import com.vbay.shared.enums.auction.AuctionStatus;

class AntiSnipePolicyTest {
    private final AntiSnipePolicy policy = new AntiSnipePolicy(
        Duration.ofMinutes(5),
        Duration.ofMinutes(5),
        5
    );

    @Test
    void resolveExtendedEndingTime_whenBidInsideWindow_resetsToBidTimePlusExtension() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);
        LocalDateTime endingTime = now.plusMinutes(4);
        Auction auction = auctionEndingAt(endingTime, 0);

        var result = policy.resolveExtendedEndingTime(auction, now);

        assertTrue(result.isPresent());
        assertEquals(now.plusMinutes(5), result.get());
    }

    @Test
    void resolveExtendedEndingTime_whenBidBeforeWindow_doesNotExtend() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);
        Auction auction = auctionEndingAt(now.plusMinutes(6), 0);

        var result = policy.resolveExtendedEndingTime(auction, now);

        assertTrue(result.isEmpty());
    }

    @Test
    void resolveExtendedEndingTime_whenMaxExtensionsReached_doesNotExtend() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 22, 10, 0);
        Auction auction = auctionEndingAt(now.plusMinutes(4), 5);

        var result = policy.resolveExtendedEndingTime(auction, now);

        assertTrue(result.isEmpty());
    }

    private Auction auctionEndingAt(LocalDateTime endingTime, int extensionCount) {
        return new Auction(
            1L,
            1L,
            1L,
            "Auction",
            "Description",
            new BigDecimal("1.00"),
            new BigDecimal("1.00"),
            null,
            null,
            new BigDecimal("1.00"),
            endingTime.minusHours(1),
            endingTime,
            AuctionStatus.ACTIVE,
            null,
            extensionCount
        );
    }
}
