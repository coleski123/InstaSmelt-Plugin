package me.coleski123.instantsmelt;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class InstantSmelt extends JavaPlugin {
    private Economy econ;
    private Map<Material, ItemStack> smeltingRecipes;
    private double smeltCost = 20.00;
    private boolean enableSmeltCost = true;


    @Override
    public void onEnable() {
        // Plugin startup logic
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

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            smeltCost = config.getDouble("smeltCost", 20.00);
            enableSmeltCost = config.getBoolean("enableSmeltCost", true);
        }
    }


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

        if (command.getName().equalsIgnoreCase("smeltcost") && sender instanceof Player) {
            Player player = (Player) sender;
            // Check if the player has the required permission
            if (!player.hasPermission("instasmelt.use.smeltcost")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
            if (enableSmeltCost) {
                player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "The current smelt cost is " + ChatColor.GREEN + econ.currencyNamePlural() + smeltCost);
                return false;
            }
            else;
            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "The current smelt cost is " + ChatColor.GREEN + "FREE!");
            return false;
        }


        if (command.getName().equalsIgnoreCase("instasmelt") && sender instanceof Player) {
            Player player = (Player) sender;

            // Check if the player has the required permission
            if (!player.hasPermission("instasmelt.use")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
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

                        // Check if the smelt cost is enabled in the config file
                        boolean enableSmeltCost = getConfig().getBoolean("enableSmeltCost", true);
                        if (enableSmeltCost && econ.getBalance(player) < smeltCost) {
                            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.RED + "You do not have enough money to smelt " + toFriendlyName(itemInHand.getType()));
                            return true;
                        }

                        // Deduct the smelt cost from the player's economy balance if enabled in the config file
                        if (enableSmeltCost) {
                            econ.withdrawPlayer(player, smeltCost);
                            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "You have been charged " + ChatColor.GREEN + econ.currencyNamePlural() + smeltCost + " " );
                        }
                        // Get the experience amount and give it to the player
                        int experienceAmount = getExperienceAmount(cookingRecipe, stackAmount);
                        player.giveExp(experienceAmount);

                        player.getInventory().setItemInMainHand(smeltedItem);
                        player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "Your " + toFriendlyName(itemInHand.getType()) + " has been smelted into " + toFriendlyName(smeltedItem.getType()) + " and you received " + ChatColor.GREEN + experienceAmount + " XP" + ChatColor.YELLOW + "!");
                        return true;
                    }
                }
            }

            player.sendMessage(ChatColor.GOLD + "[InstaSmelt] " + ChatColor.YELLOW + "That item cannot be smelted!");
            return false;
        }
        return false;
    }

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