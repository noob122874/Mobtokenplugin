/*
 * Plugin: Mob Tokens - Life & Revival
 * Minecraft Version: 1.21.1 (Paper)
 * Description: Each player receives a unique mob token on first join with powers, life system, death punishment, and revival mechanism.
 */

package com.muskan.mobtokens;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MobTokensPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> playerLives = new HashMap<>();
    private final Set<UUID> receivedToken = new HashSet<>();
    private final Random random = new Random();

    private final String[] normalTokens = {
            "Zombie", "Skeleton", "Creeper", "Spider", "Enderman",
            "Blaze", "Witch", "Slime", "Magma Cube", "Piglin",
            "Drowned", "Husk", "Phantom", "Guardian", "Pillager",
            "Vindicator", "Evoker", "Cave Spider", "Silverfish", "Stray"
    };

    private final String[] superRareTokens = {
            "Wither", "Ender Dragon", "Warden", "Elder Guardian", "Ghast King"
    };

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Mob Tokens Plugin Enabled");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!playerLives.containsKey(player.getUniqueId())) {
            playerLives.put(player.getUniqueId(), 10);
        }
        if (!receivedToken.contains(player.getUniqueId())) {
            new BukkitRunnable() {
                public void run() {
                    openTokenSpinner(player);
                    receivedToken.add(player.getUniqueId());
                }
            }.runTaskLater(this, 20L); // Delay for safety
        }
    }

    private void openTokenSpinner(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Token Spinner");

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, createGlass());
        }

        int index = random.nextInt(25);
        String tokenName;
        if (index < 20) {
            tokenName = normalTokens[index];
        } else {
            tokenName = superRareTokens[index - 20];
        }

        ItemStack token = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = token.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + tokenName + " Token");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Power 1: TBD");
        lore.add(ChatColor.GRAY + "Power 2: TBD");
        lore.add(ChatColor.GRAY + "Power 3: TBD");
        meta.setLore(lore);
        token.setItemMeta(meta);

        inv.setItem(13, token);
        player.openInventory(inv);
        player.getInventory().addItem(token);
        player.sendMessage(ChatColor.AQUA + "You received: " + ChatColor.GOLD + tokenName + " Token!");
    }

    private ItemStack createGlass() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        int lives = playerLives.getOrDefault(uuid, 10) - 1;
        playerLives.put(uuid, lives);

        if (lives <= 0) {
            player.kickPlayer(ChatColor.RED + "You are banned for 64 hours due to losing all your lives.");
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), "Lost all lives", new Date(System.currentTimeMillis() + (64 * 60 * 60 * 1000)), null);
            dropRevivalToken(player);
        }
    }

    private void dropRevivalToken(Player player) {
        ItemStack revivalToken = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = revivalToken.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Revival Token: " + player.getName());
        revivalToken.setItemMeta(meta);
        player.getWorld().dropItemNaturally(player.getLocation(), revivalToken);
    }

    @EventHandler
    public void onUseRevival(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.TOTEM_OF_UNDYING && item.getItemMeta().getDisplayName().contains("Revival Token")) {
            String targetName = ChatColor.stripColor(item.getItemMeta().getDisplayName().replace("Revival Token: ", ""));
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(targetName);
            player.sendMessage(ChatColor.GREEN + "You have revived: " + targetName);
            item.setAmount(0);
        }
    }
}
