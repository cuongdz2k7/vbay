# Bid / AutoBid / Buy Now Refactor Context

This note captures the current design direction and rules for the auction bid refactor so the next Codex session can continue without rebuilding context.

## Client / Server Communication Contract

This section is the current contract the client controllers should follow when connecting to the backend.

### Message Envelope

Client request:

```json
{
  "requestId": "client-generated-id",
  "requestType": "GET_AUCTION_DETAIL",
  "payload": {}
}
```

`type` is also accepted as an alias for `requestType` by `RequestDistributor`, but new client code should use `requestType`.

Server response:

```json
{
  "messageType": "RESPONSE",
  "requestId": "same-id",
  "status": true,
  "message": "Human readable message",
  "data": {}
}
```

Realtime event:

```json
{
  "messageType": "EVENT",
  "eventId": "server-generated-id",
  "type": "AUCTION_STATE_UPDATED",
  "room": {
    "type": "AUCTION",
    "targetId": 123,
    "filter": null
  },
  "payload": {},
  "occurredAt": "database/server event time"
}
```

Client must treat `auctionVersion`, `userVersion`, and `updatedAt` / `occurredAt` as stale-event guards. If a local item has a newer version/time, ignore the older event.

### Rooms

```text
AUCTION
  targetId required
  used for public auction detail state and bid history events

AUCTION_LIST
  targetId must be null
  currently no filter
  used for public auction list row updates

USER
  targetId required and must be the authenticated user's id
  used for private balance, my-bid row, and AutoBid state
```

Subscribe/unsubscribe uses:

```json
{
  "requestType": "SUBSCRIBE_ROOM",
  "payload": {
    "type": "AUCTION",
    "targetId": 123
  }
}
```

### Request Types Used By Auction / Bid UI

`GET_AUCTION_LIST`

Payload:

```json
{
  "status": "ACTIVE",
  "categoryId": null,
  "sellerId": null,
  "limit": 100
}
```

Response data:

```text
AuctionListResponse
  items: List<AuctionListItemPayload>
```

`AuctionListItemPayload` is a list-row payload. It includes public auction fields and viewer-private summary flags:

```text
auctionId
auctionVersion
productId
sellerId
title
description
productName
categoryId
status
startingPrice
currentPrice
minimumBidStep
buyNowPrice
winnerUserId
reserveMet
antiSnipeExtended
thumbnailUrl
startingTime
endingTime
updatedAt
viewerBidState: ViewerAuctionBidSummaryPayload
```

Important: `AuctionListItemPayload.viewerBidState` in direct `GET_AUCTION_LIST` response is personalized for the requesting session, but public realtime `AUCTION_LIST_ITEM_UPDATED` events must not include viewer-private max AutoBid data. The summary payload intentionally has no `maxBidAmount`.

`ViewerAuctionBidSummaryPayload`:

```text
auctionId
userId
hasAutobid
autobidStatus
winning
showActiveMaxBid
updatedAt
```

`GET_AUCTION_DETAIL`

Payload:

```json
{
  "auctionId": 123
}
```

Response data:

```text
AuctionItemPayload
```

`AuctionItemPayload` is the detail payload. It includes all list fields plus:

```text
imageUrls
viewerBidState: ViewerAuctionBidStatePayload
```

`ViewerAuctionBidStatePayload` is private to the requesting user and can include AutoBid max:

```text
auctionId
userId
autobidId
maxBidAmount
autobidStatus
winning
showActiveMaxBid
updatedAt
```

Client detail rule:

```text
if viewerBidState.showActiveMaxBid:
    show "Your max bid: viewerBidState.maxBidAmount"
else:
    hide active max-bid line
```

`GET_MY_BID_LIST`

Response data:

```text
MyBidListResponse
  items: List<MyBidListItemPayload>
```

`MyBidListItemPayload`:

```text
bidId
auctionId
auctionVersion
auctionTitle
thumbnailUrl
currentPrice
auctionStatus
myBidAmount
myMaxBidAmount
bidStatus
bidSource
bidTime
startingTime
endingTime
updatedAt
```

Client my-bid row amount rule:

```text
if bidSource == AUTO_BID
   and bidStatus == WINNING
   and myMaxBidAmount != null:
    display myMaxBidAmount as "Your max bid"
else:
    display myBidAmount as the user's actual bid
```

`PLACE_BID`

Payload:

```json
{
  "auctionId": 123,
  "bidAmount": 120.00
}
```

Success response data is currently null in `RequestDistributor`; client should update UI from realtime events. On rejected AutoBid retaliation cases, server may persist/broadcast auction changes and then respond with `status=false`.

`AUTO_BID`

Payload:

```json
{
  "auctionId": 123,
  "maxBidAmount": 250.00
}
```

`bidAmount` is accepted as a backward-compatible alias for `maxBidAmount`, but new client code should send `maxBidAmount`.

Success response data:

```text
AutobidRegistrationResult
```

On rejected-retaliation, server can still broadcast public bid/auction changes, then response status is false with message:

```text
Someone has already placed a higher maximum bid.
```

`BUY_NOW`

Payload:

```json
{
  "auctionId": 123
}
```

Success response data is currently null in `RequestDistributor`; client should update UI from realtime events.

`DEPOSIT_BALANCE`

Payload:

```json
{
  "amount": 100.00
}
```

Response data:

```text
UserBalanceResponse
```

Server also emits `USER_BALANCE_UPDATED` to the authenticated user's `USER` room.

### Realtime Event Types

`AUCTION_LIST_ITEM_UPDATED`

Room:

```text
AUCTION_LIST
```

Payload:

```text
AuctionListItemPayload
```

Purpose:

```text
Create/update one public auction list row.
Do not rely on this event for viewer-private AutoBid max.
```

Client merge key:

```text
auctionId
```

`AUCTION_STATE_UPDATED`

Room:

```text
AUCTION:{auctionId}
```

Payload:

```text
AuctionStatePayload
```

Fields:

```text
auctionId
auctionVersion
status
currentPrice
reserveMet
antiSnipeExtended
winnerUserId
startingTime
endingTime
updatedAt
endedAt
stateChangeReason
```

`stateChangeReason` values:

```text
STARTED
BID_PLACED
BUY_NOW
TIME_EXPIRED_ENDED
TIME_EXPIRED_FAILED
```

Client should use this event to update detail header/current price/countdown/status.

`BID_HISTORY_ITEM_ADDED`

Room:

