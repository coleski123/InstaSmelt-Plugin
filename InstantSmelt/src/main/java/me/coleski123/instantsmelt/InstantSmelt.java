package me.coleski123.instantsmelt;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import java.util.Iterator;
import java.util.Map;

public class InstantSmelt extends JavaPlugin {

    private Map<Material, ItemStack> smeltingRecipes;


    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(ChatColor.GREEN + "InstaSmelt has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(ChatColor.RED + "InstaSmelt has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            while(recipeIterator.hasNext()){
                Recipe recipe = recipeIterator.next();
                if( recipe instanceof FurnaceRecipe) {
                    FurnaceRecipe cookingRecipe = (FurnaceRecipe) recipe;
                    if (cookingRecipe.getInputChoice().test(itemInHand)) {
                        smeltedItem = cookingRecipe.getResult();
                        int stackAmount = itemInHand.getAmount();
                        smeltedItem.setAmount(stackAmount);

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