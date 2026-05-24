package com.vbay.server.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.server.model.Auction;
import com.vbay.server.realtime.domain.enums.AuctionListItemUpdateReason;
import com.vbay.server.service.AuctionService;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.enums.auction.AuctionStatus;




/*
Chịu trách nhiệm chính:

giữ ScheduledExecutorService
giữ Map<Long, ScheduledFuture<?>> startTasks
giữ Map<Long, ScheduledFuture<?>> endTasks
scheduleAuction(auction)
rescheduleAuction(auction)
cancelAuction(auctionId)
start()
shutdown()


syncAuctionStatus: PHẢI IDEMPOTENT


*/
public class AuctionTaskScheduler {
    private static final Logger LOGGER = LoggingUtils.getLogger(AuctionTaskScheduler.class);
    private final AuctionService auctionService;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    
    ///danh sách auctions và thời điểm thực thi auctions đó
    private final Map<Long, ScheduledFuture<?>> startTasks = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> endTasks = new ConcurrentHashMap<>();

    private ScheduledFuture<?> recoveryFuture;


    public AuctionTaskScheduler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void start() {
        try {
            List<Auction> auctions = auctionService.findPendingSchedules();
            for (Auction auction : auctions) {
                rescheduleAuction(auction);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to register pending auction schedules", e);
        }
        ///schedule atfixed rate cứ 30s lại refresh 1 lần để đảm bảo không bị miss auction nào cả
        recoveryFuture = executor.scheduleAtFixedRate(
            this::recoverMissedAuctions,
            0,
            30,
            TimeUnit.SECONDS
        );
    }

     private void runEndTask(long auctionId) {
        try {
            auctionService.syncAuctionStatus(auctionId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running end task for auction: " + auctionId, e);
        } finally {
            endTasks.remove(auctionId);
            startTasks.remove(auctionId);
        }
    }

    private void scheduleEnd (Auction auction, LocalDateTime now) {
        long delayMillis = Duration.between(now, auction.getEndingTime()).toMillis();
        if (delayMillis <= 0) {
            runEndTask(auction.getId());
            return;
        }
        ScheduledFuture<?> future = executor.schedule(
            () -> runEndTask(auction.getId()),
            delayMillis,
            TimeUnit.MILLISECONDS
        );
        endTasks.put(auction.getId(), future);
    }

    private void runStartTask(long auctionId) {
        try {
            auctionService.syncAuctionStatus(auctionId); 

            Auction refreshed = auctionService.findAuctionById(auctionId);
            if (refreshed != null && refreshed.getStatus() == AuctionStatus.ACTIVE) {
                scheduleEnd(refreshed, auctionService.getDatabaseTime());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to run start task for auction " + auctionId, e);
        } finally {
            startTasks.remove(auctionId);
        }
    }

    void scheduleStart (Auction auction, LocalDateTime now) {
        long delayMillis = Duration.between(now, auction.getStartingTime()).toMillis();
        if (delayMillis <= 0) {
            /*
            server restart
            recovery
            load task từ DB
            seller đổi giờ về quá khứ
            */
            runStartTask(auction.getId());
            return;
        } 
        ScheduledFuture<?> future = executor.schedule(
            () -> runStartTask(auction.getId()),
            delayMillis,
            TimeUnit.MILLISECONDS
        );
        startTasks.put(auction.getId(), future);
    }

    public void cancelAuction(long auctionId) {
        ScheduledFuture<?> startFuture = startTasks.remove(auctionId);
        if (startFuture != null) {
            startFuture.cancel(false);
        }

        ScheduledFuture<?> endFuture = endTasks.remove(auctionId);
        if (endFuture != null) {
            endFuture.cancel(false);
        }
    }

    public void cancelAuction(Auction auction) {
        if (auction != null) {
            cancelAuction(auction.getId());
        }
    }


    public void rescheduleAuction(Auction auction) {
        cancelAuction(auction);
        try {
            LocalDateTime now = auctionService.getDatabaseTime();

            if (auction.getStatus() == AuctionStatus.SCHEDULED) {
                scheduleStart(auction, now);
            }

            if (auction.getStatus() == AuctionStatus.SCHEDULED 
                || auction.getStatus() == AuctionStatus.ACTIVE) {
                scheduleEnd(auction, now);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule auction " + auction.getId(), e);
        }
    }

    public void refreshAuctionSchedule(long auctionId, AuctionListItemUpdateReason reason) {
        try {
            Auction auction = auctionService.findAuctionById(auctionId);
            if (auction == null) {
                cancelAuction(auctionId);
                return;
            }

            switch (reason) {
                case CREATED, TIME_CHANGED -> {
                    if (auction.getStatus().isClosedForBidding()) {
                        cancelAuction(auctionId);
                    } else {
                        rescheduleAuction(auction);
                    }
                }
                case STATUS_CHANGED -> {
                    if (auction.getStatus().isClosedForBidding()) {
                        cancelAuction(auctionId);
                    } else {
                        rescheduleAuction(auction);
                    }
                }
                case SELLER_UPDATED -> {
                    // Seller-only updates do not affect auction timing.
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to refresh schedule for auction " + auctionId, e);
        }
    }

    private void recoverMissedAuctions() {
        try {
            List<Auction> auctions = auctionService.findRecoverableSchedules();

            for (Auction auction : auctions) {
                auctionService.syncAuctionStatus(auction.getId());

                Auction refreshed = auctionService.findAuctionById(auction.getId());
                if (refreshed != null && !refreshed.getStatus().isClosedForBidding()) {
                    rescheduleAuction(refreshed);
                } else {
                    cancelAuction(auction.getId());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while recovering missed auctions", e);
        }
    }

    public void shutdown() {
        if (recoveryFuture != null) {
            recoveryFuture.cancel(false);
        }
        for (ScheduledFuture<?> future : startTasks.values()) {
            future.cancel(false);
        }
        for (ScheduledFuture<?> future : endTasks.values()) {
            future.cancel(false);
        }
        startTasks.clear();
        endTasks.clear();

        executor.shutdown();
    }
}
