package org.dimasik.liteauction.frontend.commands;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.exceptions.NotAPlayerException;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.market.menus.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class CommandExecutor implements TabExecutor {
    private final HashMap<String, SubCommand> subCommands = new HashMap<>();
    @Getter
    private static final HashMap<Player, AuctionType> auctionTypes = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return true;
        }
        if(args.length < 1){
            if(auctionTypes.getOrDefault(player, AuctionType.MARKET) == AuctionType.MARKET) {
                new org.dimasik.liteauction.frontend.menus.market.menus.Main(1).setPlayer(player).compile().open();
            }
            else {
                new org.dimasik.liteauction.frontend.menus.bids.menus.Main(1).setPlayer(player).compile().open();
            }
            return true;
        }

        String subCommandKey = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandKey);
        if(subCommand == null || (!subCommand.getRequiredPermission().isEmpty() && !player.hasPermission(subCommand.getRequiredPermission()))){
            player.sendMessage(Parser.color(" &#00D4FBПомощь по системе аукциона:"));
            player.sendMessage(Parser.color(""));
            player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah &f — открыть меню аукциона"));
            player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah sell <цена> &f— выставить товар на Рынок"));
            player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah sell <цена> full &f— выставить товар (весь лот)"));
            player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah sell <цена> <шаг> [время] &f- выставить товар (Ставки)"));
            player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah sell auto &f— продать товар (рыночная цена)"));
            player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah search [название] &f— найти предметы на аукционе"));
            player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah player <никнейм> &f— все товары на игрока"));
            player.sendMessage(Parser.color(" &#00D4FB▍&#00D4FB /ah sound &f— переключить слышимость звуков аукциона"));
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
