package org.dimasik.liteauction;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.liteauction.api.events.EventManager;
import org.dimasik.liteauction.backend.communication.AbstractCommunication;
import org.dimasik.liteauction.backend.communication.impl.*;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.exceptions.UnsupportedConfigurationException;
import org.dimasik.liteauction.backend.listeners.JoinListener;
import org.dimasik.liteauction.backend.storage.databases.AbstractDatabase;
import org.dimasik.liteauction.backend.storage.databases.impl.*;
import org.dimasik.liteauction.backend.utils.nms.ContainerUtil;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.economy.EconomyEditor;
import org.dimasik.liteauction.economy.impl.StickEco;
import org.dimasik.liteauction.economy.impl.VaultEco;
import org.dimasik.liteauction.frontend.commands.CommandExecutor;
import org.dimasik.liteauction.frontend.commands.impl.*;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;
import org.dimasik.liteauction.frontend.menus.bids.listeners.ItemBidsListener;
import org.dimasik.liteauction.frontend.menus.market.listeners.*;

import java.io.File;

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
    private static EventManager eventManager;
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
        setupEvents();
        setupEconomy();
        setupCommand();
        setupListeners();
        setupBoughtItem();
        if(ConfigManager.getBoolean("settings/database.yml", "isHead", true)) {
            startRunnable();
        }
    }

    private void setupConfig(){
        String[] files = {
                "config.yml",
                "settings/database.yml",
                "settings/settings.yml",
                "design/commands/sell.yml",
                "design/commands/search.yml",
                "design/commands/main.yml",
                "design/commands/sound.yml",
                "design/menus/main.yml",
                "design/menus/market/main.yml",
                "design/menus/market/confirm_item.yml",
                "design/menus/market/count_buy_item.yml",
                "design/menus/market/remove_item.yml",
                "design/menus/market/sell.yml",
                "design/menus/market/unsold.yml",
                "design/menus/bids/main.yml",
                "design/menus/bids/item_bids.yml",
                "design/menus/bids/remove_item.yml",
                "design/menus/bids/sell.yml",
        };
        for(String filePath : files) {
            File file = new File(super.getDataFolder(), filePath);
            if (!file.exists()) {
                super.saveResource(filePath, false);
            }
        }
        try {
            ConfigManager.init(this);
            super.getLogger().info("   |   ᴋᴏʜфигуᴘᴀция уᴄпᴇшʜᴏ зᴀгᴘʏжᴇʜᴀ");
        }
        catch (UnsupportedConfigurationException e){
            super.getLogger().warning("   |   ᴏшибᴋᴀ зᴀгᴘузᴋи ᴋᴏʜфигуᴘᴀции: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void setupDatabase(){
        switch (ConfigManager.getString("settings/database.yml", "database.type", "Redis").toLowerCase()){
            case "mysql" -> databaseManager = new Mysql(
                    ConfigManager.getString("settings/database.yml", "database.global.host", "localhost"),
                    ConfigManager.getString("settings/database.yml", "database.global.user", "root"),
                    ConfigManager.getString("settings/database.yml", "database.global.password", "сайнес гпт кодер"),
                    ConfigManager.getString("settings/database.yml", "database.global.database", "lite_auction")
            );
            case "sqlite" -> databaseManager = new SQLite(
                    ConfigManager.getString("settings/database.yml", "database.local.file", "database.db")
            );
            case "default" -> throw new UnsupportedConfigurationException("Тип базы данных не существует!");
        }
        databaseManager.createTables().join();

        switch (ConfigManager.getString("settings/database.yml", "communication.type", "Redis").toLowerCase()){
            case "redis" -> communicationManager = new Redis(
                    ConfigManager.getString("settings/database.yml", "communication.redis.host", "localhost"),
                    ConfigManager.getInt("settings/database.yml", "communication.redis.port", 6379),
                    ConfigManager.getString("settings/database.yml", "communication.redis.password", "сайнес гпт кодер"),
                    ConfigManager.getString("settings/database.yml", "communication.redis.channel", "auction")
            );
            case "rabbitmq" -> communicationManager = new RabbitMQ(
                    ConfigManager.getString("settings/database.yml", "communication.rabbitmq.host", "localhost"),
                    ConfigManager.getInt("settings/database.yml", "communication.rabbitmq.port", 5672),
                    ConfigManager.getString("settings/database.yml", "communication.rabbitmq.user", "root"),
                    ConfigManager.getString("settings/database.yml", "communication.rabbitmq.password", "сайнес гпт кодер"),
                    ConfigManager.getString("settings/database.yml", "communication.rabbitmq.vhost", "/"),
                    ConfigManager.getString("settings/database.yml", "communication.rabbitmq.channel", "auction")
            );
            case "nats" -> communicationManager = new Nats(
                    ConfigManager.getStringList("settings/database.yml", "communication.nats.host").toArray(new String[]{}),
                    ConfigManager.getString("settings/database.yml", "communication.nats.user", "root"),
                    ConfigManager.getString("settings/database.yml", "communication.nats.password", "сайнес гпт кодер"),
                    ConfigManager.getString("settings/database.yml", "communication.nats.channel", "auction")
            );
            case "websocket" -> communicationManager = new WebSocket(
                    ConfigManager.getString("settings/database.yml", "communication.websocket.host", "localhost"),
                    ConfigManager.getInt("settings/database.yml", "communication.websocket.port", 6379),
                    ConfigManager.getString("settings/database.yml", "communication.websocket.password", "сайнес гпт кодер"),
                    ConfigManager.getString("settings/database.yml", "communication.websocket.channel", "auction")
            );
            case "local" -> communicationManager = new Local();
            case "default" -> throw new UnsupportedConfigurationException("Метод коммуникации не существует!");
        }
    }

    private void setupEvents(){
        eventManager = new EventManager();
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
            Inventory inventory = ContainerUtil.getActiveContainer(player);
            InventoryHolder holder = inventory.getHolder();
            if(holder instanceof AbstractMenu){
                inventory.close();
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