```text
AUCTION:{auctionId}
```

Payload:

```text
BidHistoryItemPayload
  auctionId
  auctionVersion
  bidId
  bidderId
  bidderDisplayName
  bidAmount
  bidStatus
  bidSource
  bidTime
```

`bidSource` values include:

```text
USER_BID
AUTO_BID
BUY_NOW
```

This is public bid history. It must never include AutoBid max amount.

`MY_BID_LIST_ITEM_UPDATED`

Room:

```text
USER:{userId}
```

Payload:

```text
MyBidListItemPayload
```

Purpose:

```text
Upsert one row in "My bids".
Can be emitted after manual bid, AutoBid retaliation/register, Buy Now, and auction close.
```

Use the my-bid row amount rule above for `myBidAmount` vs `myMaxBidAmount`.

`AUTOBID_UPDATED`

Room:

```text
USER:{userId}
```

Payload:

```text
AutobidUpdatedPayload
  auctionId
  userId
  autobidId
  maxBidAmount
  autobidStatus
  winning
  showActiveMaxBid
  updatedAt
```

Purpose:

```text
Private AutoBid contract/state update for the owner.
Client should merge it into auction detail viewer state and any local auction-list summary for that auction.
```

Display rule:

```text
if showActiveMaxBid:
    show "Your max bid: maxBidAmount"
else:
    hide active max-bid line
```

`maxBidAmount` is private and must only come through `USER` room or direct viewer-specific responses.

`USER_BALANCE_UPDATED`

Room:

```text
USER:{userId}
```

Payload:

```text
UserBalanceUpdatedPayload
  userId
  userVersion
  availableBalance
  holdBalance
  reason
  updatedAt
```

Client should replace wallet state when `userVersion` is newer.

### Event Coverage By Command

Manual bid accepted with no AutoBid:

```text
AUCTION_LIST_ITEM_UPDATED
AUCTION_STATE_UPDATED
BID_HISTORY_ITEM_ADDED
MY_BID_LIST_ITEM_UPDATED for affected users
USER_BALANCE_UPDATED
```

Manual bid rejected because winning AutoBid retaliates:

```text
AUCTION_LIST_ITEM_UPDATED
AUCTION_STATE_UPDATED
BID_HISTORY_ITEM_ADDED with bidSource AUTO_BID
MY_BID_LIST_ITEM_UPDATED for affected users
response status=false after side effects
```

Manual bid beats winning AutoBid:

```text
AUCTION_LIST_ITEM_UPDATED
AUCTION_STATE_UPDATED
BID_HISTORY_ITEM_ADDED
MY_BID_LIST_ITEM_UPDATED
AUTOBID_UPDATED old AutoBid LOST
USER_BALANCE_UPDATED
```

Register AutoBid accepted:

```text
AUCTION_LIST_ITEM_UPDATED
AUCTION_STATE_UPDATED
BID_HISTORY_ITEM_ADDED with bidSource AUTO_BID
MY_BID_LIST_ITEM_UPDATED
AUTOBID_UPDATED new WINNING
USER_BALANCE_UPDATED
```

Register AutoBid accepted and beats old AutoBid:

```text
AUCTION_LIST_ITEM_UPDATED
AUCTION_STATE_UPDATED
BID_HISTORY_ITEM_ADDED with bidSource AUTO_BID
MY_BID_LIST_ITEM_UPDATED
AUTOBID_UPDATED old LOST
AUTOBID_UPDATED new WINNING
USER_BALANCE_UPDATED
```

Increase max AutoBid:

```text
AUTOBID_UPDATED same AutoBid WINNING with new maxBidAmount
USER_BALANCE_UPDATED
```

No bid history event should be emitted for increase max because no bid record is created.

Buy Now:

```text
AUCTION_LIST_ITEM_UPDATED
AUCTION_STATE_UPDATED with stateChangeReason BUY_NOW
BID_HISTORY_ITEM_ADDED with bidSource BUY_NOW
MY_BID_LIST_ITEM_UPDATED
AUTOBID_UPDATED old AutoBid WON/LOST if a winning AutoBid existed
USER_BALANCE_UPDATED
```

Auction close by time:

```text
AUCTION_LIST_ITEM_UPDATED
AUCTION_STATE_UPDATED with TIME_EXPIRED_ENDED or TIME_EXPIRED_FAILED
MY_BID_LIST_ITEM_UPDATED
AUTOBID_UPDATED WON/LOST if a winning AutoBid existed
```

### Privacy Rules

Public rooms:

```text
AUCTION
AUCTION_LIST
```

Must not expose:

```text
AutoBid maxBidAmount
viewer private wallet/balance
viewer private my-bid row for other users
```

Private user room:

```text
USER:{authenticatedUserId}
```

May expose:

```text
maxBidAmount for that user only
balance for that user only
my-bid list item for that user only
```

Direct request responses such as `GET_AUCTION_DETAIL` and `GET_AUCTION_LIST` may include viewer-specific state only for the authenticated requester. Public realtime events must remain public and reusable across all clients.

## Core Rules

DB is the source of truth. Do not depend on client online/offline state. Client receives realtime updates only; it does not trigger AutoBid resolution.

All changes to auction `currentPrice`, `winnerUserId`, bid state, autobid state, balance, or payment must happen inside one DB transaction. Lock the auction first with `SELECT ... FOR UPDATE`. Do not use RAM locks for AutoBid.

AutoBid statuses:

```java
WINNING
LOST
WON
CANCELLED
```

There is only one `WINNING` AutoBid per auction. Other AutoBids should be `LOST`, `WON`, or `CANCELLED`.

`Autobid` no longer stores `holdAmount`. `maxBidAmount` is the AutoBid contract amount. When a user registers AutoBid, the system holds the full `maxBidAmount`. Any AutoBid release uses `maxBidAmount`.

`Bid` and `Autobid` must remain separate entities:

- `Bid` is bid history/current winner evidence.
- `Autobid` is a persisted user strategy/contract that can create `AUTO_BID` bid records.

## Balance Rules

Manual bid:

- Hold manual bid amount.
- Release old manual winner's held bid amount when outbid.

AutoBid:

- Register/increase holds `maxBidAmount` or the delta when increasing.
- LOST/CANCELLED releases AutoBid `maxBidAmount`.
- WINNING means the wallet is holding the AutoBid contract amount.

Buy Now:

