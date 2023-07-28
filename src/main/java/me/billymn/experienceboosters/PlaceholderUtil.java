package me.billymn.experienceboosters;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlaceholderUtil {

    private final JavaPlugin plugin;

    public PlaceholderUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String replacePlaceholders(Player player, String message) {
        // Replace the custom placeholder %player_name% with the actual player's name using PlaceholderAPI
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }
}
