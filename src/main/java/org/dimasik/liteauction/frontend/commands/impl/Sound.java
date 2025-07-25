package org.dimasik.liteauction.frontend.commands.impl;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.commands.SubCommand;

import java.util.List;

public class Sound extends SubCommand {
    public Sound(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        try {
            if (LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                player.sendMessage(Parser.color("&#00D5FB▶ &fЗвуки &#FB2222выключены&f."));
                LiteAuction.getInstance().getDatabaseManager().getSoundsManager().setSoundToggle(player.getName(), false);
            }
            else{
                player.sendMessage(Parser.color("&#00D5FB▶ &fЗвуки &#05F700включены&f."));
                LiteAuction.getInstance().getDatabaseManager().getSoundsManager().setSoundToggle(player.getName(), true);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public List<String> getTabCompletes(String[] args) {
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
