package me.billymn.experienceboosters;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExperienceBoosters extends JavaPlugin {

    private final Map<UUID, Map<String, BoosterInfo>> playerBoosters = new HashMap<>();
    private ExperienceBoosterListener experienceBoosterListener;
    private ConfigManager configManager;
    private SellPriceBooster sellPriceBooster;

    private void registerEventListeners() {
        // Register the ShopSellListener event listener
        getServer().getPluginManager().registerEvents(new ShopSellListener(sellPriceBooster), this);
    }

    @Override
    public void onEnable() {
        getLogger().info("ExperienceBooster plugin has been enabled.");
        configManager = new ConfigManager(this);
        experienceBoosterListener = new ExperienceBoosterListener(this, configManager);
        new GiveBoosterCommand(this);
        new PlayerNamePlaceholder().register();

        sellPriceBooster = new SellPriceBooster(this);
        registerEventListeners();

        // Register the /reloadconfig command only if the plugin is enabled
        if (isEnabled()) {
            getCommand("reloadconfig").setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof Player && !sender.hasPermission("experiencebooster.reloadconfig")) {
                    sender.sendMessage(configManager.getPlayerNoPermissionMessage());
                    return true;
                }

                configManager.reloadConfig();
                sender.sendMessage(configManager.getReloadConfigMessage());
                return true;
            });

            getCommand("enablebooster").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be used by players.");
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("experiencebooster.enablebooster")) {
                    player.sendMessage(configManager.getPlayerNoPermissionMessage());
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("Usage: /enablebooster <type> <duration_in_minutes> [booster_factor]");
                    return true;
                }

                String type = args[0].toLowerCase();
                int duration;
                try {
                    duration = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid duration format. Please use a number for the duration.");
                    return true;
                }

                if (!type.equals("mining") && !type.equals("farming") && !type.equals("pve") && !type.equals("global")) {
                    player.sendMessage("Invalid booster type. Supported types: mining, farming, pve, global.");
                    return true;
                }

                Double boosterFactor = null;
                if (args.length >= 3) {
                    try {
                        boosterFactor = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid booster factor. Please use a number for the factor.");
                        return true;
                    }
                }

                long boosterEndTime = System.currentTimeMillis() + duration * 60 * 1000;

                experienceBoosterListener.addBooster(player.getUniqueId(), type, boosterEndTime, boosterFactor);

                player.sendMessage("The " + type + " booster is now active for " + duration + " minutes"
                        + (boosterFactor != null ? " with a factor of " + boosterFactor : "") + "!");
                return true;
            });

            getCommand("enableserverbooster").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be used by players.");
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("experiencebooster.enableserverbooster")) {
                    player.sendMessage(configManager.getPlayerNoPermissionMessage());
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("Usage: /enableserverbooster <type> <duration_in_minutes> [booster_factor]");
                    return true;
                }

                String type = args[0].toLowerCase();
                int duration;
                try {
                    duration = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid duration format. Please use a number for the duration.");
                    return true;
                }

                if (!type.equals("mining") && !type.equals("farming") && !type.equals("pve") && !type.equals("global") && !type.equals("sellprice")) {
                    player.sendMessage("Invalid booster type. Supported types: mining, farming, pve, global, and sellprice.");
                    return true;
                }

                Double boosterFactor = null;
                if (args.length >= 3) {
                    try {
                        boosterFactor = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid booster factor. Please use a number for the factor.");
                        return true;
                    }
                }

                long boosterEndTime = System.currentTimeMillis() + duration * 60 * 1000;

                String serverIdentifier = generateUniqueServerIdentifier(Bukkit.getServer().getName(), type);

                // Add the server-wide booster using the ExperienceBoosterListener
                experienceBoosterListener.addServerBooster(serverIdentifier, type, boosterEndTime, boosterFactor, player.getName());

                // Schedule the removal of the server-wide booster after the specified duration
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // This code will be executed after the specified duration
                    sellPriceBooster.removeServerBooster(serverIdentifier); // Pass the duration to the removeServerBooster method
                }, duration * 60 * 20);

                if (type.equals("sellprice")) {
                    // Handle sell price booster separately
                    SellPriceBooster sellPriceBooster = getSellPriceBooster(); // Get the instance of SellPriceBooster
                    if (sellPriceBooster != null) {
                        sellPriceBooster.addServerBooster(serverIdentifier, type, boosterEndTime, boosterFactor, player.getName());
                    } else {
                        player.sendMessage("SellPriceBooster not available.");
                    }
                } else {
                    // For other types, handle as usual
                    experienceBoosterListener.addServerBooster(generateUniqueServerIdentifier(Bukkit.getServer().getName(), type), type, boosterEndTime, boosterFactor, player.getName());
                }

                // Set the player's name for the specific server-wide booster
                PlayerNamePlaceholder.setPlayerName(serverIdentifier, player.getName());

                player.sendMessage("The server-wide " + type + " booster is now active for " + duration + " minutes"
                        + (boosterFactor != null ? " with a factor of " + boosterFactor : "") + "!");

                setServerBoosterActivatedBy(player.getName());

                String message = configManager.getServerBoosterActivatedMessage();
                if (message.isEmpty()) {
                    message = "&aA server-wide {type} booster has been activated by {player} for {factor}x XP!";
                }

                // Replace placeholders with actual values
                message = message.replace("{type}", type);
                message = message.replace("{factor}", String.format("%.2fx", boosterFactor != null ? boosterFactor : 1.0));
                message = message.replace("{player}", player.getName());

                Bukkit.broadcastMessage(message);

                return true;
            });

            getCommand("boosterinfo").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be used by players.");
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("experiencebooster.boosterinfo")) {
                    player.sendMessage(configManager.getPlayerNoPermissionMessage());
                    return true;
                }

                Map<String, BoosterInfo> playerBoosters = this.playerBoosters.getOrDefault(player.getUniqueId(), new HashMap<>());

                long currentTime = System.currentTimeMillis();

                StringBuilder message = new StringBuilder();
                message.append("Active boosters:");

                for (String type : playerBoosters.keySet()) {
                    BoosterInfo boosterInfo = playerBoosters.get(type);
                    long boosterEndTime = boosterInfo.getBoosterEndTime();

                    if (currentTime < boosterEndTime) {
                        long remainingTimeInSeconds = (boosterEndTime - currentTime) / 1000;
                        long remainingMinutes = remainingTimeInSeconds / 60;
                        long remainingSeconds = remainingTimeInSeconds % 60;

                        message.append("\n- ").append(type).append(": ").append(remainingMinutes).append("m ").append(remainingSeconds).append("s");
                    } else {
                        // The booster has expired, so remove it from the player's boosters map
                        playerBoosters.remove(type);
                    }
                }

                if (message.toString().equals("Active boosters:")) {
                    message.append(" None");
                }

                player.sendMessage(message.toString());
                return true;
            });
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("ExperienceBooster plugin has been disabled.");
    }

    public Map<UUID, Map<String, BoosterInfo>> getPlayerBoosters() {
        return playerBoosters;
    }

    public Map<String, BoosterInfo> getPlayerBoosters(UUID playerUUID) {
        return playerBoosters.getOrDefault(playerUUID, new HashMap<>());
    }

    public Map<String, BoosterInfo> getServerBoosters() {
        return experienceBoosterListener.getServerBoosters();
    }

    private String serverBoosterActivatedBy = null;

    public String getServerBoosterActivatedBy() {
        return serverBoosterActivatedBy;
    }

    public void setServerBoosterActivatedBy(String playerName) {
        this.serverBoosterActivatedBy = playerName;
    }

    // Implement the method to generate a unique server identifier for the server-wide booster
    private String generateUniqueServerIdentifier(String serverName, String boosterType) {
        UUID randomUUID = UUID.randomUUID();
        return serverName + "_" + boosterType + "_" + randomUUID.toString();
    }

    public void removeServerBooster(String serverIdentifier, long duration) {
        // Schedule the removal of the server-wide booster after the specified duration
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Retrieve the SellPriceBooster instance from the ExperienceBoosters plugin
            SellPriceBooster sellPriceBooster = getSellPriceBooster();
            if (sellPriceBooster == null) {
                return; // SellPriceBooster not available
            }

            sellPriceBooster.removeServerBooster(serverIdentifier); // Pass the server identifier to the removeServerBooster method

            // Other actions to take when the server-wide booster expires
        }, duration * 20); // Convert duration from minutes to ticks (1 minute = 20 ticks)
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SellPriceBooster getSellPriceBooster() {
        return sellPriceBooster;
    }

    // ... (other methods)
}
