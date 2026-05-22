package com.vbay.server.service.bid.engine;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import com.vbay.server.model.Auction;

public class AntiSnipePolicy {
    private static final int DEFAULT_MAX_EXTENSIONS = 5;

    private final Duration window;
    private final Duration extension;
    private final int maxExtensions;

    public AntiSnipePolicy() {
        this(Duration.ofMinutes(5), Duration.ofMinutes(5), DEFAULT_MAX_EXTENSIONS);
    }

    public AntiSnipePolicy(Duration window, Duration extension) {
        this(window, extension, DEFAULT_MAX_EXTENSIONS);
    }

    public AntiSnipePolicy(Duration window, Duration extension, int maxExtensions) {
        this.window = window;
        this.extension = extension;
        this.maxExtensions = maxExtensions;
    }

    public Optional<LocalDateTime> resolveExtendedEndingTime(Auction auction, LocalDateTime bidTime) {
        if (auction.getAntiSnipeExtensionCount() >= maxExtensions) {
            return Optional.empty();
        }

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
