package org.dimasik.liteauction.frontend.commands.impl;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.ItemEncrypt;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.commands.SubCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
        else if(args[1].equalsIgnoreCase("deleteItem")){
            if(args.length < 3){
                player.sendMessage("Укажите ID");
                return;
            }
            try {
                int id = Integer.parseInt(args[2]);
                Optional<SellItem> optional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(id).get();
                if(optional.isEmpty()){
                    player.sendMessage("Нет предмета с таким ID");
                }
                else{
                    SellItem sellItem = optional.get();
                    ItemHoverUtil.sendHoverItemMessage(player, Parser.color("Предмет %item%&f удален с аукциона игрока " + sellItem.getPlayer()), sellItem.decodeItemStack());
                    LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(id);
                    player.sendMessage("");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("ID не число");
            } catch (ExecutionException | InterruptedException e) {
                player.sendMessage("Ошибка удаления предмета. Смотри консоль.");
                Bukkit.getLogger().warning("Ошибка при удалении предмета через команду: " + e);
            }
        }
        else if(args[1].equalsIgnoreCase("addUnsoldItemTo")){
            if(args.length < 3){
                player.sendMessage("Укажите игрока.");
                return;
            }
            String target = args[2];

            ItemStack itemStack = player.getItemInHand();
            if(itemStack == null || itemStack.getType().isAir()){
                player.sendMessage(Parser.color("&#FB2222▶ &fДля продажи товара &#FB2222возьмите предмет &fв главную руку."));
                return;
            }

            int price = 1;
            try{
                price = Integer.parseInt(args[3]);
            }
            catch (Exception ignore){}

            int itemCount = itemStack.getAmount();
            if((price / itemCount) * itemCount < 1){
                player.sendMessage("Цена <= 0!");
                return;
            }

            boolean byOne = (args.length == 3) || args[4].equalsIgnoreCase("true");

            try {
                LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().addItem(target, ItemEncrypt.encodeItem(itemStack), TagUtil.getAllTags(itemStack), price, itemCount, byOne);
                ItemHoverUtil.sendHoverItemMessage(player, Parser.color("Предмет %item%&f добавлен в истекшие предметы игроку " + target + " по цене " + (price * itemCount) + " (" + price + " за 1 ед.)"), itemStack);
                player.setItemInHand(null);
            } catch (IOException e) {
                player.sendMessage("Ошибка кодирования предмета. Смотри консоль.");
                Bukkit.getLogger().warning("Ошибка кодирования предмета: " + e);
            }
        }
        else if(args[1].equalsIgnoreCase("resellItems")){
            if(args.length < 3){
                player.sendMessage("Укажите игрока.");
                return;
            }
            String target = args[2];

            LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().resellItems(target).thenAccept(
                (v) -> {
                    player.sendMessage("Предметы перевыставлены.");
                }
            );
        }
        else if (args[1].equalsIgnoreCase("changelog")) {
            if (args.length < 4) {
                player.sendMessage("Использование: /ah admin changelog [с какой версии] [по какую версию]");
                return;
            }

            String fromVersion = args[2];
            String toVersion = args[3];

            Bukkit.getScheduler().runTaskAsynchronously(LiteAuction.getInstance(), () -> {
                try {
                    URL url = new URL("https://raw.githubusercontent.com/Dimasik201O/LiteAuction/master/change.log");
                    List<String> lines = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                    }

                    List<String> changelog = extractChangelog(lines, fromVersion, toVersion);

                    if (changelog.isEmpty()) {
                        player.sendMessage("Нет изменений между " + fromVersion + " и " + toVersion);
                    } else {
                        player.sendMessage(Parser.color("&#00D4FB▶ &f▶ Ченджлог &#00D4FB" + fromVersion + " &f→ &#00D4FB" + toVersion + "&f:"));
                        for (String change : changelog) {
                            if(change.startsWith("[!]")) {
                                player.sendMessage(Parser.color(" &7" + change));
                            }
                            else{
                                player.sendMessage(Parser.color("&#FF2222▶ " + change));
                            }
                        }
                    }

                } catch (IOException e) {
                    player.sendMessage("Ошибка загрузки ченджлога. Смотри консоль.");
                    Bukkit.getLogger().warning("Ошибка чтения ченджлога: " + e);
                }
            });
        }

    }

    @Override
    public List<String> getTabCompletes(CommandSender sender, String[] args) {
        if(args.length == 2){
            return List.of("getTags", "deleteItem", "addUnsoldItemTo", "resellItems", "changelog");
        }
        else if(args.length == 3){
            if(args[1].equalsIgnoreCase("resellItems")){
                return Bukkit
                    .getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .toList();
            }
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

    private List<String> extractChangelog(List<String> lines, String from, String to) {
        List<String> result = new ArrayList<>();
        boolean inRange = false;

        for (String line : lines) {
            if (line.startsWith("[v")) {
                if (line.contains(from) && !inRange) {
                    inRange = true;
                }
                if (inRange) {
                    result.add(line);
                }
                if (line.contains(to)) {
                    break;
                }
            } else if (inRange && line.startsWith("[!]")) {
                result.add(line);
            }
        }
        return result;
    }

}
