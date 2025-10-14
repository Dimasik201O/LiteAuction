package org.dimasik.liteauction.frontend.commands.impl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.exceptions.NotAPlayerException;
import org.dimasik.liteauction.backend.storage.models.HistoryItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.commands.SubCommand;

import java.util.List;

public class History extends SubCommand {
    public History(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        LiteAuction.getInstance().getDatabaseManager().getHistoryItems().getPlayerHistory(player.getName(), false).thenAccept((historyItems) -> {
            for(HistoryItem historyItem : historyItems){
                ItemStack itemStack = historyItem.decodeItemStack();
                player.sendMessage(PlaceholderUtils.replace(
                        player,
                        ConfigManager.getString("design/commands/history.yml", "history", "&7[%datetime%] &#00B4D4%buyer% &#FCFCFCкупил у вас &#9AF6FC%item% x%amount% &#FCFCFCза &#FCC700%format:full_price%"),
                        true,
                        new Pair<>("%datetime%", Formatter.formatDateTime(historyItem.getTime())),
                        new Pair<>("%buyer%", historyItem.getBuyer()),
                        new Pair<>("%item%", itemStack.getType().toString().toUpperCase()),
                        new Pair<>("%amount%", String.valueOf(historyItem.getAmount())),
                        new Pair<>("%price%", String.valueOf(historyItem.getPrice())),
                        new Pair<>("%full_price%", String.valueOf(historyItem.getPrice() * historyItem.getAmount())),
                        new Pair<>("%format:price%", Formatter.formatPrice(historyItem.getPrice())),
                        new Pair<>("%format:full_price%", Formatter.formatPrice(historyItem.getPrice() * historyItem.getAmount()))
                ));
            }
        });
    }

    @Override
    public List<String> getTabCompletes(CommandSender sender, String[] args) throws NotAPlayerException {
        return List.of();
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
