package org.dimasik.liteauction.frontend.commands.impl;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.commands.SubCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Admin extends SubCommand {
    public Admin(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        if(args[1].equalsIgnoreCase("getTags")){
            ItemStack itemStack = player.getItemInHand();
            if(itemStack != null){
                player.sendMessage("Полные теги:");
                sendTagsWithHover(player, TagUtil.getAllTags(itemStack));
                player.sendMessage("");
                player.sendMessage("Частичные теги:");
                sendTagsWithHover(player, TagUtil.getPartialTags(itemStack));
            }
            else{
                player.sendMessage("У вас нет предмета в руке");
            }
        }
    }

    @Override
    public List<String> getTabCompletes(String[] args) {
        if(args.length == 2){
            return List.of("getTags");
        }
        return List.of();
    }

    @Override
    public int getRequiredArgs() {
        return 1;
    }

    @Override
    public String getRequiredPermission() {
        return "liteauction.admin";
    }

    public static void sendTagsWithHover(Player player, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            player.sendMessage("Нет тегов для отображения.");
            return;
        }

        ComponentBuilder message = new ComponentBuilder("Теги: ").color(net.md_5.bungee.api.ChatColor.GRAY);

        int i = 0;
        for (String tag  : tags) {
            TextComponent tagComponent = new TextComponent(tag);
            tagComponent.setColor(ChatColor.WHITE);

            tagComponent.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Нажмите чтобы скопировать\n")
                            .color(net.md_5.bungee.api.ChatColor.GRAY)
                            .append(tag).color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .create()
            ));

            tagComponent.setClickEvent(new ClickEvent(
                    ClickEvent.Action.COPY_TO_CLIPBOARD,
                    tag
            ));

            message.append(tagComponent);

            if (i < tags.size() - 1) {
                message.append(", ").color(net.md_5.bungee.api.ChatColor.GRAY);
            }
            i++;
        }

        player.spigot().sendMessage(message.create());
    }
}