- Buy Now wins over every AutoBid.
- Money leaves wallet available balance and becomes `payment HELD`.
- Do not use `hold_balance` for Buy Now.
- If buyer is current winner, first release existing hold, then decrease available by Buy Now price.
- If current winner is a winning AutoBid, release the AutoBid max amount, not bid amount.

Buying power for Buy Now:

```text
if buyer == currentWinningBid.bidderId:
    trueAvailable = available + currentWinningBid.bidAmount
else:
    trueAvailable = available
```

This is the rule currently preferred by the user, even though AutoBid internally held `maxBidAmount`.

## Resolution Architecture

Direction: use a central resolution pipeline.

```text
Service
  validates request/session/user/auction
  opens transaction
  locks auction
  locks users if needed
  loads currentWinningBid and winningAutobid
  calls AuctionBidEngine
  calls BidResolutionApplier
  builds result
  commits
  publishes domain events

AuctionBidEngine
  pure rule calculation
  no DB query
  no DB update
  no broadcast
  returns BidResolution

BidResolution
  transaction plan/data object
  contains balance changes, bid creates/status/finalization, autobid creates/status/max updates,
  auction changes, payment creates

BidResolutionApplier
  applies all changes to DB in transaction
  reloads final auction/state data
  returns AppliedBidResolution
```

`BidResolution` has a builder:

```java
BidResolution.accepted(auctionId)
BidResolution.rejected(auctionId, message)
```

`accepted/rejected` indicates requester outcome, not whether applier should run. A rejected resolution may still contain side effects to apply and broadcast, e.g. manual bid gets rejected by a winning AutoBid but the system AutoBid bid is still created and auction price updates.

## Important Classes

Resolution model lives around:

```text
server/src/main/java/com/vbay/server/service/bid/resolution/model/bid/BidResolution.java
server/src/main/java/com/vbay/server/service/bid/resolution/model/bid/AppliedBidResolution.java
server/src/main/java/com/vbay/server/service/bid/resolution/BidResolutionApplier.java
server/src/main/java/com/vbay/server/service/bid/resolution/AppliedBidResultMapper.java
server/src/main/java/com/vbay/server/service/bid/engine/AuctionBidEngine.java
```

Bid changes:

```text
BidCreate
BidStatusUpdate
BidFinalization
MarkOtherBidsLostAfterBuyNow
```

Keep `BidStatusUpdate` for one bid only. Keep `BidFinalization` for bulk auction-level bid finalization. Do not merge them; syncStatus/endIfExpired will need bulk finalization later.

Autobid changes:

```text
AutobidCreate
AutobidStatusChange
AutobidMaxBidUpdate
```

Do not put max update into status change. They are different operations.

Auction changes use polymorphism:

```text
AuctionChange
CurrentBidAuctionChange
BuyNowAuctionChange
AntiSnipeAuctionExtensionChange
```

`BidResolution` now uses `List<AuctionChange>`, not a single `AuctionChange`, because one bid can update current price/winner and extend ending time in the same resolution.

Use `addAuctionChange(...)` / `getAuctionChanges()`.

## Buy Now Flow Target

`BuyNowService.buyNow(...)` should only orchestrate:

1. Check session.
2. Validate request DTO.
3. Open transaction.
4. Lock auction.
5. Get DB time.
6. Load current winning bid.
7. Load winning AutoBid.
8. Validate auction eligibility:
   - not closed
   - started
   - not ended
   - buyer not seller
   - buyNowPrice exists
9. Lock buyer user.
10. Validate buyer active and buying power using currentWinningBid.
11. Build `BidResolution` via `auctionBidEngine.resolveBuyNow(...)`.
12. Apply via `bidResolutionApplier.apply(...)`.
13. Load `AuctionListItemResult`.
14. Build `BuyNowResult` via `AppliedBidResultMapper.toBuyNowResult(...)`.
15. Commit.
16. Publish events.

`BuyNowService` should not:

- release balances directly
- save bids directly
- update bid statuses directly
- update auction directly
- create payments directly
- call old `AutobidEngine` or `AutobidService.applyAutobidResolutionChanges`

## Buy Now Engine Logic

`AuctionBidEngine.resolveBuyNow(...)` should:

- If winningAutoBid exists:
  - if buyer is AutoBid user: AutoBid -> `WON`
  - else AutoBid -> `LOST`
  - release `winningAutobid.maxBidAmount`
- If currentWinningBid exists and it is not covered by the winning AutoBid release:
  - release `currentWinningBid.bidAmount`
- Decrease buyer available by `buyNowPrice`.
- Create `BidCreate` with:

```java
BidSource.BUY_NOW
BidStatus.WON
amount = buyNowPrice
```

- Add `MarkOtherBidsLostAfterBuyNow(auctionId, BidSource.BUY_NOW)`.
- Add `BuyNowAuctionChange(auctionId, buyerId, buyNowPrice)`.
- Add `PaymentCreate` for `PaymentType.BUY_NOW`, `PaymentStatus.HELD`.

Current bug/cleanup in `AuctionBidEngine`: remove dead local variables such as unused `List<BidFinalization>` and unused `AuctionChange auctionChange`; builder already receives these changes.

## BidResolutionApplier Expectations

Applier should:

- Apply balances.
- Apply autobid creates/max updates/status updates.
- Save bid creates and collect created `Bid` entities.
- Apply single bid status updates.
- Apply bid finalizations.
- Apply all auction changes in order.
- Save payments.
- Reload auction.
- Build affected my bid items.
- Return `AppliedBidResolution`.

Balance updates should publish final balance, not intermediate balance. The applier should apply all balance changes first, collect affected user ids, and then read final balances once per user:

```text
apply all balance changes first
collect affected userId -> final/reason
read final balances once per user after all balance updates
```

Also ensure release happens before hold/decrease for Buy Now/current winner cases.

## AppliedBidResolution / Mapper

`AppliedBidResolution` should be a data holder, not a use-case mapper. It should contain:

```java
Auction refreshedAuction
List<Bid> createdBids
List<Payment> createdPayments
List<UserBalanceResult> balanceResults
List<UserMyBidListItemResult> affectedMyBidItems
long auctionVersion
```

Do not put helpers like `getRequiredFirstPayment()` into it.

`AppliedBidResultMapper` should map applied resolution to command results.

For Buy Now, it should:

- find the created bid with `BidSource.BUY_NOW`
- find the payment whose `winningBidId` equals that bid id
- call `ResultMapper.toBuyNowResult(...)`

Avoid "first payment" logic.

