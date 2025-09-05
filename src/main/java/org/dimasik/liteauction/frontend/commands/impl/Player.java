package org.dimasik.liteauction.frontend.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.dimasik.liteauction.frontend.commands.SubCommand;
import org.dimasik.liteauction.frontend.menus.Main;

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
            Main main = new Main(1);
            main.setPlayer(player);
            main.setTarget(target);
            main.compile().open();
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
