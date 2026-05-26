package com.vbay.ui.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import com.vbay.shared.dto.realtimeDTO.payload.BidHistoryItemPayload;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public final class BidPriceChart extends Pane {
    private final List<BidHistoryItemPayload> bids = new ArrayList<>();
    
    public BidPriceChart() {
        getStyleClass().add("bid-price-chart");
        
        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> draw();
        widthProperty().addListener(resizeListener);
        heightProperty().addListener(resizeListener);
    }
    
    public void setBids(List<BidHistoryItemPayload> bidsList) {
        this.bids.clear();
        if (bidsList != null) {
            List<BidHistoryItemPayload> sorted = new ArrayList<>(bidsList);
            sorted.sort((b1, b2) -> b1.getBidTime().compareTo(b2.getBidTime()));
            this.bids.addAll(sorted);
        }
        draw();
    }
    
    public void addBid(BidHistoryItemPayload bid) {
        if (bid == null) return;
        
        this.bids.add(bid);
        this.bids.sort((b1, b2) -> b1.getBidTime().compareTo(b2.getBidTime()));
        
        if (this.bids.size() > 15) {
            this.bids.remove(0);
        }
        
        draw();
    }
    
    private String formatTime(java.time.LocalDateTime time) {
        if (time == null) return "Now";
        try {
            ZonedDateTime utcZoned = time.atZone(ZoneId.of("UTC"));
            ZonedDateTime vnZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
            return vnZoned.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            try {
                return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (Exception ex) {
                return "Now";
            }
        }
    }

    private void draw() {
        getChildren().clear();
        
        double w = getWidth();
        double h = getHeight();
        
        if (w <= 0 || h <= 0 || bids.isEmpty()) {
            Text emptyText = new Text("No price data available");
            emptyText.setFill(Color.web("#8c8c8c"));
            emptyText.setFont(Font.font("Inter", 13));
            emptyText.setX((w - emptyText.getLayoutBounds().getWidth()) / 2);
            emptyText.setY(h / 2);
            getChildren().add(emptyText);
            return;
        }
        
        double paddingLeft = 56;
        double paddingRight = 24;
        double paddingTop = 24;
        double paddingBottom = 40;
        
        double drawW = w - paddingLeft - paddingRight;
        double drawH = h - paddingTop - paddingBottom;
        
        BigDecimal maxPrice = bids.get(0).getBidAmount();
        BigDecimal minPrice = bids.get(0).getBidAmount();
        for (BidHistoryItemPayload bid : bids) {
            BigDecimal amt = bid.getBidAmount();
            if (amt.compareTo(maxPrice) > 0) maxPrice = amt;
            if (amt.compareTo(minPrice) < 0) minPrice = amt;
        }
        
        double maxVal = maxPrice.doubleValue();
        double minVal = minPrice.doubleValue();
        if (maxVal == minVal) {
            maxVal += 10.0;
            minVal = Math.max(0, minVal - 10.0);
        } else {
            double diff = maxVal - minVal;
            maxVal += diff * 0.15;
            minVal = Math.max(0, minVal - 0.15 * diff);
        }
        
        int gridLines = 4;
        for (int i = 0; i < gridLines; i++) {
            double y = paddingTop + (drawH / (gridLines - 1)) * i;
            Line gridLine = new Line(paddingLeft, y, w - paddingRight, y);
            gridLine.setStroke(Color.web("#222222"));
            gridLine.setStrokeWidth(1.0);
            gridLine.getStrokeDashArray().addAll(4.0, 4.0);
            getChildren().add(gridLine);

            // Draw y-axis price label on the left
            double gridVal = maxVal - ((maxVal - minVal) / (gridLines - 1)) * i;
            Text gridValText = new Text("$" + (int) Math.round(gridVal));
            gridValText.setFill(Color.web("#8c8c8c"));
            gridValText.setFont(Font.font("Inter", 10));
            gridValText.setX(10);
            gridValText.setY(y + 4);
            getChildren().add(gridValText);
        }
        
        int n = bids.size();
        double[] xs = new double[n];
        double[] ys = new double[n];
        
        for (int i = 0; i < n; i++) {
            double x;
            if (n == 1) {
                x = paddingLeft + drawW / 2.0;
            } else {
                x = paddingLeft + (drawW / (n - 1)) * i;
            }
            double val = bids.get(i).getBidAmount().doubleValue();
            double y = h - paddingBottom - ((val - minVal) / (maxVal - minVal)) * drawH;
            xs[i] = x;
            xs[i] = x;
            ys[i] = y;
        }
        
        if (n > 1) {
            Polygon area = new Polygon();
            area.getPoints().addAll(xs[0], h - paddingBottom);
            for (int i = 0; i < n; i++) {
                area.getPoints().addAll(xs[i], ys[i]);
            }
            area.getPoints().addAll(xs[n - 1], h - paddingBottom);
            
            LinearGradient areaGrad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#22C55E", 0.15)),
                new Stop(1, Color.web("#22C55E", 0.0))
            );
            area.setFill(areaGrad);
            getChildren().add(area);
        }
        
        Polyline trendLine = new Polyline();
        for (int i = 0; i < n; i++) {
            trendLine.getPoints().addAll(xs[i], ys[i]);
        }
        trendLine.setStroke(Color.web("#22C55E"));
        trendLine.setStrokeWidth(3.0);
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#22C55E", 0.6));
        glow.setRadius(10.0);
        glow.setSpread(0.1);
        trendLine.setEffect(glow);
        getChildren().add(trendLine);
        
        for (int i = 0; i < n; i++) {
            double cx = xs[i];
            double cy = ys[i];
            
            Circle outerCircle = new Circle(cx, cy, 6);
            outerCircle.setFill(Color.web("#22C55E"));
            getChildren().add(outerCircle);
            
            Circle innerCircle = new Circle(cx, cy, 3.5);
            innerCircle.setFill(Color.web("#141414"));
            getChildren().add(innerCircle);
            
            Text priceText = new Text("$" + bids.get(i).getBidAmount().setScale(0, RoundingMode.HALF_UP).toString());
            priceText.setFill(Color.web("#22C55E"));
            priceText.setFont(Font.font("Inter", 11));
            double textW = priceText.getLayoutBounds().getWidth();
            priceText.setX(cx - textW / 2);
            priceText.setY(cy - 12);
            getChildren().add(priceText);

            // Draw x-axis time label below node
            String timeStr = formatTime(bids.get(i).getBidTime());
            Text timeText = new Text(timeStr);
            timeText.setFill(Color.web("#8c8c8c"));
            timeText.setFont(Font.font("Inter", 9));
            double timeTextW = timeText.getLayoutBounds().getWidth();
            timeText.setX(cx - timeTextW / 2);
            timeText.setY(h - paddingBottom + 20);
            getChildren().add(timeText);
        }
    }
}
