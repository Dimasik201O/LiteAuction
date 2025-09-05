package org.dimasik.liteauction.frontend.commands;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.exceptions.NotAPlayerException;
import org.dimasik.liteauction.backend.utils.Parser;

import java.util.List;

public abstract class SubCommand {
    @Getter
    private String name;

    public SubCommand(String name) {
        this.name = name;
    }

    public abstract void execute(Player player, Command command, String[] args);

    public abstract List<String> getTabCompletes(CommandSender sender, String[] args) throws NotAPlayerException;

    public abstract int getRequiredArgs();

    public void register(){
        LiteAuction.getInstance().getCommandExecutor().getSubCommands().put(name, this);
    }

    public abstract String getRequiredPermission();

    public void leaveUsage(Player player){
        player.sendMessage(Parser.color(" &#00D4FBПомощь по системе аукциона:"));
        player.sendMessage(Parser.color(""));
        player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah &f — открыть меню аукциона"));
        player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah sell <цена> &f— выставить товар на Рынок"));
        player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah sell <цена> full &f— выставить товар (весь лот)"));
        player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah sell auto &f— продать товар (рыночная цена)"));
        player.sendMessage(Parser.color(" &#00D4FB&n▍&#00D4FB /ah search [название] &f— найти предметы на аукционе"));
        player.sendMessage(Parser.color(" &#00D4FB▍&#00D4FB /ah player <никнейм> &f— все товары на рынке"));
        player.sendMessage(Parser.color(""));
    }

    public Player requirePlayer(CommandSender sender){
        if(sender instanceof Player player){
            return player;
        }
        else{
            throw new NotAPlayerException();
        }
    }
}
