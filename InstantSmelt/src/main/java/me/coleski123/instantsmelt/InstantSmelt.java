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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;d

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

    @Override
    public void onEnable() {
        // Plugin startup logic
        instaSmeltGUI = new InstaSmeltGUI(this);
        getServer().getPluginManager().registerEvents(new InstaSmeltGUI(this), this);
        instance = this;
        getLogger().info(ChatColor.GREEN + "InstaSmelt has been enabled!");
        setupEconomy();
        setupEconomy();
        createConfig();
        loadConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(ChatColor.RED + "InstaSmelt has been disabled!");
    }

    //Reload the config
    public void reload() {
        InstantSmelt.getInstance().reloadConfig();
        getLogger().info(ChatColor.translateAlternateColorCodes('&', "&eThe config has been reloaded!"));
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

        if (command.getName().equalsIgnoreCase("instasmeltconfig")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("instasmelt.use.gui.config")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
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
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
            loadConfig();
            if (enableSmeltCost) {
                player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "The current smelt cost is " + ChatColor.GREEN + econ.format(smeltCost));
                return false;
            }
            else;
            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "The current smelt cost is " + ChatColor.GREEN + "FREE!");
            return false;
        }

        //Main instasmelt command
        if (command.getName().equalsIgnoreCase("instasmelt") && sender instanceof Player) {
            Player player = (Player) sender;

            loadConfig();
            // Check if the player has the required permission
            if (!player.hasPermission("instasmelt.use")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }

            //Iterates through all the available recipes in the server and checks if the player's held item matches the input of any FurnaceRecipe
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

                        // Check if the smelt cost is enabled in the config file
                        boolean enableSmeltCost = getConfig().getBoolean("enableSmeltCost", true);
                        if (enableSmeltCost && econ.getBalance(player) < smeltCost) {
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.RED + "You do not have enough money to smelt " + toFriendlyName(itemInHand.getType()));
                            return true;
                        }

                        // Deduct the smelt cost from the player's economy balance if enabled in the config file
                        if (enableSmeltCost) {
                            econ.withdrawPlayer(player, smeltCost);
                            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "You have been charged " + ChatColor.GREEN + econ.format(smeltCost));
                        }
                        // Get the experience amount and give it to the player
                        int experienceAmount = getExperienceAmount(cookingRecipe, stackAmount);
                        player.giveExp(experienceAmount);

                        //Sets the item in the player's main hand to smelted item.
                        player.getInventory().setItemInMainHand(smeltedItem);
                        player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "Your " + toFriendlyName(itemInHand.getType()) + " has been smelted into " + toFriendlyName(smeltedItem.getType()) + " and you received " + ChatColor.GREEN + experienceAmount + " XP" + ChatColor.YELLOW + "!");
                        //Play the smelt sound
                        player.playSound(player.getLocation(), smeltSound, 2, 1);
                        //Play the XP orb sound
                        Random random = new Random();
                        float pitch = 0.5f + random.nextFloat();
                        player.playSound(player.getLocation(), xpSound, 0.6f,pitch);
                        return true;
                    }
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "That item cannot be smelted!");
            return false;
        }
        return false;
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
    private String toFriendlyName(Material material) {
        return material.name().toLowerCase().replace('_', ' ');
    }
}