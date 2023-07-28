package me.billymn.experienceboosters;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ExperienceBoosterListener implements Listener {


    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Boolean> boosterMessagesSent = new HashMap<>();
    private final Map<String, BoosterInfo> serverBoosters = new HashMap<>(); // Update to store BoosterInfo objects
    private final Map<String, Long> globalBoosters = new HashMap<>(); // Store global boosters
    private final String serverBoosterActivatedMessage;

    private final Map<String, Long> serverBoosterEndTimes = new HashMap<>();


    public ExperienceBoosterListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.serverBoosterActivatedMessage = configManager.getServerBoosterActivatedMessage(); // Initialize the variable


        // Run a repeating task to update booster status for all online players
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (String serverIdentifier : serverBoosterEndTimes.keySet()) {
                    long boosterEndTime = serverBoosterEndTimes.get(serverIdentifier);
                    if (boosterEndTime <= currentTime) {
                        // The booster has expired, so remove it from the map
                        serverBoosterEndTimes.remove(serverIdentifier);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 second
    }





    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int originalExp = event.getAmount();

        double serverBoosterFactor = 1.0;
        Map<String, BoosterInfo> serverBoosters = getServerBoosters();
        if (serverBoosters != null && !serverBoosters.isEmpty()) {
            long currentTime = System.currentTimeMillis();

            for (String type : serverBoosters.keySet()) {
                BoosterInfo boosterInfo = serverBoosters.get(type);
                long boosterEndTime = boosterInfo.getBoosterEndTime();
                if (currentTime < boosterEndTime) {
                    double boosterFactor = boosterInfo.getBoosterFactor();
                    serverBoosterFactor *= boosterFactor;
                } else {
                    // The server-wide booster has expired, so remove it from the serverBoosters map
                    serverBoosters.remove(type);
                }
            }
        }

        // Check if there is a global booster active
        Map<String, Long> globalBoosters = getGlobalBoosters();
        if (globalBoosters != null && !globalBoosters.isEmpty()) {
            long currentTime = System.currentTimeMillis();

            for (String type : globalBoosters.keySet()) {
                long boosterEndTime = globalBoosters.get(type);
                if (currentTime < boosterEndTime) {
                    double boosterFactor = configManager.getBoosterFactor(type); // Use the correct method to get booster factor
                    serverBoosterFactor *= boosterFactor;
                } else {
                    // The global booster has expired, so remove it from the globalBoosters map
                    globalBoosters.remove(type);
                }
            }
        }

        // Calculate the total booster factor (including both server-wide and global boosters) for the player
        double totalBoosterFactor = serverBoosterFactor;
        Map<String, BoosterInfo> playerBoosters = getPlayerBoosters(player.getUniqueId());
        if (playerBoosters != null && !playerBoosters.isEmpty()) {
            long currentTime = System.currentTimeMillis();

            for (String type : playerBoosters.keySet()) {
                BoosterInfo boosterInfo = playerBoosters.get(type);
                long boosterEndTime = boosterInfo.getBoosterEndTime();
                if (currentTime < boosterEndTime) {
                    double boosterFactor = boosterInfo.getBoosterFactor();
                    totalBoosterFactor *= boosterFactor;
                } else {
                    // The player's booster has expired, so remove it from the playerBoosters map
                    playerBoosters.remove(type);
                    boosterMessagesSent.remove(player.getUniqueId()); // Remove the message sent flag for this player
                }
            }
        }

        // Apply the total booster factor to the XP
        int doubledExp = (int) Math.round(originalExp * totalBoosterFactor);
        event.setAmount(doubledExp);
    }

    // Method to add a booster for a specific type to a player
    public void addBooster(UUID playerUUID, String type, long boosterEndTime, Double boosterFactor) {
        Map<String, BoosterInfo> playerBoosters = getPlayerBoosters(playerUUID);
        if (boosterFactor == null) {
            boosterFactor = configManager.getPlayerBoosterFactor(type); // Use the default config setting if boosterFactor is not provided
        }
        BoosterInfo boosterInfo = new BoosterInfo(boosterEndTime, boosterFactor);
        playerBoosters.put(type, boosterInfo);
    }


    // Update the addServerBooster method to handle boosterFactor priority
    public void addServerBooster(String serverIdentifier, String type, long boosterEndTime, Double boosterFactor, String playerName) {
        BoosterInfo boosterInfo = new BoosterInfo(boosterEndTime, boosterFactor);
        boosterInfo.setPlayerName(playerName);
        serverBoosters.put(type, boosterInfo);

        if (type.equals("sellprice")) {
            serverBoosterEndTimes.put(serverIdentifier, boosterEndTime);
        }

        // Set the name of the player who activated the server-wide booster
        setServerBoosterActivatedBy(playerName);

        // Broadcast the activated booster to all online players
        String message = configManager.getServerBoosterActivatedMessage();

        // Replace placeholders in the message with the actual values
        message = message.replace("{type}", type).replace("{factor}", String.format("%.2fx", boosterFactor)).replace("{player}", playerName);

        // Broadcast the activated booster to all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    // Method to add a global booster for a specific type
    public void addGlobalBooster(String type, long boosterEndTime) {
        globalBoosters.put(type, boosterEndTime);
    }

    // Method to get the server-wide boosters
    public Map<String, BoosterInfo> getServerBoosters() {
        return serverBoosters;
    }

    // Method to get the global boosters
    public Map<String, Long> getGlobalBoosters() {
        return globalBoosters;
    }

    // Method to get the boosters for a specific player
    private Map<String, BoosterInfo> getPlayerBoosters(UUID playerUUID) {
        return ((ExperienceBoosters) plugin).getPlayerBoosters().computeIfAbsent(playerUUID, k -> new HashMap<>());
    }

    // Method to send the booster status to a player
    private void sendBoosterStatus(Player player) {
        Map<String, BoosterInfo> serverBoosters = getServerBoosters();
        if (serverBoosters.isEmpty()) {
            ActionBar.clearActionBar(player);
            return; // No active boosters, return early to avoid showing the action bar
        }

        String actionBarMessage = configManager.getActionBarMessage();
        long currentTime = System.currentTimeMillis();
        StringBuilder messageBuilder = new StringBuilder();

        // Check server-wide boosters
        for (String type : serverBoosters.keySet()) {
            BoosterInfo boosterInfo = serverBoosters.get(type);
            long boosterEndTime = boosterInfo.getBoosterEndTime();
            if (currentTime < boosterEndTime) {
                long remainingTimeInSeconds = (boosterEndTime - currentTime) / 1000;
                long remainingMinutes = remainingTimeInSeconds / 60;
                long remainingSeconds = remainingTimeInSeconds % 60;
                String formattedTime = String.format("%dm %ds", remainingMinutes, remainingSeconds);
                String boosterInfoStr = type + ": " + formattedTime;
                messageBuilder.append(boosterInfoStr).append(", ");
            }
        }

        // Remove the trailing ", " if there are active boosters
        if (messageBuilder.length() > 2) {
            messageBuilder.setLength(messageBuilder.length() - 2);
        }

        if (messageBuilder.length() == 0) {
            ActionBar.clearActionBar(player);
        } else {
            // Replace the {type} placeholder directly with the formatted booster information
            actionBarMessage = actionBarMessage.replace("{type}", messageBuilder.toString());
            ActionBar.sendActionBar(player, actionBarMessage);
        }
    }

    // This class needs to be static to be accessed from a static context
    private static class ActionBar {
        public static void sendActionBar(Player player, String message) {
            player.sendActionBar(ChatColor.translateAlternateColorCodes('&', message));
        }

        public static void clearActionBar(Player player) {
            player.sendActionBar(""); // Clear the action bar
        }
    }



    private String serverBoosterActivatedBy = null;

    public String getServerBoosterActivatedBy() {
        return serverBoosterActivatedBy;
    }

    public void setServerBoosterActivatedBy(String playerName) {
        this.serverBoosterActivatedBy = playerName;
    }


}

