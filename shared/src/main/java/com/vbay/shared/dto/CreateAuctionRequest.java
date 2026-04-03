package com.vbay.shared.dto;

public class CreateAuctionRequest {
    private String Tile;
    private String Description;
    private double InitialPrice, StepPrice;
    private String StartingTime, EndingTime;
    private double DepositAmount;

    // Constructor
    public CreateAuctionRequest(
        String Tile,
        String Description,
        double InitialPrice, double StepPrice,
        String StartingTime, String EndingTime,
        double DepositAmount
    ) {
        this.Tile = Tile;
        this.Description = Description;
        this.InitialPrice = InitialPrice;
        this.StepPrice = StepPrice;
        this.StartingTime = StartingTime;
        this.EndingTime = EndingTime;
        this.DepositAmount = DepositAmount;
    }

    // Getter
    public String getTile() {
        return this.Tile;
    }

    public String getDescription() {
        return this.Description;
    }

    public double getInitialPrice() {
        return this.InitialPrice;
    }

    public double getStepPrice() {
        return this.StepPrice;
    }

    public String getStartingTime() {
        return this.StartingTime;
    }

    public String getEndingTime() {
        return this.EndingTime;
    }

    public double getDepositAmount() {
        return this.DepositAmount;
    }

    @Override
    public String toString() {
        return "CreateAuctionRequest{" +
            "Tile='" + this.getTile() + '\'' +
            ", Description='" + this.getDescription() + '\'' +
            ", InitialPrice=" + this.getInitialPrice() +
            ", StepPrice=" + this.getStepPrice() +
            ", StartingTime='" + this.getStartingTime() + '\'' +
            ", EndingTime='" + this.getEndingTime() + '\'' +
            ", DepositAmount=" + this.getDepositAmount() +
            '}';
    }
}
