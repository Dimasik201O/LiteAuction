package org.dimasik.liteauction.backend.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

import static org.dimasik.liteauction.backend.utils.ItemNameUtil.getLocalizedItemName;

public class ItemHoverUtil {
    public static void sendHoverItemMessage(Player player, String message, ItemStack hoverItem) {
        String[] parts = message.split("%item%", 2);

        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(parts[0]));

        TextComponent itemComponent = new TextComponent(TextComponent.fromLegacyText(getItemDisplayName(hoverItem)));
        itemComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, createHoverContent(hoverItem)));

        if (textComponent.getExtra() != null && !textComponent.getExtra().isEmpty()) {
            BaseComponent last = (BaseComponent) textComponent.getExtra().get(textComponent.getExtra().size() - 1);
            itemComponent.setColor(last.getColorRaw());
            itemComponent.setBold(last.isBoldRaw());
            itemComponent.setItalic(last.isItalicRaw());
            itemComponent.setUnderlined(last.isUnderlinedRaw());
            itemComponent.setStrikethrough(last.isStrikethroughRaw());
            itemComponent.setObfuscated(last.isObfuscatedRaw());
        }

        textComponent.addExtra(itemComponent);

        if (parts.length > 1) {
            textComponent.addExtra(new TextComponent(TextComponent.fromLegacyText(parts[1])));
        }

        player.spigot().sendMessage(textComponent);
    }

    private static Item createHoverContent(ItemStack item) {
        String nbtString = NBTUtil.getNBTAsString(item);
        return new Item(
                item.getType().getKey().toString(),
                item.getAmount(),
                net.md_5.bungee.api.chat.ItemTag.ofNbt(nbtString.isEmpty() ? "{}" : nbtString)
        );
    }

    private static String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return "[" + getLocalizedItemName(item.getType()) + "]";
    }

    public static String getHoverItemMessage(String message, ItemStack hoverItem) {
        try {
            return ItemEncrypt.encodeItem(hoverItem) + " " + message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}