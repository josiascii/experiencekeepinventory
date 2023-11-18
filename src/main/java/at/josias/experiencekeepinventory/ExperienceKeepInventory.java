package at.josias.experiencekeepinventory;

import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExperienceKeepInventory extends JavaPlugin implements CommandExecutor, Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("xpcheck")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int player_level = player.getLevel();
                float player_exp = player.getExp() * player.getExpToLevel();
                int amount_of_items = amountOfItems(player);
                int total_exp = (int) player_exp + levelToExp(player_level);
                if (total_exp >= amount_of_items && amount_of_items != 0) {
                    player.sendMessage(ChatColor.DARK_GREEN + "Enough XP [" + ChatColor.GRAY + total_exp + ChatColor.DARK_GREEN + "] to save your items [" + ChatColor.GRAY + amount_of_items + ChatColor.DARK_GREEN + "]");
                } else if (total_exp < amount_of_items && amount_of_items != 0) {
                    int exp_needed = amount_of_items - total_exp;
                    player.sendMessage(ChatColor.DARK_RED + "You need " + ChatColor.GRAY + exp_needed + ChatColor.DARK_RED + " more XP [" + ChatColor.GRAY + total_exp + ChatColor.DARK_RED + "] to save your items [" + ChatColor.GRAY + amount_of_items + ChatColor.DARK_RED + "]");
                } else {
                    player.sendMessage("You have nothing to lose :)");
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY) == Boolean.TRUE) {
            int player_level = player.getLevel();
            float player_exp = player.getExp() * player.getExpToLevel();
            int amount_of_items = amountOfItems(player);
            int total_exp = (int) player_exp + levelToExp(player_level);
            if (total_exp >= amount_of_items && amount_of_items != 0) {
                player.giveExp(-amount_of_items);
                int dropped_exp = total_exp - amount_of_items;
                if (dropped_exp > 100) {
                    dropped_exp = 100;
                }
                event.setDroppedExp(dropped_exp);
                player.setExp(0);
                player.setLevel(0);
                player.sendMessage(ChatColor.DARK_GREEN + "Items [" + ChatColor.GRAY + amount_of_items + ChatColor.DARK_GREEN + "] kept due to enough XP [" + ChatColor.GRAY + total_exp + ChatColor.DARK_GREEN + "]");
            } else if (total_exp < amount_of_items && amount_of_items != 0) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
                int dropped_exp = total_exp;
                if(dropped_exp > 100) {
                    dropped_exp = 100;
                }
                event.setDroppedExp(dropped_exp);
                player.setExp(0);
                player.setLevel(0);
                player.getInventory().clear();
                player.sendMessage(ChatColor.DARK_RED + "Items [" + ChatColor.GRAY + amount_of_items + ChatColor.DARK_RED + "] dropped due to insufficient XP [" + ChatColor.GRAY + total_exp + ChatColor.DARK_RED + "]");
            } else {
                if(total_exp > 100) {
                    total_exp = 100;
                }
                event.setDroppedExp(total_exp);
                player.setExp(0);
                player.setLevel(0);
            }
        }
    }

    private int amountOfItems(Player player) {
        int amount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    private int levelToExp(int player_level) {
        if (player_level <= 16) {
            return (int) (Math.pow(player_level, 2) + 6 * player_level);
        } else if (player_level <= 31) {
            return (int) (2.5 * Math.pow(player_level, 2) - 40.5 * player_level + 360);
        } else {
            return (int) (4.5 * Math.pow(player_level, 2) - 162.5 * player_level + 2220);
        }
    }

}