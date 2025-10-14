package org.dimasik.liteauction.backend.storage.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.ItemEncryptUtil;

import java.io.IOException;

@Setter
@Getter
@AllArgsConstructor
public class HistoryItem {
    private final int id;
    private String player;
    private String buyer;
    private String itemStack;
    private int amount;
    private int price;
    private long time;
    private boolean displayed;

    public ItemStack decodeItemStack() {
        try {
            return ItemEncryptUtil.decodeItem(itemStack);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void encodeAndPutItemStack(ItemStack itemStack) {
        try {
            this.itemStack = ItemEncryptUtil.encodeItem(itemStack.asOne());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HistoryItem clone() {
        return new HistoryItem(
                id,
                player,
                buyer,
                itemStack,
                amount,
                price,
                time,
                displayed
        );
    }
}