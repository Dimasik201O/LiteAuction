package org.dimasik.liteauction;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.mysql.DatabaseManager;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.redis.RedisManager;
import org.dimasik.liteauction.backend.redis.UpdateData;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.economy.EconomyEditor;
import org.dimasik.liteauction.economy.impl.StickEco;
import org.dimasik.liteauction.economy.impl.VaultEco;
import org.dimasik.liteauction.frontend.commands.CommandExecutor;
import org.dimasik.liteauction.frontend.commands.impl.*;
import org.dimasik.liteauction.frontend.listeners.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public final class LiteAuction extends JavaPlugin {
    private DatabaseManager databaseManager;
    private RedisManager redisManager;
    private CommandExecutor commandExecutor;
    @Getter
    private static final ConcurrentHashMap<UpdateData, Integer> items = new ConcurrentHashMap<>();
    @Getter
    private static ItemStack boughtItem;
    @Getter
    private static LiteAuction instance;
    @Getter
    private static EconomyEditor economyEditor;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        new UpdateChecker(this, super.getFile()).checkForUpdates();

        setupConfig();
        setupDatabase();
        setupEconomy();
        setupCommand();
        setupListeners();
        setupBoughtItem();
        if(ConfigManager.isIS_HEAD()) {
            startRunnable();
        }
    }

    private void setupConfig(){
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        ConfigManager.init(config);
    }

    private void setupDatabase(){
        databaseManager = new DatabaseManager(ConfigManager.getMYSQL_HOST(), ConfigManager.getMYSQL_USER(), ConfigManager.getREDIS_PASSWORD(), ConfigManager.getMYSQL_DATABASE());
        databaseManager.initialize().join();
        databaseManager.getSellItemsManager().moveExpiredItems();

        redisManager = new RedisManager(ConfigManager.getREDIS_HOST(), ConfigManager.getREDIS_PORT(), ConfigManager.getREDIS_PASSWORD(), ConfigManager.getREDIS_CHANNEL());
    }

    private void setupEconomy(){
        if(ConfigManager.getECONOMY_EDITOR().equalsIgnoreCase("StickEco")){
            economyEditor = new StickEco();
        }
        else{
            economyEditor = new VaultEco();
        }
    }

    private void setupCommand(){
        commandExecutor = new CommandExecutor();
        var command = getCommand("ah");
        assert command != null;
        command.setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);

        new Sell("sell").register();
        new Search("search").register();
        new Help("help").register();
        new Sound("sound").register();
        new Player("player").register();
        new Admin("admin").register();
    }

    private void setupListeners(){
        PluginManager pluginManager = super.getServer().getPluginManager();
        pluginManager.registerEvents(new MainListener(), this);
        pluginManager.registerEvents(new RemoveItemListener(), this);
        pluginManager.registerEvents(new SellListener(), this);
        pluginManager.registerEvents(new UnsoldListener(), this);
        pluginManager.registerEvents(new ConfirmItemListener(), this);
        pluginManager.registerEvents(new CountBuyItemListener(), this);
        pluginManager.registerEvents(new JoinListener(), this);
    }

    private void setupBoughtItem(){
        boughtItem = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = boughtItem.getItemMeta();
        itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CЭтот предмет &x&F&F&2&2&2&2уже купили&x&D&5&D&B&D&C!"));
        boughtItem.setItemMeta(itemMeta);
    }

    public void startRunnable(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> LiteAuction.getInstance().getDatabaseManager().moveExpiredItems(), 0, 50);
    }

    public static void removeClosedUpdates(){
        for(Map.Entry<UpdateData, Integer> entry : getItems().entrySet()){
            UpdateData updateData = entry.getKey();
            if(updateData.getInventory().getViewers().isEmpty()){
                items.remove(updateData, entry.getValue());
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(redisManager != null){
            redisManager.close();
        }
    }

    public static void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
        for(int id = 0; id < inventory.getStorageContents().length; ++id) {
            ItemStack item = inventory.getItem(id);
            if (item == null || item.getType().isAir()) {
                inventory.addItem(new ItemStack[]{itemStack});
                return;
            }

            if (item.isSimilar(itemStack)) {
                int count = item.getMaxStackSize() - item.getAmount();
                if (count > 0) {
                    if (itemStack.getAmount() <= count) {
                        inventory.addItem(new ItemStack[]{itemStack});
                        return;
                    }

                    ItemStack i = itemStack.clone();
                    i.setAmount(count);
                    inventory.addItem(new ItemStack[]{i});
                    itemStack.setAmount(itemStack.getAmount() - count);
                }
            }
        }

        Bukkit.getScheduler().runTask(instance, () -> {
            location.getWorld().dropItemNaturally(location, itemStack);
        });
    }
}
