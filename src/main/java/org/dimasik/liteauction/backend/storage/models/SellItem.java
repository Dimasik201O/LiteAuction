package org.dimasik.liteauction.backend.storage.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.ItemEncrypt;

import java.io.IOException;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
public class SellItem {
    private final int id;
    private String player;
    private String itemStack;
    private Set<String> tags;
    private int price;
    private int amount;
    private boolean byOne;
    private long createTime;

    public ItemStack decodeItemStack() {
        try {
            return ItemEncrypt.decodeItem(itemStack);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void encodeAndPutItemStack(ItemStack itemStack) {
        try {
            this.itemStack = ItemEncrypt.encodeItem(itemStack.asOne());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}