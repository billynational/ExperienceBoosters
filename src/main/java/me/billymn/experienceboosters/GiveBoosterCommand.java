package me.billymn.experienceboosters;

import me.billymn.experienceboosters.BoosterItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class GiveBoosterCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public GiveBoosterCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("givebooster").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("experiencebooster.givebooster")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /givebooster <player>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }

        // Create the booster item
        ItemStack boosterItem = BoosterItem.createBoosterItem();

        // Give the booster item to the target player
        targetPlayer.getInventory().addItem(boosterItem);

        // Inform the sender that the booster has been given
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            if (playerSender.equals(targetPlayer)) {
                playerSender.sendMessage(ChatColor.GREEN + "You have received an XP booster!");
            } else {
                playerSender.sendMessage(ChatColor.GREEN + "You have given an XP booster to " + targetPlayer.getName() + "!");
                targetPlayer.sendMessage(ChatColor.GREEN + "You have received an XP booster from " + playerSender.getName() + "!");
            }
        } else {
            // Console sender
            sender.sendMessage(ChatColor.GREEN + "You have given an XP booster to " + targetPlayer.getName() + "!");
            targetPlayer.sendMessage(ChatColor.GREEN + "You have received an XP booster from the console!");
        }

        return true;
    }
}
