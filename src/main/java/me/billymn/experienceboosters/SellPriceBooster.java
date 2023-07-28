package me.billymn.experienceboosters;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.shop.ShopsNotLoadedException;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SellPriceBooster {

    private final Map<ShopItem, Double> originalShopPrices = new HashMap<>();


    private final Map<String, Double> serverBoosterEffects = new HashMap<>();
    private final Map<UUID, Double> playerBoosterEffects = new HashMap<>();
    private final ExperienceBoosters plugin;

    public SellPriceBooster(ExperienceBoosters plugin) {
        this.plugin = plugin;
    }

    public double getPlayerBoosterFactor(UUID playerUUID) {
        return playerBoosterEffects.getOrDefault(playerUUID, 1.0);
    }

    public void addSellPriceBooster(UUID playerUUID, double boosterFactor) {
        playerBoosterEffects.put(playerUUID, boosterFactor);
        applySellPriceBooster(playerUUID, boosterFactor);
    }

    public void removeSellPriceBooster(UUID playerUUID) {
        playerBoosterEffects.remove(playerUUID);
        removeSellPriceBoosterModifier(playerUUID);
    }

    public void addServerBooster(String serverIdentifier, String type, long boosterEndTime, Double boosterFactor, String playerName) {
        serverBoosterEffects.put(serverIdentifier, boosterFactor);
        applyServerSellPriceBooster(serverIdentifier, boosterFactor);
        scheduleServerBoosterExpiration(serverIdentifier, boosterEndTime);
    }

    private void applyServerSellPriceBooster(String serverIdentifier, double boosterFactor) {
        System.out.println("Applying server sell price booster with factor: " + boosterFactor);

        try {
            // Iterate through all shops and apply the booster to their items
            for (Shop shop : ShopGuiPlusApi.getPlugin().getShopManager().getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    applySellPriceBoosterToShopItem(shopItem, boosterFactor);
                }
            }
        } catch (ShopsNotLoadedException e) {
            e.printStackTrace();
        }
    }

    private void scheduleServerBoosterExpiration(String serverIdentifier, long boosterEndTime) {
        // Calculate the duration until the booster expires
        long currentTime = System.currentTimeMillis();
        long duration = boosterEndTime - currentTime;

        // Schedule the removal of the server-wide booster after the specified duration
        new BukkitRunnable() {
            @Override
            public void run() {
                removeServerBooster(serverIdentifier);
            }
        }.runTaskLater(plugin, duration / 50); // Convert duration from milliseconds to ticks (1 tick = 50 milliseconds)
    }

    public void removeServerBooster(String serverIdentifier) {
        serverBoosterEffects.remove(serverIdentifier);
        applyServerSellPriceBooster(serverIdentifier, 1.0); // Revert to default boosterFactor of 1.0
        resetShopPrices();
    }

    private void resetShopPrices() {
        try {
            // Iterate through all shops and reset the sell prices of their items
            for (Shop shop : ShopGuiPlusApi.getPlugin().getShopManager().getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    double originalSellPrice = originalShopPrices.getOrDefault(shopItem, shopItem.getSellPrice());
                    shopItem.setSellPrice(originalSellPrice);
                }
            }
        } catch (ShopsNotLoadedException e) {
            e.printStackTrace();
        }
    }

    private void storeOriginalShopPrices() {
        try {
            // Iterate through all shops and store the original sell prices of their items
            for (Shop shop : ShopGuiPlusApi.getPlugin().getShopManager().getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    originalShopPrices.put(shopItem, shopItem.getSellPrice());
                }
            }
        } catch (ShopsNotLoadedException e) {
            e.printStackTrace();
        }
    }


private void applySellPriceBoosterToShopItem(ShopItem shopItem, double boosterFactor) {
        ItemStack item = shopItem.getItem();
        double originalSellPrice = shopItem.getSellPrice();
        double modifiedSellPrice = originalSellPrice * boosterFactor;
        shopItem.setSellPrice(modifiedSellPrice);
    }

    public void applySellPriceBooster(UUID playerUUID, double boosterFactor) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            return;
        }

        // Get the player's inventory item
        ItemStack item = player.getInventory().getItemInMainHand();

        // Get the shop item from the item stack
        ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(item);
        if (shopItem == null) {
            return; // Item is not in any shop
        }

        // Get the current sell price of the item
        double originalSellPrice = shopItem.getSellPrice();

        // Apply the sell price booster factor to the original sell price
        double modifiedSellPrice = originalSellPrice * boosterFactor;

        // Update the sell price of the shop item
        shopItem.setSellPrice(modifiedSellPrice);

        // Inform the player that the sell price booster has been activated or deactivated
        String message = plugin.getConfigManager().getBoosterEnabledMessage("Sell Price Booster", 0);
        message = message.replace("{factor}", String.format("%.2fx", boosterFactor));
        message = Utils.colorize(message);
        player.sendMessage(message);

        // Update the associated item stack in the player's inventory
        ShopGuiPlusApi.getItemStackShop(player, item);
    }

    public void removeSellPriceBoosterModifier(UUID playerUUID) {
        playerBoosterEffects.remove(playerUUID);

        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            return;
        }

        // Iterate through all shops and revert the sell prices for the player's items
        try {
            for (Shop shop : ShopGuiPlusApi.getPlugin().getShopManager().getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    ItemStack item = shopItem.getItem();
                    ShopItem originalShopItem = ShopGuiPlusApi.getItemStackShopItem(item);

                    // Check if the shop item is from the same shop and has the same sell price booster factor
                    if (originalShopItem != null && originalShopItem.getSellPrice() == shopItem.getSellPrice()) {
                        applySellPriceBoosterToShopItem(shopItem, 1.0); // Revert to default boosterFactor of 1.0
                    }
                }
            }
        } catch (ShopsNotLoadedException e) {
            e.printStackTrace();
        }

        // Update the associated item stacks in the player's inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                ShopGuiPlusApi.getItemStackShop(player, item);
            }
        }
    }


}
