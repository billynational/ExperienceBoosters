package me.billymn.experienceboosters;


import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

public class PlayerNamePlaceholder extends PlaceholderExpansion {

    private static final Map<String, String> playerNamesMap = new HashMap<>();

    public static void setPlayerName(String serverIdentifier, String playerName) {
        playerNamesMap.put(serverIdentifier, playerName);
    }

    public static String getPlayerName(String serverIdentifier) {
        return playerNamesMap.get(serverIdentifier);
    }

    @Override
    public boolean persist() {
        return true; // Persist the placeholder so it can be used in config.yml
    }

    @Override
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null; // Check if PlaceholderAPI is installed
    }

    @Override
    public String getIdentifier() {
        return "experienceboosters";
    }

    @Override
    public String getAuthor() {
        return "Billymn";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return null;
        }

        if ("player_who_enabled_server_wide_booster".equals(identifier)) {
            ExperienceBoosters plugin = (ExperienceBoosters) Bukkit.getPluginManager().getPlugin("ExperienceBoosters");
            if (plugin != null) {
                String playerName = plugin.getServerBoosterActivatedBy();
                return playerName != null ? playerName : "Unknown";
            }
        }

        return null; // Return null if the requested placeholder is not found
    }

    // You may need to implement a method to get the server identifier depending on how you handle server-wide boosters.
    private String getServerIdentifierFromSomeWhere() {
        // Implement this method based on your server's logic to get the server identifier.
        // It could be the server name or any other unique identifier for the server-wide booster.
        return "server1"; // Example: Hardcoded server identifier for demonstration purposes
    }
}
