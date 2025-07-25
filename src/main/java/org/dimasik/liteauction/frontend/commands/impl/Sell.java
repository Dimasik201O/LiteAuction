package org.dimasik.liteauction.frontend.commands.impl;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.NumberType;
import org.dimasik.liteauction.backend.enums.SortingType;
import org.dimasik.liteauction.backend.mysql.DatabaseManager;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.*;
import org.dimasik.liteauction.frontend.commands.SubCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sell extends SubCommand {
    public Sell(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        boolean full = args.length > 2 && args[2].equalsIgnoreCase("full");
        boolean confirm = args.length > 2 && args[2].equalsIgnoreCase("confirm");
        NumberType numberType = NumberType.DEFAULT;
        String number = args[1];
        if(number.equalsIgnoreCase("auto")){
            ItemStack itemStack = player.getItemInHand();
            if(itemStack == null || itemStack.getType().isAir()){
                player.sendMessage(Parser.color("&#FB2222▶ &fДля продажи товара &#FB2222возьмите предмет &fв главную руку."));
                return;
            }
            if(itemStack.getType().toString().endsWith("SHULKER_BOX")) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
                if (blockStateMeta != null) {
                    BlockState blockState = blockStateMeta.getBlockState();
                    if (blockState instanceof ShulkerBox) {
                        ShulkerBox shulkerBoxState = (ShulkerBox) blockState;
                        if (!shulkerBoxState.getInventory().isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&#00D5FB▶ &#D2D7D8Нельзя продавать шалкер с предметами."));
                            return;
                        }
                    }
                }
            }

            try {
                List<SellItem> sellItems = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(SortingType.CHEAPEST_PER_UNIT, TagUtil.getPartialTags(itemStack)).get();
                int priceForOne = ConfigManager.getDEFAULT_AUTO_PRICE();
                if(!sellItems.isEmpty()){
                    priceForOne = sellItems.get(0).getPrice();
                }

                int itemCount = itemStack.getAmount();
                if(confirm){
                    LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().addItem(player.getName(), ItemEncrypt.encodeItem(itemStack.asOne()), TagUtil.getAllTags(itemStack), priceForOne, itemCount, full);
                    ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &fВы успешно выставили на продажу &#9AF5FB%item%&f &#9AF5FBx" + itemCount), itemStack);
                    player.setItemInHand(null);
                }
                else{
                    player.sendMessage(Parser.color("&#00D4FB▶ &fВведите &#00D4FB/ah sell auto confirm&f, чтобы подтвердить продажу. Полная цена: &#FBA800" + Formatter.formatPrice(priceForOne * itemCount) + "&f, за 1 шт.: &#FBA800" + Formatter.formatPrice(priceForOne)));
                }
            } catch (Exception e) {

            }
            return;
        }
        if(args[1].endsWith("kk")){
            numberType = NumberType.KK;
            number = number.substring(0, number.length() - 2);
        }
        else if(args[1].endsWith("m")){
            numberType = NumberType.M;
            number = number.substring(0, number.length() - 1);
        }
        else if(args[1].endsWith("k")){
            numberType = NumberType.K;
            number = number.substring(0, number.length() - 1);
        }
        try {
            int price = Integer.parseInt(number);
            switch (numberType){
                case K -> price*=1000;
                case KK, M -> price*=1000000;
            }
            ItemStack itemStack = player.getItemInHand();
            if(itemStack == null || itemStack.getType().isAir()){
                player.sendMessage(Parser.color("&#FB2222▶ &fДля продажи товара &#FB2222возьмите предмет &fв главную руку."));
                return;
            }
            if(itemStack.getType().toString().endsWith("SHULKER_BOX")) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
                if (blockStateMeta != null) {
                    BlockState blockState = blockStateMeta.getBlockState();
                    if (blockState instanceof ShulkerBox) {
                        ShulkerBox shulkerBoxState = (ShulkerBox) blockState;
                        if (!shulkerBoxState.getInventory().isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&#00D5FB▶ &#D2D7D8Нельзя продавать шалкер с предметами."));
                            return;
                        }
                    }
                }
            }

            try {
                int itemCount = itemStack.getAmount();
                if((price / itemCount) * itemCount < 1){
                    leaveUsage(player);
                    return;
                }
                else if((price / itemCount) * itemCount > 1000000000){
                    leaveUsage(player);
                    return;
                }
                LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().addItem(player.getName(), ItemEncrypt.encodeItem(itemStack.asOne()), TagUtil.getAllTags(itemStack), price / itemCount, itemCount, full);
                ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &fВы успешно выставили на продажу &#9AF5FB%item%&f &#9AF5FBx" + itemCount), itemStack);
                player.setItemInHand(null);
            } catch (IOException e) {
                player.sendMessage(Parser.color("&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри кодировании предмета."));
            }
        } catch (NumberFormatException e) {
            leaveUsage(player);
        }
    }

    @Override
    public List<String> getTabCompletes(String[] args) {
        List<String> completions = new ArrayList<>();
        String lastArg = args[args.length - 1];
        if(args.length == 2){
            completions.add("auto");
        }
        else if(args.length == 3){
            if(args[1].equalsIgnoreCase("auto")){
                completions.add("confirm");
            }
            completions.add("full");
        }
        switch (args.length){
            case 2:
                try{
                    int cnt = Integer.parseInt(lastArg);
                    completions.add(cnt + "k");
                    completions.add(cnt + "kk");
                    completions.add(cnt + "m");
                } catch (NumberFormatException e) {}
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
