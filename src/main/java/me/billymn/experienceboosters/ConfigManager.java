package me.billymn.experienceboosters;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final ConfigurationSection messagesConfig;
    private final ConfigurationSection boostersConfig;
    private final ConfigurationSection permissionsConfig;
    private FileConfiguration config; // Add this line to declare the member variable

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reloadConfig(); // Call the reloadConfig method when initializing
        this.config = plugin.getConfig();
        this.messagesConfig = plugin.getConfig().getConfigurationSection("messages");
        this.boostersConfig = plugin.getConfig().getConfigurationSection("boosters");
        this.permissionsConfig = plugin.getConfig().getConfigurationSection("permissions");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig(); // Update the config member variable after reloading

    }


    public String getBoosterEnabledMessage(String type, int duration) {
        String message = messagesConfig.getString("boosterEnabled");
        message = message.replace("%type%", type).replace("%duration%", String.valueOf(duration));
        return Utils.colorize(message);
    }

    public String getBoosterExpiredMessage(String type) {
        String message = messagesConfig.getString("boosterExpired");
        message = message.replace("%type%", type);
        return Utils.colorize(message);
    }

    public String getPlayerNoPermissionMessage() {
        return Utils.colorize(messagesConfig.getString("noPermission"));
    }

    public int getBoosterDuration(String type) {
        return boostersConfig.getInt(type);
    }

    public String getReloadConfigMessage() {
        String message = plugin.getConfig().getString("messages.reloadConfig");
        if (message == null) {
            // Provide a default message if the reloadConfig message is not found in the config.yml
            message = "&aConfiguration reloaded successfully.";
        }
        return Utils.colorize(message);
    }

    public String getPermission(String permissionType) {
        return permissionsConfig.getString(permissionType);
    }

    // Add the method to retrieve the booster factor for a specific type
    public double getBoosterFactor(String type) {
        String path = "boosters." + type + ".booster_factor";
        if (config.contains(path)) {
            return config.getDouble(path);
        } else {
            return 1.0; // Return 1.0 as the default factor if no booster_factor is specified in the config
        }
    }

    public String getActionBarMessage() {
        String message = config.getString("action-bar-message", "&aActive boosters: {boosters}");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // Add the method to retrieve the server-wide booster factor for a specific type
    public double getServerBoosterFactor(String type) {
        return config.getDouble("serverBoosters." + type);
    }

    // Add the method to retrieve the player-specific booster factor for a specific type
    public double getPlayerBoosterFactor(String type) {
        return config.getDouble("playerBoosters." + type);
    }

    // Remove this method as it's not needed
    // public double getGlobalBoosterFactor() {
    //     return configManager.getBoosterFactor("global");
    // }

    // Add the method to retrieve the global booster factor
    public double getGlobalBoosterFactor() {
        return config.getDouble("global");
    }

    public String getServerBoosterActivatedMessage() {
        String message = config.getString("messages.serverBoosterActivated");
        if (message == null) {
            // Provide a default message if the serverBoosterActivated message is not found in the config.yml
            message = "&aA server-wide {type} booster has been activated for {factor}x XP!";
        }
        return Utils.colorize(message);
    }

    private String replacePlaceholders(String message) {
        if (message.isEmpty()) {
            return "";
        }

        // Replace {player} placeholder with the player's name
        if (message.contains("{player}")) {
            message = message.replace("{player}", "{PLAYER_NAME}");
        }

        // Replace other placeholders as needed
        // For example, if there were more placeholders like {type} or {factor}, you can replace them here.

        return Utils.colorize(message);
    }

    public String applyPlaceholders(Player player, String message) {
        if (player == null) return message; // Return the message as-is if the player is null
        return PlaceholderAPI.setPlaceholders(player, message);
    }

}

