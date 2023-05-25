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
    public static boolean instasmeltcurrencyplacement = false;
    private Plugin plugin;
    private Economy economy;
    private double smeltCost;
    private boolean enableSmeltCost;
    private String smeltCostMessage;
    private String instasmeltPrefix;
    private String smeltCostMessageFree;
    private String notenoughMoneylang;
    private String moneyChargedlang;
    private String cannotbeSmelted;
    private String nopermissionFail;
    private String configcmdFail;
    private String configGUI1;
    private String configGUI1Value;
    private String configGUI1Underscore;
    private String configGUI2;
    private String configGUI2Value;
    private String configGUI2Underscore;
    private String configGUI3;
    private String configGUI3Underscore;
    private String configGUI4;
    private String configGUI4Underscore;
    private String configGUISaveMSG;
    private String configGUIeditCostMSG;
    private String configGUIeditCostCancelMSG;
    private String configGUIPositiveNumberMSG;
    private String configGUISmeltCostSetToMessage;
    private String configGUIInvalidNumberFormat;
    private String smeltingmessage;
    private String configGUITitle;
    public static String instasmeltcurrency;
//    private boolean instasmeltcurrencyplacement;
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
        instasmeltcurrency = config.getString("currency", "$");
        instasmeltcurrencyplacement = config.getBoolean("iscurrencyinfront", true);
        instasmeltPrefix = config.getString("messages.Prefix", "&6[InstaSmelt] ");
        nopermissionFail = config.getString("messages.NoPermission", "&cYou do not have permission to use this command!");
        configcmdFail = config.getString("messages.PlayerOnlyCmd", "&cThis command can only be run by a player.");
        smeltCostMessage = config.getString("messages.SmeltCost", "&eThe current smelt cost is ");
        smeltCostMessageFree = config.getString("messages.SmeltCostFree", "&2FREE! ");
        notenoughMoneylang = config.getString("messages.NotEnoughMoney", "&cYou do not have enough money to smelt ");
        moneyChargedlang = config.getString("messages.Charged", "&eYou have been charged ");
        cannotbeSmelted = config.getString("messages.smelting.SmeltFail", "&eThat item cannot be smelted!");
        smeltingmessage = config.getString("messages.smelting.Smelt-Success", "&eYour {ITEM1} has been smelted into {ITEM2} and you received {XPAMOUNT}XP!");
        configGUITitle = config.getString("messages.ConfigGUI.GUI Title", "&9InstaSmelt Config");
        configGUI1 = config.getString("messages.ConfigGUI.NameTagTitle", "&eSmelt Cost");
        configGUI1Value = config.getString("messages.ConfigGUI.NameTagValue", "&fCurrent value: ");
        configGUI1Underscore = config.getString("messages.ConfigGUI.NameTagClickMSG", "&7Click to edit");
        configGUI2 = config.getString("messages.ConfigGUI.IronIngotTitle", "&eEnable Smelt Cost");
        configGUI2Value = config.getString("messages.ConfigGUI.IronIngotValue", "&fCurrent value: ");
        configGUI2Underscore = config.getString("messages.ConfigGUI.IronIngotClickMSG", "&7Click to toggle");
        configGUI3 = config.getString("messages.ConfigGUI.NetherStarTitle", "&eSave Config");
        configGUI3Underscore = config.getString("messages.ConfigGUI.NetherStarClickMSG", "&7Click to save config");
        configGUI4 = config.getString("messages.ConfigGUI.BarrierTitle", "&cClick to close config");
        configGUI4Underscore = config.getString("messages.ConfigGUI.BarrierClickMSG", "&7Click to close config");
        configGUISaveMSG = config.getString("messages.ConfigGUI.ConfigSaveMessage", "&eConfig has been saved!");
        configGUIeditCostMSG = config.getString("messages.ConfigGUI.EditSmeltCostMessage", "&eEnter the new smelt cost in the chat. Type &c'cancel' &eto cancel.");
        configGUIeditCostCancelMSG = config.getString("messages.ConfigGUI.CancelEditSmeltCostMessage", "&eSmelt cost editing cancelled.");
        configGUIPositiveNumberMSG = config.getString("messages.ConfigGUI.SmeltCostPositiveNumberMessage", "&cThe smelt cost must be a positive number.");
        configGUISmeltCostSetToMessage = config.getString("messages.ConfigGUI.SmeltCostSetToMessage", "&eSmelt cost updated to ");
        configGUIInvalidNumberFormat = config.getString("messages.ConfigGUI.InvalidNumberFormatMessage", "&cInvalid number format. Please enter a valid number or type 'cancel'.");
    }

    private void enableSmeltCost() {
        enableSmeltCost = !enableSmeltCost;
    }

    public void openGUI(Player player) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Inventory inventory = Bukkit.createInventory(null, 9, configGUITitle.replace("&", "§"));

                loadConfig();
                // Add smelt cost item
                ItemStack smeltCostItem = new ItemStack(Material.NAME_TAG);
                ItemMeta smeltCostMeta = smeltCostItem.getItemMeta();
                smeltCostMeta.setDisplayName(configGUI1.replace("&", "§"));
                List<String> smeltCostLore = new ArrayList<>();
                smeltCostLore.add("");
                if (instasmeltcurrencyplacement) {
                    smeltCostLore.add(configGUI1Value.replace("&", "§") + ChatColor.GREEN + instasmeltcurrency + smeltCost);
                } else {
                    smeltCostLore.add(configGUI1Value.replace("&", "§") + ChatColor.GREEN + smeltCost + instasmeltcurrency);
                }
                smeltCostLore.add("");
                smeltCostLore.add(configGUI1Underscore.replace("&", "§"));
                smeltCostMeta.setLore(smeltCostLore);
                smeltCostItem.setItemMeta(smeltCostMeta);
                inventory.setItem(0, smeltCostItem);

                // Add enable smelt cost item
                ItemStack enableSmeltCostItem = new ItemStack(Material.IRON_INGOT);
                ItemMeta enableSmeltCostMeta = enableSmeltCostItem.getItemMeta();
                enableSmeltCostMeta.setDisplayName(configGUI2.replace("&", "§"));
                List<String> enableSmeltCostLore = new ArrayList<>();
                enableSmeltCostLore.add("");
                enableSmeltCostLore.add(configGUI2Value.replace("&", "§") + ChatColor.GREEN + enableSmeltCost);
                enableSmeltCostLore.add("");
                enableSmeltCostLore.add(configGUI2Underscore.replace("&", "§"));
                enableSmeltCostMeta.setLore(enableSmeltCostLore);
                enableSmeltCostItem.setItemMeta(enableSmeltCostMeta);
                inventory.setItem(2, enableSmeltCostItem);

                //Save Config Item
                ItemStack reloadItem = new ItemStack(Material.NETHER_STAR);
                ItemMeta reloadMeta = reloadItem.getItemMeta();
                reloadMeta.setDisplayName(configGUI3.replace("&", "§"));
                List<String> reloadLore = new ArrayList<>();
                reloadLore.add("");
                reloadLore.add(configGUI3Underscore.replace("&", "§"));
                reloadMeta.setLore(reloadLore);
                reloadItem.setItemMeta(reloadMeta);
                inventory.setItem(7, reloadItem);

                //Close Config Button
                ItemStack closeItem = new ItemStack(Material.BARRIER);
                ItemMeta closeMeta = closeItem.getItemMeta();
                closeMeta.setDisplayName(configGUI4.replace("&", "§"));
                List<String> closeLore = new ArrayList<>();
                closeLore.add("");
                closeLore.add(configGUI4Underscore.replace("&", "§"));
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
                player.sendMessage(instasmeltPrefix.replace("&", "§") + configGUISaveMSG.replace("&", "§"));
                break;
            case NAME_TAG:
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1);
                player.closeInventory();
                player.sendMessage(instasmeltPrefix.replace("&", "§") + configGUIeditCostMSG.replace("&", "§"));
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
                event.getPlayer().sendMessage(instasmeltPrefix.replace("&", "§") + configGUIeditCostCancelMSG.replace("&", "§"));
                event.getPlayer().removeMetadata("instasmelt:editing", plugin);
                return;
            }

            try {
                double newSmeltCost = Double.parseDouble(event.getMessage());
                if (newSmeltCost < 0) {
                    event.getPlayer().sendMessage(instasmeltPrefix.replace("&", "§") + configGUIPositiveNumberMSG.replace("&", "§"));
                    return;
                }

                smeltCost = newSmeltCost;
                updateConfig();
                if (instasmeltcurrencyplacement) {
                    event.getPlayer().sendMessage(instasmeltPrefix.replace("&", "§") + configGUISmeltCostSetToMessage.replace("&", "§") + ChatColor.GREEN + instasmeltcurrency + smeltCost);
                } else {
                    event.getPlayer().sendMessage(instasmeltPrefix.replace("&", "§") + configGUISmeltCostSetToMessage.replace("&", "§") + ChatColor.GREEN + smeltCost + instasmeltcurrency);
                }
                event.getPlayer().removeMetadata("instasmelt:editing", plugin);
                openGUI(event.getPlayer());

                //Cancel the chat event to prevent the message from being broadcasted.
                event.setCancelled(true);
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(instasmeltPrefix.replace("&", "§") + configGUIInvalidNumberFormat.replace("&", "§"));
            }
        }
    }

    private void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.options().header("PLEASE DO NOT EDIT THE SMELT COST OR ENABLE SMELT COST USE /isconfig FOR THAT...ONCE DONE WITH THE CURRENCY/MESSAGES SAVE THIS CONFIG AND RELOAD SERVER!");
        config.options().copyHeader(true);
        config.set("smeltCost", smeltCost);
        config.set("enableSmeltCost", enableSmeltCost);
        config.set("currency", instasmeltcurrency);
        config.set("iscurrencyinfront", instasmeltcurrencyplacement);
        config.set("messages.Prefix", instasmeltPrefix);
        config.set("messages.NoPermission", nopermissionFail);
        config.set("messages.PlayerOnlyCmd", configcmdFail);
        config.set("messages.SmeltCost", smeltCostMessage);
        config.set("messages.SmeltCostFree", smeltCostMessageFree);
        config.set("messages.NotEnoughMoney", notenoughMoneylang);
        config.set("messages.Charged", moneyChargedlang);
        config.set("messages.smelting.SmeltFail", cannotbeSmelted);
        config.set("messages.smelting.Smelt-Success", smeltingmessage);
        config.set("messages.ConfigGUI.GUI Title", configGUITitle);
        config.set("messages.ConfigGUI.NameTagTitle", configGUI1);
        config.set("messages.ConfigGUI.NameTagValue", configGUI1Value);
        config.set("messages.ConfigGUI.NameTagClickMSG", configGUI1Underscore);
        config.set("messages.ConfigGUI.IronIngotTitle", configGUI2);
        config.set("messages.ConfigGUI.IronIngotValue", configGUI2Value);
        config.set("messages.ConfigGUI.IronIngotClickMSG", configGUI2Underscore);
        config.set("messages.ConfigGUI.NetherStarTitle", configGUI3);
        config.set("messages.ConfigGUI.NetherStarClickMSG", configGUI3Underscore);
        config.set("messages.ConfigGUI.BarrierTitle", configGUI4);
        config.set("messages.ConfigGUI.BarrierClickMSG", configGUI4Underscore);
        config.set("messages.ConfigGUI.ConfigSaveMessage", configGUISaveMSG);
        config.set("messages.ConfigGUI.EditSmeltCostMessage", configGUIeditCostMSG);
        config.set("messages.ConfigGUI.CancelEditSmeltCostMessage", configGUIeditCostCancelMSG);
        config.set("messages.ConfigGUI.SmeltCostPositiveNumberMessage", configGUIPositiveNumberMSG);
        config.set("messages.ConfigGUI.SmeltCostSetToMessage", configGUISmeltCostSetToMessage);
        config.set("messages.ConfigGUI.InvalidNumberFormatMessage", configGUIInvalidNumberFormat);
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