## Realtime / Publish Requirements

After Buy Now commit, publish:

```text
AuctionListItemUpdatedDomainEvent(... STATUS_CHANGED ...)
BuyNowDomainEvent(result)
UserBalanceUpdatedDomainEvent for every final balance result
```

`BuyNowRealtimeHandler` already broadcasts:

- auction state update
- bid history item
- my bid list item updates from `BuyNowResult.affectedMyBidItems`

No separate `BidUpdatedDomainEvent` is needed for Buy Now if `BuyNowDomainEvent` covers auction state/history/my-bid.

Potential realtime bug:

`RealtimeEventMapper.toBuyNowHistoryItemEvent(...)` currently uses `BidSource.USER_BID.name()` in older code. It should use `BidSource.BUY_NOW.name()`.

Manual bid realtime should publish `BidUpdatedDomainEvent`. Its auction state payload must include `status`, `startingTime`, `endingTime`, and `antiSnipeExtended`, so clients can update countdown and show that the auction has been extended.

## Repository / Schema Status

`data_init.sql` autobids table already has no `hold_amount`.

`auctions` now has anti-snipe state:

```sql
anti_snipe_extension_count INT NOT NULL DEFAULT 0
```

`Auction` has:

```java
int antiSnipeExtensionCount
```

`AuctionRowMapper.mapAuction(...)` should pass `anti_snipe_extension_count` into the DB constructor, not patch it later unless needed. Auction list/detail SQL should expose only a boolean:

```sql
CASE
  WHEN a.anti_snipe_extension_count > 0 THEN TRUE
  ELSE FALSE
END AS anti_snipe_extended
```

The shared payloads expose:

```java
boolean antiSnipeExtended
```

Do not expose the extension count to users unless the UI explicitly needs “x/5 extensions used”.

`JdbcAutobidRepository` was updated to:

- insert only `auction_id, user_id, max_bid_amount, status`
- select only `id, auction_id, user_id, max_bid_amount, status, created_at, updated_at`
- update status/max without `hold_amount`
- implement `updateMaxBidAmount(long autobidId, BigDecimal maxBidAmount)`

`AutobidRepository` contains:

```java
Autobid save(Autobid autobid)
Optional<Autobid> findWinningByAuctionId(long auctionId)
void update(AutobidChange change)
void updateStatus(long autobidId, AutobidStatus status)
void updateMaxBidAmount(long autobidId, BigDecimal maxBidAmount)
```

Long term: old `AutobidChange` still has `newHoldAmount` and old `AutobidEngine` still references `getHoldAmount()`. Those old classes are in transitional state and should be replaced by the new resolution model.

## Current Wiring Status

Current wiring direction:

- `PLACE_BID` routes to `ManualBidService.placeBid(...)`.
- `BUY_NOW` routes to `BuyNowService.buyNow(...)`.
- `AUTO_BID` routes to `AutobidService.registerAutobid(...)`.
- `GET_MY_BID_LIST` routes to `BidQueryService.getMyBidList(...)`.

There are duplicate/refactor-copy files in:

```text
service/bid/manual/ManualBidService.java
service/bid/query/BidQueryService.java
service/bid/buyNow/BuyNowService.java
```

Be careful to identify the actual wired service before deleting or changing behavior.

## Current Build Status

Last known verification:

```text
mvn -pl server -am test
Tests run: 31, Failures: 0, Errors: 0
BUILD SUCCESS
```

## Manual Bid / AutoBid Future Direction

Manual bid is being refactored using the same resolution pipeline.

`ManualBidService.placeBid(...)` should orchestrate only:

1. Check session.
2. Validate DTO.
3. Open transaction.
4. Lock auction first.
5. Get DB time.
6. Validate auction:
   - not closed
   - started
   - not ended
   - bidder is not seller
   - bid amount is lower than Buy Now price if Buy Now exists.
7. Load current winning bid.
8. Validate minimum bid:
   - if no current winner: at least `auction.currentPrice`
   - else at least `auction.currentPrice + minimumBidStep`
9. Load winning AutoBid.
10. Reject immediately if requester is the winning AutoBid user. This case has no side effects.
11. Lock bidder user.
12. Validate user active and buying power.
13. Call `auctionBidEngine.resolveManualBid(...)`.
14. Apply with `BidResolutionApplier`.
15. Load `AuctionListItemResult` after apply.
16. Build `PlaceBidResult` via `AppliedBidResultMapper.toPlaceBidResult(...)`.
17. Detect anti-snipe extension by comparing refreshed ending time with locked auction ending time.
18. Commit.
19. Publish:
    - `AuctionListItemUpdatedDomainEvent(... TIME_CHANGED ...)` if anti-snipe extended the auction.
    - otherwise `AuctionListItemUpdatedDomainEvent(... BID_UPDATED ...)`.
    - `BidUpdatedDomainEvent(result)`.
    - `UserBalanceUpdatedDomainEvent` for every balance result.
20. If `resolution.isAccepted() == false`, throw `ValidationException(resolution.getMessage())` after commit/publish, so the requester gets a reject response but AutoBid side effects are already persisted and broadcast.

Do not throw on every rejected resolution before apply. A rejected resolution may still have side effects, especially AutoBid retaliation.

Manual bid with no winning AutoBid:

- create `USER_BID`
- old winner `OUTBID`
- hold manual bidder amount
- release old manual winner
- update auction current bid/winner

Manual bid against winning AutoBid:

- if bidder is winning AutoBid user: reject
- if manual amount > winningAutoBid.maxBidAmount:
  - accepted
  - manual bidder wins
  - old AutoBid -> `LOST`
  - release AutoBid max amount
  - hold manual bid amount
  - create `USER_BID`
  - update auction
- if manual amount <= winningAutoBid.maxBidAmount:
  - rejected for requester with message:
    `"Someone has already placed a higher maximum bid."`
  - create system `AUTO_BID`
  - winner remains AutoBid user
  - update currentPrice to `min(manualAmount + minStep, maxBidAmount)`
  - no manual bidder hold

Important AutoBid retaliation detail:

```java
addAuctionChangesForWinningBid(
    resolution,
    auction,
    activeWinningAutobid.getUserId(),
    autoBidAmount,
    command.getBidTime()
);
```

Do not use the manual bidder or manual amount in this branch.

## Anti-Snipe Rules

Anti-snipe is server-side, transactionally applied, and must not depend on client state.

Rule:

