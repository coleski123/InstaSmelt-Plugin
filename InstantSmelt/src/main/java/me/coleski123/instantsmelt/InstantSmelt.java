package me.coleski123.instantsmelt;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InstantSmelt extends JavaPlugin {
    private Economy econ;
    private Map<Material, ItemStack> smeltingRecipes;
    private double smeltCost = 20.75;
    private boolean enableSmeltCost = true;
    private static InstantSmelt instance;

    public static InstantSmelt getInstance() {
        return instance;
    }

    private Plugin plugin;
    private FileConfiguration configFile;
    private InstaSmeltGUI instaSmeltGUI;
    //private String instasmeltcurrency;
    private boolean instasmeltcurrencyplacement = true;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instaSmeltGUI = new InstaSmeltGUI(this);

        int pluginId = 19877; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        getServer().getPluginManager().registerEvents(new InstaSmeltGUI(this), this);
        instance = this;
        String pluginConsolePrefix = ChatColor.YELLOW + "[InstaSmelt]";
        sendConsoleMessage(pluginConsolePrefix + ChatColor.GREEN + " InstaSmelt has been enabled!");
        setupEconomy();
        setupEconomy();
        createConfig();
        loadConfig();

        new UpdateChecker(this, 109263).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                sendConsoleMessage(pluginConsolePrefix + ChatColor.GREEN + " No new versions available.");
            } else {
                sendConsoleMessage(pluginConsolePrefix + ChatColor.RED + " A new version is now available! Download: https://www.spigotmc.org/resources/instasmelt.109263//");
            }
        });

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        String pluginConsolePrefix = ChatColor.YELLOW + "[InstaSmelt]";
        sendConsoleMessage(pluginConsolePrefix + ChatColor.RED + " InstaSmelt has been disabled!");
    }

    //Reload the config
    public void reload() {
        InstantSmelt.getInstance().reloadConfig();
        getLogger().info(ChatColor.translateAlternateColorCodes('&', "&eThe config has been saved!"));
    }

    //Creates the config.yml and defines the smeltCost/enableSmeltCost
    private void createConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            config.set("smeltCost", smeltCost);
            config.set("enableSmeltCost", enableSmeltCost);
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Loads the config.yml and sets the default values of SmeltCost & EnableSmeltCost
    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            smeltCost = config.getDouble("smeltCost", 20.75);
            enableSmeltCost = config.getBoolean("enableSmeltCost", true);
        }
    }

    //Vault economy
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String instasmeltPrefix = getConfig().getConfigurationSection("messages").getString("Prefix");
        instasmeltPrefix = instasmeltPrefix.replace("&", "§");

        String smeltCostLanguage = getConfig().getConfigurationSection("messages").getString("SmeltCost");
        smeltCostLanguage = smeltCostLanguage.replace("&", "§");

        String smeltCostLanguageFree = getConfig().getConfigurationSection("messages").getString("SmeltCostFree");
        smeltCostLanguageFree = smeltCostLanguageFree.replace("&", "§");

        String notenoughMoneyLang = getConfig().getConfigurationSection("messages").getString("NotEnoughMoney");
        notenoughMoneyLang = notenoughMoneyLang.replace("&", "§");

        String moneyCharged = getConfig().getConfigurationSection("messages").getString("Charged");
        moneyCharged = moneyCharged.replace("&", "§");

        String smeltFail = getConfig().getConfigurationSection("messages.smelting").getString("SmeltFail");
        smeltFail = smeltFail.replace("&", "§");

        String noPerm = getConfig().getConfigurationSection("messages").getString("NoPermission");
        noPerm = noPerm.replace("&", "§");

        if (command.getName().equalsIgnoreCase("instasmeltconfig")) {
            if (!(sender instanceof Player)) {
                String configcmdFail = getConfig().getConfigurationSection("messages").getString("PlayerOnlyCmd");
                configcmdFail = configcmdFail.replace("&", "§");
                sender.sendMessage(configcmdFail);
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("instasmelt.use.gui.config")) {
                player.sendMessage(noPerm);
                return true;
            }

            // Open the InstaSmeltGUI for the player
            instaSmeltGUI.openGUI(player);
            return true;
        }

        //SmeltCost Command
        if (command.getName().equalsIgnoreCase("instasmeltcost") && sender instanceof Player) {
            Player player = (Player) sender;
            // Check if the player has the required permission
            if (!player.hasPermission("instasmelt.use.smeltcost")) {
                player.sendMessage(noPerm);
                return true;
            }
            loadConfig();
            if (enableSmeltCost) {
                smeltCostLanguage = smeltCostLanguage.replace("&", "§");
                if (InstaSmeltGUI.instasmeltcurrencyplacement) {
                    player.sendMessage(instasmeltPrefix + smeltCostLanguage + ChatColor.GREEN + InstaSmeltGUI.instasmeltcurrency + smeltCost);
                } else {
                    player.sendMessage(instasmeltPrefix + smeltCostLanguage + ChatColor.GREEN + smeltCost + InstaSmeltGUI.instasmeltcurrency);
                }
                return false;
            } else ;
            player.sendMessage(instasmeltPrefix + smeltCostLanguage + smeltCostLanguageFree);
            return false;
        }

        //Main instasmelt command
        if (command.getName().equalsIgnoreCase("instasmelt") && sender instanceof Player) {
            Player player = (Player) sender;

            loadConfig();

            // Check if the player has the required permission
            if (!player.hasPermission("instasmelt.use")) {
                player.sendMessage(noPerm);
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("all")) {

                ItemStack itemInHands = player.getInventory().getItemInMainHand();
                ItemStack smeltedItems = null;

                Iterator<Recipe> recipeIteratornew = getServer().recipeIterator();
                while (recipeIteratornew.hasNext()) {
                    Recipe recipes = recipeIteratornew.next();
                    if (recipes instanceof FurnaceRecipe) {
                        FurnaceRecipe cookingRecipes = (FurnaceRecipe) recipes;
                        if (cookingRecipes.getInputChoice().test(itemInHands)) {
                            smeltedItems = cookingRecipes.getResult();
                            int stackAmount = itemInHands.getAmount();
                            smeltedItems.setAmount(stackAmount);

                            //Orb Pickup Sound
                            Sound xpSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                            //Play the furnace or smoker sound based on the smelted item
                            Sound smeltSound = Sound.BLOCK_FURNACE_FIRE_CRACKLE;
                            if (smeltedItems.getType().isEdible()) {
                                smeltSound = Sound.BLOCK_SMOKER_SMOKE;
                            }

                            // Clone the item in hand for smelting
                            ItemStack smeltedItemClone = itemInHands.clone();
                            smeltedItemClone.setAmount(stackAmount);


                            // Calculate the cost per stack
                            double smeltCostPerStack = smeltCost;

                            // Calculate the total cost for all stacks excluding the stack in hand
                            int itemsOutsideHand = countItems(player.getInventory(), itemInHands.getType());

                            // Calculate the cost for smelting this specific amount of items
                            double totalSmeltCost = smeltCostPerStack * itemsOutsideHand;


                            if (enableSmeltCost && econ.getBalance(player) < totalSmeltCost) {

                                double remainingBalance = totalSmeltCost * econ.getBalance(player);
                                String totalBalanceRemaining = Double.toString(totalSmeltCost);
                                String itemName = toFriendlyName(itemInHands.getType());



                                notenoughMoneyLang = notenoughMoneyLang.replace("{ITEMNAME}", itemName);
                                if (InstaSmeltGUI.instasmeltcurrencyplacement) {
                                    notenoughMoneyLang = notenoughMoneyLang.replace("{TOTALAMOUNT}", InstaSmeltGUI.instasmeltcurrency + totalBalanceRemaining);
                                    notenoughMoneyLang = notenoughMoneyLang.replace("{PLAYERWALLET}", InstaSmeltGUI.instasmeltcurrency + econ.getBalance(player));
                                } else {
                                    notenoughMoneyLang = notenoughMoneyLang.replace("{TOTALAMOUNT}", totalBalanceRemaining + InstaSmeltGUI.instasmeltcurrency);
                                    notenoughMoneyLang = notenoughMoneyLang.replace("{PLAYERWALLET}", + econ.getBalance(player) + InstaSmeltGUI.instasmeltcurrency);
                                }
                                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                player.sendMessage(instasmeltPrefix + notenoughMoneyLang);
                                return true;
                            }

                            // Deduct the smelt cost from the player's economy balance if enabled in the config file
                            if (enableSmeltCost) {
                                econ.withdrawPlayer(player, totalSmeltCost);
                                if (InstaSmeltGUI.instasmeltcurrencyplacement) {
                                    player.sendMessage(instasmeltPrefix + moneyCharged + ChatColor.GREEN + InstaSmeltGUI.instasmeltcurrency + totalSmeltCost);
                                } else {
                                    player.sendMessage(instasmeltPrefix + moneyCharged + ChatColor.GREEN + totalSmeltCost + InstaSmeltGUI.instasmeltcurrency);
                                }
                            }

                            // Replace the item in hand with the smelted item
                            player.getInventory().setItemInMainHand(smeltedItemClone);

                            // Get the player's inventory
                            Inventory playerInventory = player.getInventory();

                            // Count the total number of slots containing the item to be smelted
                            int totalSlotsWithItem = 0;
                            for (int i = 0; i < playerInventory.getSize(); i++) {
                                // Skip the shield slot (usually slot 40 in the player's inventory)
                                if (i == 40) {
                                    continue;
                                }

                                ItemStack inventoryItem = playerInventory.getItem(i);

                                // Check if the inventory slot is not empty and if it matches the item you want to smelt
                                if (inventoryItem != null && inventoryItem.isSimilar(itemInHands)) {
                                    totalSlotsWithItem += inventoryItem.getAmount();
                                }
                            }

                            // Get the experience amount and give it to the player
                            //int experienceAmount = getExperienceAmount(cookingRecipes, stackAmount);
                            int experienceAmount = getExperienceAmount(cookingRecipes, totalSlotsWithItem);
                            player.giveExp(experienceAmount);

                            boolean smeltedAny = true;

                            while (smeltedAny) {
                                smeltedAny = false;

                                // Loop through the player's entire inventory (including hotbar and main inventory) and smelt matching items
                                for (int i = 0; i < playerInventory.getSize(); i++) {
                                    // Skip the shield slot (usually slot 40 in the player's inventory)
                                    if (i == 40) {
                                        continue;
                                    }

                                    ItemStack inventoryItem = playerInventory.getItem(i);

                                    // Check if the inventory slot is not empty and if it matches the item you want to smelt
                                    if (inventoryItem != null && inventoryItem.isSimilar(itemInHands)) {
                                        int availableAmount = inventoryItem.getAmount();

                                        // Determine how many smelted items can be produced from the available raw items
                                        int smeltedItemAmount = Math.min(availableAmount, totalSlotsWithItem);

                                        if (smeltedItemAmount > 0) {

                                            // Create a new smelted item stack with the correct amount
                                            ItemStack clonedSmeltedItem = smeltedItems.clone();
                                            clonedSmeltedItem.setAmount(smeltedItemAmount);

                                            // Subtract the smelted item amount from the inventory stack
                                            inventoryItem.setAmount(availableAmount - smeltedItemAmount);

                                            // Find the first empty slot and place the smelted item there
                                            for (int j = 0; j < playerInventory.getSize(); j++) {
                                                // Skip the shield slot (usually slot 40 in the player's inventory)
                                                if (j == 40) {
                                                    continue;
                                                }

                                                ItemStack emptySlot = playerInventory.getItem(j);
                                                if (emptySlot == null || emptySlot.getType() == Material.AIR) {
                                                    playerInventory.setItem(j, clonedSmeltedItem);
                                                    break;
                                                }
                                            }

                                            // Update the total count of slots with the item to be smelted
                                            totalSlotsWithItem -= smeltedItemAmount;
                                            smeltedAny = true;
                                        }
                                    }
                                }
                            }

                            String itemName = toFriendlyName(itemInHands.getType());
                            String smeltedItemName = toFriendlyName(smeltedItems.getType());
                            String xpAmount = String.valueOf(experienceAmount);
                            String smeltingmessage = getConfig().getConfigurationSection("messages.smelting").getString("Smelt-Success");
                            smeltingmessage = smeltingmessage.replace("{ITEM1}", itemName);
                            smeltingmessage = smeltingmessage.replace("{ITEM2}", smeltedItemName);
                            smeltingmessage = smeltingmessage.replace("{XPAMOUNT}", xpAmount);
                            smeltingmessage = smeltingmessage.replace("&", "§");
                            player.sendMessage(instasmeltPrefix + smeltingmessage);

                            // Play the smelt sound
                            player.playSound(player.getLocation(), smeltSound, 2, 1);
                            // Play the XP orb sound
                            Random random = new Random();
                            float pitch = 0.5f + random.nextFloat();
                            player.playSound(player.getLocation(), xpSound, 0.6f, pitch);
                            return true;
                        }
                    }
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                player.sendMessage(instasmeltPrefix + smeltFail);

                return false;
            }

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            ItemStack smeltedItem = null;

            Iterator<Recipe> recipeIterator = getServer().recipeIterator();
            while (recipeIterator.hasNext()) {
                Recipe recipe = recipeIterator.next();
                if (recipe instanceof FurnaceRecipe) {
                    FurnaceRecipe cookingRecipe = (FurnaceRecipe) recipe;
                    if (cookingRecipe.getInputChoice().test(itemInHand)) {
                        smeltedItem = cookingRecipe.getResult();
                        int stackAmount = itemInHand.getAmount();
                        smeltedItem.setAmount(stackAmount);

                        //Orb Pickup Sound
                        Sound xpSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                        //Play the furnace or smoker sound based on the smelted item
                        Sound smeltSound = Sound.BLOCK_FURNACE_FIRE_CRACKLE;
                        if (smeltedItem.getType().isEdible()) {
                            smeltSound = Sound.BLOCK_SMOKER_SMOKE;
                        }

                        double totalCost = smeltCost * stackAmount;

                        // Check if the smelt cost is enabled in the config file
                        boolean enableSmeltCost = getConfig().getBoolean("enableSmeltCost", true);
                        String itemName = toFriendlyName(itemInHand.getType());

                        if (enableSmeltCost && econ.getBalance(player) < totalCost) {
                            double remainingBalance = totalCost * econ.getBalance(player);
                            String totalBalanceRemaining = Double.toString(totalCost);

                            notenoughMoneyLang = notenoughMoneyLang.replace("{ITEMNAME}", itemName);
                            if (InstaSmeltGUI.instasmeltcurrencyplacement) {
                                notenoughMoneyLang = notenoughMoneyLang.replace("{TOTALAMOUNT}", InstaSmeltGUI.instasmeltcurrency + totalBalanceRemaining);
                                notenoughMoneyLang = notenoughMoneyLang.replace("{PLAYERWALLET}", InstaSmeltGUI.instasmeltcurrency + econ.getBalance(player));
                            } else {
                                notenoughMoneyLang = notenoughMoneyLang.replace("{TOTALAMOUNT}", totalBalanceRemaining + InstaSmeltGUI.instasmeltcurrency);
                                notenoughMoneyLang = notenoughMoneyLang.replace("{PLAYERWALLET}", + econ.getBalance(player) + InstaSmeltGUI.instasmeltcurrency);
                            }
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                            player.sendMessage(instasmeltPrefix + notenoughMoneyLang);
                            return true;
                        }

                        // Deduct the smelt cost from the player's economy balance if enabled in the config file
                        if (enableSmeltCost) {
                            econ.withdrawPlayer(player, totalCost);
                            if (InstaSmeltGUI.instasmeltcurrencyplacement) {
                                player.sendMessage(instasmeltPrefix + moneyCharged + ChatColor.GREEN + InstaSmeltGUI.instasmeltcurrency + totalCost);
                            } else {
                                player.sendMessage(instasmeltPrefix + moneyCharged + ChatColor.GREEN + totalCost + InstaSmeltGUI.instasmeltcurrency);
                            }
                        }
                        // Get the experience amount and give it to the player
                        int experienceAmount = getExperienceAmount(cookingRecipe, stackAmount);
                        player.giveExp(experienceAmount);

                        //Sets the item in the player's main hand to smelted item.
                        player.getInventory().setItemInMainHand(smeltedItem);
                        String smeltedItemName = toFriendlyName(smeltedItem.getType());
                        String xpAmount = String.valueOf(experienceAmount);
                        String smeltingmessage = getConfig().getConfigurationSection("messages.smelting").getString("Smelt-Success");
                        smeltingmessage = smeltingmessage.replace("{ITEM1}", itemName);
                        smeltingmessage = smeltingmessage.replace("{ITEM2}", smeltedItemName);
                        smeltingmessage = smeltingmessage.replace("{XPAMOUNT}", xpAmount);
                        smeltingmessage = smeltingmessage.replace("&", "§");
                        player.sendMessage(instasmeltPrefix + smeltingmessage);
                        //Play the smelt sound
                        player.playSound(player.getLocation(), smeltSound, 2, 1);
                        //Play the XP orb sound
                        Random random = new Random();
                        float pitch = 0.5f + random.nextFloat();
                        player.playSound(player.getLocation(), xpSound, 0.6f, pitch);
                        return true;
                    }
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            player.sendMessage(instasmeltPrefix + smeltFail);
            return false;
        }
        return false;
    }


    // Helper method to count items of a specific type in a player's inventory
    private int countItems(Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack itemStack : inventory) {
            if (itemStack != null && itemStack.getType() == material) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }

    //Calculates the amount of experience points that a player will receive when smelting an item in a furnace, based on the furnace recipe and the number of items being smelted
    private int getExperienceAmount(FurnaceRecipe recipe, int stackAmount) {
        float experience = recipe.getExperience();

        int count = 1;
        int experienceAmount = 0;
        while(count <= stackAmount) {
            double rand = Math.random();
            if (rand <= (double) experience){
                experienceAmount += (int) Math.ceil(experience);
            }
            count++;
        }
        return experienceAmount;
    }

    public String toFriendlyName(Material material) {
        return material.name().toLowerCase().replace('_', ' ');
    }

    private void sendConsoleMessage(String message) {
        getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}