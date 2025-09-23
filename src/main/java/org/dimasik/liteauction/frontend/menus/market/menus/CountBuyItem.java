package org.dimasik.liteauction.frontend.menus.market.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CountBuyItem extends AbstractMenu {
    @Setter
    private boolean forceClose = false;
    private int count;
    private SellItem sellItem;
    private Main back;

    public CountBuyItem(SellItem sellItem, Main back, int count){
        this.sellItem = sellItem;
        this.back = back;
        this.count = count;
    }

    public CountBuyItem compile(){
        try{
            inventory = ConfigUtils.buildInventory(this, "design/menus/market/count_buy_item.yml", "inventory-type",
                    ConfigManager.getString("design/menus/market/count_buy_item.yml", "gui-title", "&x&0&0&D&8&F&F Покупка предмета")
            );
            Pair<ItemStack, Integer> dec10 = ConfigUtils.buildItem("design/menus/market/count_buy_item.yml", "decrease10", "&x&F&F&2&2&2&2▶ Уменьшить на 10 единиц");
            inventory.setItem(ConfigManager.getInt("design/menus/market/count_buy_item.yml", "decrease10.slot", 0), dec10.getLeft());
            Pair<ItemStack, Integer> dec1 = ConfigUtils.buildItem("design/menus/market/count_buy_item.yml", "decrease1", "&x&F&F&2&2&2&2▶ Уменьшить на 1 единиц");
            inventory.setItem(ConfigManager.getInt("design/menus/market/count_buy_item.yml", "decrease1.slot", 1), dec1.getLeft());
            if(true){
                ItemStack itemStack = sellItem.decodeItemStack();
                itemStack.setAmount(sellItem.getAmount());
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.addAll(ConfigManager.getStringList("design/menus/market/count_buy_item.yml", "confirm.lore").stream().map(s -> PlaceholderUtils.replace(
                        s,
                        true,
                        new Pair<>("%categories%", String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(sellItem.getTags()))),
                        new Pair<>("%seller%", sellItem.getPlayer()),
                        new Pair<>("%full_price%", Formatter.formatPrice(sellItem.getPrice() * count)),
                        new Pair<>("%price%", Formatter.formatPrice(sellItem.getPrice()))
                )).toList());
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                itemStack.setAmount(count);
                inventory.setItem(ConfigManager.getInt("design/menus/market/count_buy_item.yml", "confirm.slot", 2), itemStack);
            }
            Pair<ItemStack, Integer> inc1 = ConfigUtils.buildItem("design/menus/market/count_buy_item.yml", "increase1", "&x&0&5&F&B&0&0▶ Увеличить на 1 единиц");
            inventory.setItem(ConfigManager.getInt("design/menus/market/count_buy_item.yml", "increase1.slot", 3), inc1.getLeft());
            Pair<ItemStack, Integer> inc10 = ConfigUtils.buildItem("design/menus/market/count_buy_item.yml", "increase10", "&x&0&5&F&B&0&0▶ Увеличить на 10 единиц");
            inventory.setItem(ConfigManager.getInt("design/menus/market/count_buy_item.yml", "increase10.slot", 4), inc10.getLeft());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public CountBuyItem setPlayer(Player player){
        this.viewer = player;
        return this;
    }
}