```text
if bidTime >= auction.endingTime - antiSnipeWindow
and auction.antiSnipeExtensionCount < maxExtensions:
    newEndingTime = bidTime + antiSnipeExtension
```

Default policy:

```java
window = 5 minutes
extension = 5 minutes
maxExtensions = 5
```

Use `bidTime + extension`, not `endingTime + extension`, to avoid runaway stacking.

If the calculated ending time is not after the current ending time, no extension is applied.

Anti-snipe should run whenever a resolution creates a new winning bid and changes auction current price/winner:

- manual bidder wins with `USER_BID`
- manual bidder beats winning AutoBid
- manual bidder is rejected but winning AutoBid creates an `AUTO_BID` retaliation bid

Anti-snipe should not run for:

- rejected no-op validations
- Buy Now
- auction close/finalize

`AuctionBidEngine` should use a helper so every winning-bid branch adds both current bid update and optional anti-snipe extension:

```java
private void addAuctionChangesForWinningBid(
        BidResolution.Builder resolution,
        Auction auction,
        long winnerUserId,
        BigDecimal winningAmount,
        LocalDateTime bidTime) {
    resolution.addAuctionChange(new CurrentBidAuctionChange(
        auction.getId(),
        winnerUserId,
        winningAmount
    ));

    antiSnipePolicy.resolveExtendedEndingTime(auction, bidTime)
        .ifPresent(endingTime -> resolution.addAuctionChange(
            new AntiSnipeAuctionExtensionChange(auction.getId(), endingTime)
        ));
}
```

`AntiSnipeAuctionExtensionChange` should call:

```java
auctionRepository.applyAntiSnipeExtension(auctionId, endingTime)
```

This repository method must update both:

```sql
ending_time = ?
anti_snipe_extension_count = anti_snipe_extension_count + 1
version = version + 1
```

and should only update if the new ending time is later than the current one.

Scheduler integration:

- Manual bid publishes `AuctionListItemUpdatedDomainEvent(... TIME_CHANGED ...)` when anti-snipe extended ending time.
- `AuctionScheduleDomainEventHandler` already listens to `TIME_CHANGED`.
- `AuctionTaskScheduler.refreshAuctionSchedule(...)` reloads the auction from DB, cancels old tasks, and schedules end task using the new ending time.
- The scheduler should not calculate or set anti-snipe time; it only reacts to DB state.

User-visible state:

- Expose `antiSnipeExtended` boolean in auction list/detail/state payloads.
- Compute it as `anti_snipe_extension_count > 0`.
- Keep count internal unless UI needs exact count.

Register/increase AutoBid should also use `AuctionBidEngine + BidResolutionApplier`.

## Register AutoBid / Increase Max AutoBid Direction

Do not continue building new register/increase behavior on the old transitional classes:

```text
server/src/main/java/com/vbay/server/service/bid/autobid/AutobidEngine.java
server/src/main/java/com/vbay/server/service/bid/autobid/AutobidService.java
server/src/main/java/com/vbay/server/service/bid/autobid/model/AutobidResolution.java
```

Those still reference old concepts such as `holdAmount` / `AutobidChange.newHoldAmount`. New AutoBid command flows should use:

```text
AuctionBidEngine
BidResolution
BidResolutionApplier
AutobidCreate
AutobidStatusChange
AutobidMaxBidUpdate
```

### AutoBid Invariants

If a `WINNING` AutoBid exists for an auction, the current winning bid must also exist and must belong to the same user:

```text
winningAutobid.status == WINNING
=> currentWinningBid exists
=> currentWinningBid.bidderId == winningAutobid.userId
=> currentWinningBid.status == WINNING
```

If this invariant is broken, fail fast. Do not try to recover by treating the current winning bid as manual or by releasing both bid amount and AutoBid max amount. A winning AutoBid release uses `maxBidAmount`; do not additionally release the `Bid.bidAmount` for the same user.

### Register AutoBid Service Flow

`AutobidService.registerAutobid(...)` should orchestrate only:

1. Check session.
2. Validate request DTO.
3. Open transaction.
4. Lock auction first.
5. Get DB time.
6. Validate auction:
   - not closed
   - started
   - not ended
   - requester is not seller
7. Validate max AutoBid amount:
   - positive
   - lower than `buyNowPrice` if Buy Now exists
8. Load current winning bid.
9. Validate minimum max amount:
   - if no current winner: at least `auction.startingPrice`
   - if current winner exists: at least `auction.currentPrice + minimumBidStep`
10. Load winning AutoBid.
11. If winning AutoBid exists, assert it matches current winning bid using the invariant above.
12. Load requester existing AutoBid by `(auctionId, userId)`.
13. Reject if requester already has an AutoBid for this auction, unless a future explicit re-enter rule is designed.
14. Lock requester user.
15. Validate requester active and buying power:
   - if requester is current manual winner, buying power is `available + currentWinningBid.bidAmount`
   - otherwise buying power is `available`
   - required amount is full `maxBidAmount`
16. Call `auctionBidEngine.resolveRegisterAutobid(...)`.
17. Apply via `BidResolutionApplier`.
18. Load `AuctionListItemResult` after apply.
19. Build event/result before commit.
20. Commit.
21. Publish:
   - `AuctionListItemUpdatedDomainEvent(... TIME_CHANGED ...)` if anti-snipe extended the auction.
   - otherwise `AuctionListItemUpdatedDomainEvent(... BID_UPDATED ...)`.
   - `BidUpdatedDomainEvent(...)` because register AutoBid can create an `AUTO_BID` bid.
   - `UserBalanceUpdatedDomainEvent` for every final balance result.
22. If `resolution.isAccepted() == false`, throw `ValidationException(resolution.getMessage())` after commit/publish, because AutoBid retaliation side effects may already be validly persisted.

### Register AutoBid Engine Logic

If no winning AutoBid exists:

- If current winning bid exists:
  - mark old winning bid `OUTBID`
  - release old winner's `bidAmount`
- Hold requester full `maxBidAmount`.
- Create `AutobidCreate(... WINNING ...)`.
- Create `BidCreate(... BidSource.AUTO_BID, BidStatus.WINNING ...)`.
- AutoBid bid amount:

```text
if no current winning bid:
    autoBidAmount = auction.startingPrice
else:
    autoBidAmount = min(auction.currentPrice + minimumBidStep, maxBidAmount)
```

- Add current bid/winner auction change.
- Apply anti-snipe because a new winning bid was created.

