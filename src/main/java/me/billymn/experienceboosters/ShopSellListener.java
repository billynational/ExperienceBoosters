package me.billymn.experienceboosters;


import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShopSellListener implements Listener {

    private final SellPriceBooster sellPriceBooster;

    public ShopSellListener(SellPriceBooster sellPriceBooster) {
        this.sellPriceBooster = sellPriceBooster;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShopPreTransaction(ShopPreTransactionEvent event) {
        System.out.println("ShopPreTransactionEvent triggered");
        if (event == null || event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player == null) return;

        // Get the sell price booster factor for the player
        double boosterFactor = sellPriceBooster.getPlayerBoosterFactor(player.getUniqueId());

        ShopItem shopItem = event.getShopItem();
        if (shopItem == null) return;

        double originalSellPrice = shopItem.getSellPrice();

        // Apply the sell price booster factor to the original sell price
        double modifiedSellPrice = originalSellPrice * boosterFactor;

        // Set the modified sell price for the item
        event.setPrice(modifiedSellPrice);

        // For debugging, print the values to the console
        System.out.println("Original Sell Price: " + originalSellPrice);
        System.out.println("Booster Factor: " + boosterFactor);
        System.out.println("Modified Sell Price: " + modifiedSellPrice);
    }
}
