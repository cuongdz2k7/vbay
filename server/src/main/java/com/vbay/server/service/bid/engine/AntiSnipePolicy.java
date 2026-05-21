package com.vbay.server.service.bid.engine;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import com.vbay.server.model.Auction;

public class AntiSnipePolicy {
    private final Duration window;
    private final Duration extension;

    public AntiSnipePolicy(Duration window, Duration extension) {
        this.window = window;
        this.extension = extension;
    }

    public Optional<LocalDateTime> resolveExtendedEndingTime(Auction auction, LocalDateTime bidTime) {
        LocalDateTime windowStart = auction.getEndingTime().minus(window);
        if (bidTime.isBefore(windowStart)) {
            return Optional.empty();
        }

        LocalDateTime extendedEndingTime = bidTime.plus(extension);
        if (!extendedEndingTime.isAfter(auction.getEndingTime())) {
            return Optional.empty();
        }

        return Optional.of(extendedEndingTime);
    }
}