If a winning AutoBid exists and requester max is higher:

- Assert winning AutoBid matches current winning bid.
- Mark old winning bid `OUTBID`.
- Mark old AutoBid `LOST`.
- Release old AutoBid `maxBidAmount`.
- Hold requester full `maxBidAmount`.
- Create requester AutoBid as `WINNING`.
- Create requester `AUTO_BID` bid.
- Bid amount:

```text
autoBidAmount = min(oldWinningAutobid.maxBidAmount + minimumBidStep, requester.maxBidAmount)
```

- Update auction current bid/winner and apply anti-snipe.

If a winning AutoBid exists and requester max is lower or equal:

- The request is rejected for requester, but the old winning AutoBid may still retaliate.
- Assert winning AutoBid matches current winning bid.
- Mark old winning bid `OUTBID`.
- Create new `AUTO_BID` bid for old winning AutoBid user.
- Bid amount:

```text
autoBidAmount = min(requester.maxBidAmount + minimumBidStep, oldWinningAutobid.maxBidAmount)
```

- Update auction current bid/winner and apply anti-snipe.
- Do not create AutoBid for requester.
- Do not hold requester balance.

Important: `AutobidRegistrationResult` currently assumes an `AutobidCreate` exists. That will fail for the rejected-retaliation branch above. The service must build a bid event result separately for rejected side effects, e.g. with `AppliedBidResultMapper.toPlaceBidResult(...)` or a new generic `BidUpdateResult` mapper, while only returning `AutobidRegistrationResult` for accepted register.

### Increase Max AutoBid Service Flow

`AutobidService.increaseMaxAutobidAmount(...)` should be narrower than register:

1. Check session.
2. Validate request DTO.
3. Open transaction.
4. Lock auction first.
5. Get DB time.
6. Validate auction eligibility.
7. Validate new max:
   - positive
   - lower than `buyNowPrice` if Buy Now exists
8. Load requester AutoBid by `(auctionId, userId)`.
9. Validate AutoBid belongs to requester.
10. Validate AutoBid is `WINNING`.
11. Validate `newMaxBidAmount > existing.maxBidAmount`.
12. Load current winning bid and assert it matches the winning AutoBid invariant.
13. Calculate `delta = newMaxBidAmount - existing.maxBidAmount`.
14. Lock requester user.
15. Validate requester active and `availableBalance >= delta`.
16. Call `auctionBidEngine.resolveIncreaseMaxAutobidAmount(...)`.
17. Apply via `BidResolutionApplier`.
18. Commit.
19. Publish `UserBalanceUpdatedDomainEvent` for affected balance results.

Increase max AutoBid should not:

- create a new bid
- update auction current price/winner
- apply anti-snipe
- publish `BidUpdatedDomainEvent`
- publish `AuctionListItemUpdatedDomainEvent`

Engine logic for increase max:

```text
delta = newMaxBidAmount - oldMaxBidAmount
add BalanceChange(HOLD, delta, "AUTOBID_INCREASE_HOLD")
add AutobidMaxBidUpdate(existingAutobid.id, newMaxBidAmount)
```

If UI needs explicit confirmation data beyond request success and balance realtime, add a separate `AutobidUpdateResult` / `AutobidUpdatedDomainEvent`. Do not fake this as a bid update because no bid was created.

### AutoBid Result / Mapper Notes

`AutobidRegistrationResult` can extend `BidUpdateResult` for accepted register because accepted register creates an `AUTO_BID` bid.

Keep these values distinct:

```text
maxBidAmount = AutoBid contract ceiling
bidAmount = actual bid record amount created now
currentPrice = refreshed auction current price after apply
```

`AutobidRegistrationResult` should ideally accept both `bidAmount` and `currentPrice`; do not rely on them always being equal.

Avoid ambiguous `getStatus()` on `AutobidRegistrationResult`; prefer `getAutobidStatus()` because base result already has bid and auction statuses.

## Notes For Next Session

Priority checklist:

1. Keep client-server contract in this file synced with shared DTO changes.
2. Add/update client controllers for `AUTOBID_UPDATED`, `MY_BID_LIST_ITEM_UPDATED`, `AUCTION_STATE_UPDATED`, and `AUCTION_LIST_ITEM_UPDATED`.
3. Consider adding an explicit `INCREASE_AUTOBID_MAX` request type; current server service exists but request enum/distributor path is not listed in `RequestType`.
4. Clean placeholder/debug messages from `AuctionBidEngine.resolveManualBid` and old comments in bid services.
5. Run `mvn -pl server -am test` after changing service/result/realtime contracts.

## Private AutoBid Viewer State / Realtime Rules

AutoBid max amount is private user state. Do not put `maxBidAmount` into public auction state/list events or public auction fields.

Public bid/auction event:

```text
BidUpdatedDomainEvent
  auction room
  public currentPrice/winner/history/my-bid row updates
  must not expose AutoBid max amount
```

Private AutoBid event:

```text
AutobidUpdatedDomainEvent
  user room
  maxBidAmount
  autobidStatus
  winning
  showActiveMaxBid
```

Client display rule for auction detail:

```text
if viewerBidState.showActiveMaxBid:
    show "Your max bid: viewerBidState.maxBidAmount"
else:
    hide active max-bid line
```

Client display rule for my-bid row amount:

```text
if bidSource == AUTO_BID
   and bidStatus == WINNING
   and myMaxBidAmount != null:
    display myMaxBidAmount
else:
    display myBidAmount
```

`MyBidListItemPayload` has:

```java
BigDecimal myBidAmount;      // latest actual bid amount
BigDecimal myMaxBidAmount;   // AutoBid max only when latest row is active AUTO_BID WINNING
```

`AuctionItemPayload` now has private per-viewer state:

```java
ViewerAuctionBidStatePayload viewerBidState;
```

`AuctionService.getAuctionDetail(...)` fills `viewerBidState` from:

```text
AutobidRepository.findByAuctionIdAndUserId(auctionId, session.userId)
auction.winnerUserId
```

`viewerBidState.showActiveMaxBid` is true only when:

```text
autobid.status == WINNING
and auction.winnerUserId == session.userId
```

Realtime coverage expected:

