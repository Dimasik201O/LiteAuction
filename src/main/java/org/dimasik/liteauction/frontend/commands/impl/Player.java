package org.dimasik.liteauction.frontend.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.storage.models.GuiData;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;

public class Player extends SubCommand {
    public Player(String name) {
        super(name);
    }

    @Override
    public void execute(org.bukkit.entity.Player player, Command command, String[] args) {
        String target = args[1];
        if(target.matches("^[a-zA-Z0-9_]+$") && target.length() >= 3 && target.length() <= 16){
            try {
                GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
                if(guiData.getAuctionType() == AuctionType.MARKET) {
                    org.dimasik.liteauction.frontend.menus.market.menus.Main main = new org.dimasik.liteauction.frontend.menus.market.menus.Main(1);
                    main.setTarget(target);
                    main.setSortingType(guiData.getMarketSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.setPlayer(player);
                    main.compile().open();
                }
                else {
                    org.dimasik.liteauction.frontend.menus.bids.menus.Main main = new org.dimasik.liteauction.frontend.menus.bids.menus.Main(1);
                    main.setTarget(target);
                    main.setSortingType(guiData.getBidsSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.setPlayer(player);
                    main.compile().open();
                }
            } catch (Exception e) {
                player.sendMessage(Parser.color(
                        ConfigManager.getString("design/commands/main.yml", "error", "&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия.")
                ));
            }
        }
        else{
            leaveUsage(player);
        }
    }

    @Override
    public List<String> getTabCompletes(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length == 2){
            for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
            }
        }
        return completions;
    }

    @Override
    public int getRequiredArgs() {
        return 1;
    }

    @Override
    public String getRequiredPermission() {
        return "";
    }
}
