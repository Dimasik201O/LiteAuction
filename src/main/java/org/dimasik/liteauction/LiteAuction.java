package org.dimasik.liteauction;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import org.dimasik.liteauction.backend.communication.impl.*;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.exceptions.UnsupportedConfigurationException;
import org.dimasik.liteauction.backend.listeners.JoinListener;
import org.dimasik.liteauction.backend.storage.databases.AbstractDatabase;
import org.dimasik.liteauction.backend.storage.databases.impl.*;
import org.dimasik.liteauction.backend.utils.ContainerUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.economy.EconomyEditor;
import org.dimasik.liteauction.economy.impl.StickEco;
import org.dimasik.liteauction.economy.impl.VaultEco;
import org.dimasik.liteauction.frontend.commands.CommandExecutor;
import org.dimasik.liteauction.frontend.commands.impl.*;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;
import org.dimasik.liteauction.frontend.menus.bids.listeners.ItemBidsListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.*;

@Getter
public final class LiteAuction extends JavaPlugin {
    private AbstractDatabase databaseManager;
    private AbstractCommunication communicationManager;
    private CommandExecutor commandExecutor;
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

        super.getLogger().info("   [   зᴀгᴘʏзᴋᴀ ᴋᴏʜфигуᴘᴀции   ]   ");
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
        try {
            ConfigManager.init(config);
            super.getLogger().info("   |   ᴋᴏʜȹигуᴘᴀция уᴄпᴇшʜᴏ зᴀгᴘʏжᴇʜᴀ");
        }
        catch (UnsupportedConfigurationException e){
            super.getLogger().warning("   |   ᴏшибᴋᴀ зᴀгᴘузᴋи ᴋᴏʜфигуᴘᴀции: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void setupDatabase(){
        switch (ConfigManager.getDATABASE_TYPE().toLowerCase()){
            case "mysql" -> databaseManager = new Mysql(
                    ConfigManager.getGLOBAL_HOST(),
                    ConfigManager.getGLOBAL_USER(),
                    ConfigManager.getGLOBAL_PASSWORD(),
                    ConfigManager.getGLOBAL_DATABASE()
            );
            case "sqlite" -> databaseManager = new SQLite(
                    ConfigManager.getLOCAL_FILE()
            );
        }
        databaseManager.createTables().join();

        switch (ConfigManager.getCOMMUNICATION_TYPE().toLowerCase()){
            case "redis" -> communicationManager = new Redis(
                    ConfigManager.getREDIS_HOST(),
                    ConfigManager.getREDIS_PORT(),
                    ConfigManager.getREDIS_PASSWORD(),
                    ConfigManager.getREDIS_CHANNEL()
            );
            case "rabbitmq" -> communicationManager = new RabbitMQ(
                    ConfigManager.getRABBITMQ_HOST(),
                    ConfigManager.getRABBITMQ_PORT(),
                    ConfigManager.getRABBITMQ_USER(),
                    ConfigManager.getRABBITMQ_PASSWORD(),
                    ConfigManager.getRABBITMQ_VHOST(),
                    ConfigManager.getRABBITMQ_CHANNEL()
            );
            case "nats" -> communicationManager = new Nats(
                    ConfigManager.getNATS_HOST(),
                    ConfigManager.getNATS_USER(),
                    ConfigManager.getNATS_PASSWORD(),
                    ConfigManager.getNATS_CHANNEL()
            );
            case "websocket" -> communicationManager = new WebSocket(
                    ConfigManager.getWEBSOCKET_HOST(),
                    ConfigManager.getWEBSOCKET_PORT(),
                    ConfigManager.getWEBSOCKET_PASSWORD(),
                    ConfigManager.getWEBSOCKET_CHANNEL()
            );
            case "local" -> communicationManager = new Local();
        }
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
        pluginManager.registerEvents(new JoinListener(), this);
        new org.dimasik.liteauction.frontend.menus.market.listeners.MainListener().register();
        new org.dimasik.liteauction.frontend.menus.bids.listeners.MainListener().register();
        new org.dimasik.liteauction.frontend.menus.market.listeners.SellListener().register();
        new org.dimasik.liteauction.frontend.menus.bids.listeners.SellListener().register();
        new org.dimasik.liteauction.frontend.menus.market.listeners.RemoveItemListener().register();
        new org.dimasik.liteauction.frontend.menus.bids.listeners.RemoveItemListener().register();
        new ItemBidsListener().register();
        new UnsoldListener().register();
        new ConfirmItemListener().register();
        new CountBuyItemListener().register();
    }

    private void setupBoughtItem(){
        boughtItem = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = boughtItem.getItemMeta();
        itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CЭтот предмет &x&F&F&2&2&2&2уже купили&x&D&5&D&B&D&C!"));
        boughtItem.setItemMeta(itemMeta);
    }

    public void startRunnable(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> LiteAuction.getInstance().getDatabaseManager().moveExpiredItems(), 0, 20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()){
            if(ContainerUtil.hasActiveContainer(player)){
                Inventory inventory = ContainerUtil.getActiveContainer(player);
                InventoryHolder holder = inventory.getHolder();
                if(holder instanceof AbstractMenu){
                    inventory.close();
                }
            }
        }
        if(communicationManager != null){
            communicationManager.close();
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
