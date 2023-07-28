package me.billymn.experienceboosters;

public class BoosterInfo {
    private final long boosterEndTime;
    private final double boosterFactor; // Add the boosterFactor variable

    private String playerName; // Add the playerName variable


    public BoosterInfo(long boosterEndTime, double boosterFactor) {
        this.boosterEndTime = boosterEndTime;
        this.boosterFactor = boosterFactor;
    }

    public long getBoosterEndTime() {
        return boosterEndTime;
    }

    public double getBoosterFactor() {
        return boosterFactor;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

