package me.billymn.experienceboosters;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BoosterItem {

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
}
