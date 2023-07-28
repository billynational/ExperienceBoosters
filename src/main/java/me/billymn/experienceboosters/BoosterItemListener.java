package me.billymn.experienceboosters;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.ArrayList;
import java.util.List;

public class BoosterItemListener implements Listener {

    public static ItemStack createBoosterItem() {
        ItemStack boosterItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = boosterItem.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "XP Booster");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Use this item to activate an XP booster!");
        meta.setLore(lore);
        boosterItem.setItemMeta(meta);
        return boosterItem;
    }



    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();

        // Check if the right-clicked item is the XP Booster
        if (itemInHand != null && itemInHand.getType() == Material.EXPERIENCE_BOTTLE) {
            // Check if the player right-clicked with the item
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // Consume the booster item
                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                } else {
                    player.setItemInHand(null);
                }

                // TODO: Add code here to activate the XP booster effect
                // For example, you could add an XP boost to the player.
                // This depends on how your plugin handles XP boosting.
            }
        }
    }
}
