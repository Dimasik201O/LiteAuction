package org.dimasik.liteauction.frontend.commands;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.exceptions.NotAPlayerException;
import org.dimasik.liteauction.backend.storage.models.GuiData;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class CommandExecutor implements TabExecutor {
    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return true;
        }
        if(args.length < 1){
            try {
                GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
                if(guiData.getAuctionType() == AuctionType.MARKET || !ConfigManager.getBoolean("settings/settings", "enable-bids", true)) {
                    org.dimasik.liteauction.frontend.menus.market.menus.Main main = new org.dimasik.liteauction.frontend.menus.market.menus.Main(1);
                    main.setPlayer(player);
                    main.setSortingType(guiData.getMarketSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.compile().open();
                }
                else {
                    org.dimasik.liteauction.frontend.menus.bids.menus.Main main = new org.dimasik.liteauction.frontend.menus.bids.menus.Main(1);
                    main.setPlayer(player);
                    main.setSortingType(guiData.getBidsSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.compile().open();
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(Parser.color(
                        ConfigManager.getString("design/commands/main.yml", "error", "&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия.")
                ));
            }
            return true;
        }

        String subCommandKey = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandKey);
        if(subCommand == null || (!subCommand.getRequiredPermission().isEmpty() && !player.hasPermission(subCommand.getRequiredPermission()))){
            ConfigManager
                    .getStringList("design/commands/main.yml", "usage-message")
                    .stream()
                    .map(Parser::color)
                    .forEach(player::sendMessage);
            return true;
        }

        if(subCommand.getRequiredArgs() + 1 > args.length){
            return true;
        }

        subCommand.execute(player, command, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for(SubCommand subCommand : subCommands.values()) {
                if(subCommand.getRequiredPermission().isEmpty() || commandSender.hasPermission(subCommand.getRequiredPermission())) {
                    completions.add(subCommand.getName());
                }
            }
        }
        else {
            SubCommand subCommand = subCommands.get(args[0]);
            if(subCommand != null && (subCommand.getRequiredPermission().isEmpty() || commandSender.hasPermission(subCommand.getRequiredPermission()))){
                try {
                    completions.addAll(subCommand.getTabCompletes(commandSender, args));
                } catch (NotAPlayerException e) {
                    completions.add("Exception : NotAPlayerException at executing " + subCommand.getName());
                }
            }
        }

        List<String> completions1 = new ArrayList<>();
        for(String s : completions){
            if(s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())){
                completions1.add(s);
            }
        }

        return completions1;
    }
}