```text
Register AutoBid accepted:
  AuctionListItemUpdatedDomainEvent
  BidUpdatedDomainEvent
  AutobidUpdatedDomainEvent(new WINNING)
  UserBalanceUpdatedDomainEvent

Register AutoBid accepted beating old AutoBid:
  AuctionListItemUpdatedDomainEvent
  BidUpdatedDomainEvent
  AutobidUpdatedDomainEvent(old LOST)
  AutobidUpdatedDomainEvent(new WINNING)
  UserBalanceUpdatedDomainEvent

Register AutoBid rejected-retaliation:
  AuctionListItemUpdatedDomainEvent
  BidUpdatedDomainEvent
  UserBalanceUpdatedDomainEvent if any
  no AutobidUpdatedDomainEvent unless status/max changed

Increase max AutoBid:
  AutobidUpdatedDomainEvent(WINNING with new max)
  UserBalanceUpdatedDomainEvent

Manual bid beats winning AutoBid:
  AuctionListItemUpdatedDomainEvent
  BidUpdatedDomainEvent
  AutobidUpdatedDomainEvent(old LOST)
  UserBalanceUpdatedDomainEvent

Buy Now with winning AutoBid:
  AuctionListItemUpdatedDomainEvent
  BuyNowDomainEvent
  AutobidUpdatedDomainEvent(WON if buyer is AutoBid user, else LOST)
  UserBalanceUpdatedDomainEvent

Auction close with winning AutoBid:
  AuctionListItemUpdatedDomainEvent
  AuctionClosedDomainEvent, whose handler also broadcasts AutobidUpdatedDomainEvent(WON/LOST)
```

Current list-state decision:

`GET_AUCTION_LIST` returns `AuctionListItemPayload.viewerBidState` with `ViewerAuctionBidSummaryPayload`. It is session-personalized but intentionally does not include `maxBidAmount`.

Public `AUCTION_LIST_ITEM_UPDATED` realtime events still use the public list item shape and must not expose max AutoBid.

## Client UI / UX Rules Added During JavaFX Integration

These rules capture the latest frontend direction so future work keeps the UI consistent instead of rebuilding visual decisions from screenshots.

### Overall Visual Language

Use a dark clean/glass style with restrained orange accents:

```text
primary surface: near-black / charcoal
secondary surface: transparent dark glass
accent: orange only for active controls, urgent time, key CTA, thin borders
text: white/cream for primary, muted warm gray for secondary
avoid: strong orange glow, heavy shadows, oversized cards, nested floating cards
```

Hover should be subtle:

```text
row hover = slightly brighter surface / light border
button hover = small contrast bump
avoid large orange glow unless it is the primary action
```

Cards and rows should look modern:

```text
border radius: 12-18px depending on size
table rows: compact height, clear spacing, rounded corners
header rows: full-width, rounded, slightly stronger background than data rows
do not use rectangular hard-edge frames for auction fields
```

### Bid Detail Screen Rules

Seller cannot bid on own auction:

```text
disable PLACE BID
disable SET PROXY BID / INCREASE PROXY BID
show disabled state, not an error modal
```

Leading state:

```text
if viewer is winning manually:
    show "You are leading"
    show current commitment

if viewer is winning by AutoBid and viewerBidState.showActiveMaxBid:
    show "You are leading"
    show "Your max bid: viewerBidState.maxBidAmount"
    do not expose this max anywhere public
```

Time display:

```text
Auction detail may show "Extended" beside the countdown when antiSnipeExtended is true.
MyBid table must not show the word "Extended"; it only shows the remaining time.
```

Proxy bid modal:

```text
SET PROXY BID and INCREASE PROXY BID use the same custom modal, not JavaFX Alert.
Modal should be compact and near-square, not a tall empty rectangle.
Current size direction: about 440x440, centered, dark glass, low glow.
Register mode title: "Set Proxy Bid"
Increase mode title: "Increase Proxy Bid"
Register placeholder: "Not lower than {minimumProxyBid}"
Increase placeholder: "Higher than {currentMaxBid}"
```

Proxy modal validation should happen client-side before sending:

```text
max must be positive
register max >= next valid proxy bid
increase max > current viewer max
max < buyNowPrice if buyNowPrice exists
```

If backend rejects because an old AutoBid exists but is not winning anymore, the backend rule should allow re-enter/increase according to the current service design. Do not block the user on the client just because an old AutoBid row exists; use `viewerBidState` and `autobidStatus`, not existence alone.

### MyBid Screen Rules

Column order:

```text
AUCTION TITLE
CURRENT PRICE
YOUR MAX BID
BID PLACED
TIME LEFT
SOURCE
STATUS
```

`BID PLACED` is elapsed-time display, updated every second:

```text
if bid was placed < 60 seconds ago:
    "Just now"
else if < 1 hour:
    "MMm SSs"
else if < 1 day:
    "HHh MMm"
else:
    "MMM d, yyyy"
```

`TIME LEFT`:

```text
if auction not ACTIVE or time <= now:
    "Ended"
else:
    remaining countdown
do not prefix with "Extended" on MyBid
```

Amount display:

```text
CURRENT PRICE = auction currentPrice, bright and right/center aligned
YOUR MAX BID = if active winning AutoBid then myMaxBidAmount else myBidAmount
YOUR MAX BID should be readable muted gray, not disabled-looking dark text
```

Source display:

```text
Auto Bid = blue text, no pill background
Manual = normal muted text
Only STATUS should be a pill/badge
```

Status badges:

```text
WINNING/WON = green pill
OUTBID = red pill
LOST/CANCELLED = muted gray pill
```

Table layout:

```text
header row should eat into the table width, close to both edges
data rows should align with header columns
numeric/time columns should be centered or right-aligned consistently
avoid a large dead gap between YOUR MAX BID and BID PLACED
```

### Home Personal Bid Preview Rules

The right-side portfolio preview is a compact summary, not a full table.

Bid preview node:

```text
single-line auction title with ellipsis
show current price
show BID TIME / BID PLACED summary
status on the right
thin orange rail on the left is allowed, but avoid strong glow
```

Preview should use the same `MyBidListItemPayload` merge rules as MyBid:

```text
key by auctionId
merge realtime `MY_BID_LIST_ITEM_UPDATED`
sort by updatedAt if present, otherwise bidTime
limit to most relevant recent rows
```

### Create Auction Scheduling UI Rules

Start time:

```text
DatePicker for date
three segmented fields for HH : mm : ss
do not require users to type colon manually
```

Duration:

```text
Quick Auction and Standard Auction are tab-like buttons using the same active-tab feel as Bid detail tabs.
Quick options: 30 seconds, 1 minute, 3 minutes, 5 minutes, 15 minutes, 30 minutes, 1 hour
Standard options: 1 day, 3 days, 5 days, 7 days, 14 days, 30 days
No custom duration option for now.
```

