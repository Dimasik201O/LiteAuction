package org.dimasik.liteauction.frontend.commands.impl;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.commands.SubCommand;

import java.util.List;

public class Help extends SubCommand {
    public Help(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        leaveUsage(player);
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
