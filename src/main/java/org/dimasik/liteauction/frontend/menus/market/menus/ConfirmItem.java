package org.dimasik.liteauction.frontend.menus.market.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
public class ConfirmItem extends AbstractMenu {
    @Setter
    private boolean forceClose = false;
    private SellItem sellItem;
    private Main back;

    public ConfirmItem(SellItem sellItem, Main back){
        this.sellItem = sellItem;
        this.back = back;
    }

    public ConfirmItem compile(){
        try{
            inventory = ConfigUtils.buildInventory(this, "design/menus/market/confirm_item.yml", "inventory-type",
                    ConfigManager.getString("design/menus/market/confirm_item.yml", "gui-title", "&x&0&0&D&8&F&F Покупка предмета")
            );
            List<Integer> approveSlots = ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "approve.slot");
            if(!approveSlots.isEmpty()){
                Pair<ItemStack, Integer> item = ConfigUtils.buildItem("design/menus/market/confirm_item.yml", "approve", "&x&0&5&F&B&0&0 ▶ &x&D&5&D&B&D&CКупить");
                for(Integer s : approveSlots){
                    inventory.setItem(s, item.getLeft());
                }
            }
            List<Integer> neutralSlots = ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "neutral.slot");
            if(!neutralSlots.isEmpty()){
                Pair<ItemStack, Integer> item = ConfigUtils.buildItem("design/menus/market/confirm_item.yml", "neutral", " ");
                for(Integer s : neutralSlots){
                    inventory.setItem(s, item.getLeft());
                }
            }
            List<Integer> cancelSlots = ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "cancel.slot");
            if(!cancelSlots.isEmpty()){
                Pair<ItemStack, Integer> item = ConfigUtils.buildItem("design/menus/market/confirm_item.yml", "cancel", "&x&F&F&2&2&2&2 ▶ &x&D&5&D&B&D&CОтменить покупку");
                for(Integer s : cancelSlots){
                    inventory.setItem(s, item.getLeft());
                }
            }
            if(true){
                ItemStack itemStack = sellItem.decodeItemStack();
                itemStack.setAmount(sellItem.getAmount());
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.addAll(ConfigManager.getStringList("design/menus/market/confirm_item.yml", "item.lore").stream().map(s -> PlaceholderUtils.replace(
                        s,
                        true,
                        new Pair<>("%categories%", String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(sellItem.getTags()))),
                        new Pair<>("%seller%", sellItem.getPlayer()),
                        new Pair<>("%full_price%", Formatter.formatPrice(sellItem.getPrice() * sellItem.getAmount())),
                        new Pair<>("%price%", Formatter.formatPrice(sellItem.getPrice()))
                )).toList());
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(ConfigManager.getInt("design/menus/market/confirm_item.yml", "item.slot", 13), itemStack);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ConfirmItem setPlayer(Player player){
        this.viewer = player;
        return this;
    }
}