Preview:

```text
show computed ending time = commencement + selected duration
Quick mode preview can use more urgent orange text
Standard mode preview should stay calm/muted
```

## Client Controller Wiring Rules

Controllers should follow the same realtime-first pattern:

```text
1. Load initial snapshot by direct request.
2. Subscribe to required realtime rooms.
3. Render from local state map/list.
4. Merge events by id and ignore stale versions/timestamps.
5. Do not block JavaFX application thread with slow request/response waits during navigation.
6. On command success, prefer realtime refresh; only use response data for immediate private confirmations.
7. Unsubscribe/dispose listeners when leaving the screen.
```

Do not perform synchronous network fetches directly inside navigation button handlers if the fetch can block the JavaFX thread. Navigate first or load asynchronously, then render loading/empty/error states.

### Bid Detail Controller Wiring

Initial load:

```text
GET_AUCTION_DETAIL(auctionId)
subscribe AUCTION:{auctionId}
subscribe USER:{sessionUserId}
```

Events to handle:

```text
AUCTION_STATE_UPDATED:
  update current price, winner, status, starting/ending time, reserve, antiSnipeExtended

BID_HISTORY_ITEM_ADDED:
  append/update visible bid history
  use bidSource for public history only

AUTOBID_UPDATED:
  merge into viewerBidState if payload.auctionId matches current auction
  update proxy button text:
    show INCREASE PROXY BID only for active winning AutoBid
    otherwise SET PROXY BID
  update "Your max bid" line when showActiveMaxBid is true

MY_BID_LIST_ITEM_UPDATED:
  optional detail-side personal bid state update for current auction

USER_BALANCE_UPDATED:
  replace header balance if userVersion is newer
```

Command buttons:

```text
PLACE_BID -> RequestType.PLACE_BID
BUY_NOW -> RequestType.BUY_NOW
SET PROXY BID -> RequestType.AUTO_BID
INCREASE PROXY BID -> RequestType.INCREASE_AUTOBID_MAX
WATCH ASSET -> future chart screen/modal
```

Proxy button text rule:

```text
if viewerBidState.showActiveMaxBid && viewerBidState.autobidStatus == WINNING:
    "INCREASE PROXY BID"
else:
    "SET PROXY BID"
```

### Home Controller Wiring

Initial load:

```text
GET_AUCTION_LIST for upcoming/live sections
GET_MY_BID_LIST for portfolio preview
subscribe AUCTION_LIST
subscribe USER:{sessionUserId}
```

Events:

```text
AUCTION_LIST_ITEM_UPDATED:
  merge public auction card by auctionId
  do not expect max AutoBid amount

MY_BID_LIST_ITEM_UPDATED:
  merge preview item by auctionId
  update right-side Personal Bid card immediately

AUTOBID_UPDATED:
  merge viewer bid summary for matching local auction cards
  never show max amount on Home cards

USER_BALANCE_UPDATED:
  update balance chip
```

Navigation:

```text
Click auction card -> open Bid detail with auctionId.
Click Personal Bid sidebar/nav -> open MyBid view without blocking JavaFX thread.
Click View All in portfolio preview -> open MyBid view.
```

Avoid duplicate auction cards:

```text
Use auctionId as the single key.
Do not append blindly after every reload/event.
Replace or merge existing item.
Separate upcoming/live filters should be derived from auction status/time, not duplicated source lists.
```

### MyBid Controller Wiring

Initial load:

```text
GET_MY_BID_LIST
subscribe USER:{sessionUserId}
```

Events:

```text
MY_BID_LIST_ITEM_UPDATED:
  upsert row by auctionId
  preserve latest item by auctionVersion / updatedAt

AUTOBID_UPDATED:
  optional patch of matching row's max/state if present

USER_BALANCE_UPDATED:
  update header balance through shell/root controller if available
```

Rows:

```text
click row -> open Bid detail for auctionId
filter status locally after merge
sort newest first by updatedAt, then bidTime
refresh BID PLACED and TIME LEFT labels every second
```

## Watch Asset / Bid Price Chart Future Notes

The current code has a `WATCH ASSET` button on Bid detail, but no dedicated DTO/request contract was found for a bid price chart yet. Treat this as a future feature contract unless shared DTOs are added.

Recommended UX:

```text
Open as a custom modal or separate detail tab, not JavaFX Alert.
Primary chart: bid price over time.
Secondary metadata: current price, starting price, buy now price, bid count, last bid source, last bid time.
Use dark glass surface, compact legends, and restrained accent colors.
Manual bid and AutoBid should be visually distinguishable but not over-saturated.
```

Recommended chart data model:

```text
BidPricePointPayload
  auctionId
  bidId
  bidAmount
  bidSource        // USER_BID, AUTO_BID, BUY_NOW
  bidStatus
  bidderDisplayName
  bidTime
  auctionVersion
```

Recommended response:

```text
GET_AUCTION_BID_TIMELINE
payload:
  auctionId
  limit

response:
  AuctionBidTimelineResponse
    auctionId
    startingPrice
    currentPrice
    buyNowPrice
    points: List<BidPricePointPayload>
```

Recommended realtime behavior:

```text
Initial chart loads via GET_AUCTION_BID_TIMELINE.
Subscribe to AUCTION:{auctionId}.
On BID_HISTORY_ITEM_ADDED:
  append one point if bidId is new
  update current price marker
On AUCTION_STATE_UPDATED:
  update current price/status/ending time markers
Ignore stale points using auctionVersion and bidId.
```

Privacy:

```text
Chart data is public auction history only.
Never include AutoBid maxBidAmount in chart points.
AutoBid points represent actual created AUTO_BID bid records only.
```

Chart design insight:

```text
Use a step-line chart rather than a smooth line, because bid prices change discretely.
Show point markers only for important events to avoid clutter.
Use source color by stroke/marker:
  Manual/User bid = neutral/cream
  Auto Bid = muted blue
  Buy Now = orange accent
Use tooltip on hover:
  amount, source, bidder display name, placed time
For anti-snipe, show an ending-time marker/annotation, not a price point.
```

Implementation note:

```text
If JavaFX built-in LineChart looks too default, wrap a custom Canvas/SVG-like Pane for the chart.
The app already relies on custom CSS-heavy UI, so a custom lightweight chart may look more consistent than default controls.
```
