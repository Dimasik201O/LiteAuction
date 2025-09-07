package org.dimasik.liteauction.frontend.commands.impl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.backend.utils.ItemNameUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.commands.SubCommand;
import org.dimasik.liteauction.frontend.menus.market.menus.Main;

import java.util.*;

public class Search extends SubCommand {
    public Search(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        ItemNameUtil.loadTranslationsIfNeeded();

        if(args.length < 2) {
            ItemStack itemStack = player.getItemInHand() == null || player.getItemInHand().getType().isAir() ? player.getInventory().getItemInOffHand() : player.getItemInHand();
            if(itemStack == null || itemStack.getType().isAir()){
                player.sendMessage(Parser.color("&#FB2222▶ &fПо вашему запросу не найдено ни одного фильтра."));
                return;
            }
            Main main = new Main(1);
            main.setFilters(TagUtil.getPartialTags(itemStack));
            main.setPlayer(player).compile().open();
        }
        else{
            String find = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if(!ItemNameUtil.containsTag(find)){
                player.sendMessage(Parser.color("&#FB2222▶ &fПо вашему запросу не найдено ни одного фильтра."));
                return;
            }
            Main main = new Main(1);
            main.setFilters(ItemNameUtil.escapeTag(find));
            main.setPlayer(player).compile().open();
        }
    }

    @Override
    public List<String> getTabCompletes(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return List.of();
        }

        ItemNameUtil.loadTranslationsIfNeeded();
        String all = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();

        List<String> allTags = new ArrayList<>();
        List<String> completions = new ArrayList<>();
        allTags.addAll(ItemNameUtil.getReverseTranslations().keySet());
        allTags.addAll(ItemNameUtil.getCustomTags().keySet());

        try {
            for (int i = 0; i < allTags.size(); i++) {
                String current = allTags.get(i);
                int startIndex = all.length() - args[args.length - 1].length();

                if (startIndex < 0) {
                    startIndex = 0;
                } else if (startIndex > current.length()) {
                    startIndex = current.length();
                }

                if(current.toLowerCase().startsWith(all.toLowerCase())) {
                    completions.add(current.substring(startIndex).startsWith(" ") ? current.substring(startIndex + 1) : current.substring(startIndex));
                }
            }
        } catch (Exception e) {
            return List.of();
        }

        return completions;
    }

    @Override
    public int getRequiredArgs() {
        return 0;
    }

    @Override
    public String getRequiredPermission() {
        return "";
    }
}
