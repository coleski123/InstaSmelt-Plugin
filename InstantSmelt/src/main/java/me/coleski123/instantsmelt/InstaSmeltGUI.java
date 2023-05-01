package me.coleski123.instantsmelt;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstaSmeltGUI implements Listener {
    private Plugin plugin;
    private Economy economy;
    private double smeltCost;
    private boolean enableSmeltCost;

    public InstaSmeltGUI(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
        updateConfig();
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    //Read the config.yml
    private void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        smeltCost = config.getDouble("smeltCost", 20.75);
        enableSmeltCost = config.getBoolean("enableSmeltCost", true);
    }

    private void enableSmeltCost() {
        enableSmeltCost = !enableSmeltCost;
    }

    public void openGUI(Player player) {
        BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.BLUE + "InstaSmelt Config");

            loadConfig();

        // Add smelt cost item
        ItemStack smeltCostItem = new ItemStack(Material.NAME_TAG);
        ItemMeta smeltCostMeta = smeltCostItem.getItemMeta();
        smeltCostMeta.setDisplayName(ChatColor.YELLOW + "Smelt Cost");
        List<String> smeltCostLore = new ArrayList<>();
        smeltCostLore.add("");
        smeltCostLore.add(ChatColor.WHITE + "Current value: " + ChatColor.GREEN + economy.format(smeltCost));
        smeltCostLore.add("");
        smeltCostLore.add(ChatColor.GRAY + "Click to edit");
        smeltCostMeta.setLore(smeltCostLore);
        smeltCostItem.setItemMeta(smeltCostMeta);
        inventory.setItem(0, smeltCostItem);

        // Add enable smelt cost item
        ItemStack enableSmeltCostItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta enableSmeltCostMeta = enableSmeltCostItem.getItemMeta();
        enableSmeltCostMeta.setDisplayName(ChatColor.YELLOW + "Enable Smelt Cost");
        List<String> enableSmeltCostLore = new ArrayList<>();
        enableSmeltCostLore.add("");
        enableSmeltCostLore.add(ChatColor.WHITE + "Current value: " + ChatColor.GREEN + enableSmeltCost);
        enableSmeltCostLore.add("");
        enableSmeltCostLore.add(ChatColor.GRAY + "Click to toggle");
        enableSmeltCostMeta.setLore(enableSmeltCostLore);
        enableSmeltCostItem.setItemMeta(enableSmeltCostMeta);
        inventory.setItem(2, enableSmeltCostItem);

        //Save Config Item
            ItemStack reloadItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta reloadMeta = reloadItem.getItemMeta();
            reloadMeta.setDisplayName(ChatColor.YELLOW + "Save Config");
            List<String> reloadLore = new ArrayList<>();
            reloadLore.add("");
            reloadLore.add(ChatColor.GRAY + "Click to save config");
            reloadMeta.setLore(reloadLore);
            reloadItem.setItemMeta(reloadMeta);
            inventory.setItem(7, reloadItem);

            //Close Config Button
            ItemStack closeItem = new ItemStack(Material.BARRIER);
            ItemMeta closeMeta = closeItem.getItemMeta();
            closeMeta.setDisplayName(ChatColor.RED + "Close Config");
            List<String> closeLore = new ArrayList<>();
            closeLore.add("");
            closeLore.add(ChatColor.GRAY + "Click to close config");
            closeMeta.setLore(closeLore);
            closeItem.setItemMeta(closeMeta);
            inventory.setItem(8, closeItem);

            //PlaceHolder Item
            ItemStack placeholderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta placeholderItemMeta = placeholderItem.getItemMeta();
            placeholderItemMeta.setDisplayName(ChatColor.RED + "");
            placeholderItem.setItemMeta(placeholderItemMeta);

            int[] slots = {1, 3, 4, 5, 6}; // the slots where the item will be set
            for (int slot : slots) {
                inventory.setItem(slot, placeholderItem);
            }

        player.openInventory(inventory);
    }
    };
    runnable.runTask(plugin);
        }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws IOException {
        if (!ChatColor.stripColor(event.getView().getTitle()).equals("InstaSmelt Config")) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        switch (clickedItem.getType()) {
            case NETHER_STAR:
                //Reload the plugin/config
                updateConfig();
                player.closeInventory();
                InstantSmelt.getInstance().reload();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "Config has been saved!");
                break;
            case NAME_TAG:
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1);
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "Enter the new smelt cost in the chat. Type" + ChatColor.RED + "'cancel'" + ChatColor.YELLOW + "to cancel.");
                player.setMetadata("instasmelt:editing", new FixedMetadataValue(plugin, new InstaSmeltMetadata(true, "smeltCost")));
                break;
            case BARRIER:
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1);
                player.closeInventory();
                break;
            case IRON_INGOT:
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1);
                enableSmeltCost();
                openGUI(player);
                break;
            default:
                break;
        }
        updateConfig();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasMetadata("instasmelt:editing")) {
            return;
        }

        InstaSmeltMetadata metadata = (InstaSmeltMetadata) event.getPlayer().getMetadata("instasmelt:editing").get(0).value();

        if (metadata.getType().equals("smeltCost")) {
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                event.getPlayer().sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "Smelt cost editing cancelled.");
                event.getPlayer().removeMetadata("instasmelt:editing", plugin);
                return;
            }

            try {
                double newSmeltCost = Double.parseDouble(event.getMessage());
                if (newSmeltCost < 0) {
                    event.getPlayer().sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.RED + "The smelt cost must be a positive number.");
                    return;
                }

                smeltCost = newSmeltCost;
                updateConfig();
                event.getPlayer().sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "Smelt cost updated to " + economy.format(newSmeltCost));
                event.getPlayer().removeMetadata("instasmelt:editing", plugin);
                openGUI(event.getPlayer());

                //Cancel the chat event to prevent the message from being broadcasted.
                event.setCancelled(true);
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.RED + "Invalid number format. Please enter a valid number or type 'cancel'.");
            }
        }
    }

    private void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        config.set("smeltCost", smeltCost);
        config.set("enableSmeltCost", enableSmeltCost);

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class InstaSmeltMetadata {
        private boolean editing;
        private String type;

        public InstaSmeltMetadata(boolean editing, String type) {
            this.editing = editing;
            this.type = type;
        }

        public boolean isEditing() {
            return editing;
        }

        public String getType() {
            return type;
        }
    }
}