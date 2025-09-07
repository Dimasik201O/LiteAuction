package org.dimasik.liteauction.frontend.commands.impl;

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
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.*;
import org.dimasik.liteauction.frontend.commands.SubCommand;
import org.dimasik.liteauction.frontend.commands.models.AutoSellData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sell extends SubCommand {
    private final HashMap<Player, AutoSellData> autoSells = new HashMap<>();

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
                            player.sendMessage(Parser.color("&#00D5FB▶ &#D2D7D8Нельзя продавать шалкер с предметами."));
                            return;
                        }
                    }
                }
            }

            try {
                List<SellItem> sellItems = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(MarketSortingType.CHEAPEST_PER_UNIT, TagUtil.getPartialTags(itemStack)).get();
                int priceForOne = ConfigManager.getDEFAULT_AUTO_PRICE();
                if(!sellItems.isEmpty()){
                    priceForOne = sellItems.get(0).getPrice();
                }

                int itemCount = itemStack.getAmount();
                if(confirm){
                    if(!autoSells.containsKey(player)) return;
                    AutoSellData model = autoSells.get(player);
                    if(model.price == priceForOne && model.itemStack.isSimilar(itemStack)) {
                        boolean isFull = model.full;
                        autoSells.remove(player);
                        if ((priceForOne / itemCount) * itemCount > 1000000000) {
                            leaveUsage(player);
                            return;
                        }
                        if (canSell(player)) {
                            LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().addItem(player.getName(), ItemEncrypt.encodeItem(itemStack.asOne()), TagUtil.getAllTags(itemStack), priceForOne, itemCount, isFull);
                            ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &fВы успешно выставили на продажу &#9AF5FB%item%&f &#9AF5FBx" + itemCount), itemStack);
                            player.setItemInHand(null);
                        }
                        return;
                    }
                }
                player.sendMessage(Parser.color("&#00D4FB▶ &fВведите &#00D4FB/ah sell auto confirm&f, чтобы подтвердить продажу. Полная цена: &#FBA800" + Formatter.formatPrice(priceForOne * itemCount) + "&f, за 1 шт.: &#FBA800" + Formatter.formatPrice(priceForOne)));
                autoSells.put(player, new AutoSellData(priceForOne, itemStack, full));
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
            double rawPrice = Double.parseDouble(number);
            switch (numberType){
                case K -> rawPrice*=1000;
                case KK, M -> rawPrice*=1000000;
            }
            int price = (int) rawPrice;
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
                            player.sendMessage(Parser.color("&#00D5FB▶ &#D2D7D8Нельзя продавать шалкер с предметами."));
                            return;
                        }
                    }
                }
            }

            try {
                if(!full && args.length > 2){
                    try{
                        NumberType stepType = NumberType.DEFAULT;
                        String stepNumber = args[2];
                        if(args[2].endsWith("kk")){
                            stepType = NumberType.KK;
                            stepNumber = stepNumber.substring(0, stepNumber.length() - 2);
                        }
                        else if(args[2].endsWith("m")){
                            stepType = NumberType.M;
                            stepNumber = stepNumber.substring(0, stepNumber.length() - 1);
                        }
                        else if(args[2].endsWith("k")){
                            stepType = NumberType.K;
                            stepNumber = stepNumber.substring(0, stepNumber.length() - 1);
                        }

                        double rawStep = Double.parseDouble(stepNumber);
                        switch (stepType){
                            case K -> rawStep*=1000;
                            case KK, M -> rawStep*=1000000;
                        }
                        int step = (int) rawStep;

                        if((double) price / 10 <= step){
                            player.sendMessage(Parser.color("&#FB2222▶ &fЦена шага не должна превышать 10% от начальной суммы"));
                            return;
                        }

                        long translatedTime = args.length > 3 ? parseTimeToSeconds(args[3]) : 86400L;
                        if(translatedTime == -1){
                            leaveUsage(player);
                            return;
                        }
                        if(translatedTime < 300 || translatedTime > 86400){
                            player.sendMessage(Parser.color("&#FB2222▶ &fВремя продажи предмета должна находиться в пределах &#FB22225мин...1д.&f."));
                            return;
                        }

                        if(canSell(player)) {
                            LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().addItem(player.getName(), ItemEncrypt.encodeItem(itemStack.asOne()), TagUtil.getAllTags(itemStack), price, step, (translatedTime * 1000) + System.currentTimeMillis());
                            ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &fВы успешно выставили на продажу &#9AF5FB%item%&f &#9AF5FBx" + itemStack.getAmount()), itemStack);
                            player.setItemInHand(null);
                        }
                    }
                    catch (NumberFormatException e){
                        leaveUsage(player);
                    }
                    return;
                }
                int itemCount = itemStack.getAmount();
                if((price / itemCount) * itemCount < 1){
                    leaveUsage(player);
                    return;
                }
                else if((price / itemCount) * itemCount > 1000000000){
                    leaveUsage(player);
                    return;
                }

                if(canSell(player)) {
                    LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().addItem(player.getName(), ItemEncrypt.encodeItem(itemStack.asOne()), TagUtil.getAllTags(itemStack), price / itemCount, itemCount, full);
                    ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &fВы успешно выставили на продажу &#9AF5FB%item%&f &#9AF5FBx" + itemCount), itemStack);
                    player.setItemInHand(null);
                }
            } catch (IOException e) {
                player.sendMessage(Parser.color("&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри кодировании предмета."));
            }
        } catch (NumberFormatException e) {
            leaveUsage(player);
        }
    }

    @Override
    public List<String> getTabCompletes(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        String lastArg = args[args.length - 1];
        switch (args.length){
            case 2:
                completions.add("auto");
                try{
                    int cnt = Integer.parseInt(lastArg);
                    completions.add(cnt + "k");
                    completions.add(cnt + "kk");
                    completions.add(cnt + "m");
                } catch (NumberFormatException ignore) {
                    try{
                        double cnt = Double.parseDouble(lastArg);
                        completions.add(cnt + "k");
                        completions.add(cnt + "kk");
                        completions.add(cnt + "m");
                    } catch (NumberFormatException ignored) {

                    }
                }
                break;
            case 3:
                if(args[1].equalsIgnoreCase("auto")){
                    completions.add("full");
                    Player player = super.requirePlayer(sender);
                    ItemStack itemStack = player.getItemInHand();
                    if(itemStack != null && !itemStack.getType().isAir()){
                        if(autoSells.containsKey(player)) {
                            AutoSellData model = autoSells.get(player);
                            if(model.itemStack.isSimilar(itemStack)) {
                                completions.add("confirm");
                            }
                        }
                    }
                }
                else {
                    try {
                        int cnt = Integer.parseInt(lastArg);
                        completions.add(cnt + "k");
                        completions.add(cnt + "kk");
                        completions.add(cnt + "m");
                    } catch (NumberFormatException ignore) {
                        try {
                            double cnt = Double.parseDouble(lastArg);
                            completions.add(cnt + "k");
                            completions.add(cnt + "kk");
                            completions.add(cnt + "m");
                        } catch (NumberFormatException ignored) {
                            completions.add("full");
                        }
                    }
                }
                break;
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

    private int getItemsInAuction(String player){
        try {
            List<SellItem> sellItems = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItems(player).get();
            return sellItems.size();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException();
        }
    }

    private int getPlayerSlots(Player player, int bound){
        for(int i = bound; i > 0; i--){
            if(player.hasPermission("liteauction.slots." + i)){
                return i;
            }
        }
        return 0;
    }

    private boolean canSell(Player player){
        if(getItemsInAuction(player.getName()) >= getPlayerSlots(player, 250)){
            player.sendMessage(Parser.color("&#FB2222▶ &fВы не можете больше выставлять товары на аукцион."));
            return false;
        }
        return true;
    }

    public static long parseTimeToSeconds(String input) {
        if (input == null || input.trim().isEmpty()) {
            return -1;
        }

        input = input.trim().toLowerCase();
        boolean hasTimeSymbol = input.matches(".*[дdhчмm].*");
        if (!hasTimeSymbol) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        long totalSeconds = 0;
        boolean validFormat = false;
        Pattern pattern = Pattern.compile("(\\d+)([дdhчмm])");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            try {
                long value = Integer.parseInt(matcher.group(1));
                String symbol = matcher.group(2);

                switch (symbol) {
                    case "д":
                    case "d":
                        totalSeconds += value * 24 * 60 * 60;
                        break;
                    case "ч":
                    case "h":
                        totalSeconds += value * 60 * 60;
                        break;
                    case "м":
                    case "m":
                        totalSeconds += value * 60;
                        break;
                }
                validFormat = true;
            } catch (NumberFormatException e) {
                return -1;

            }
        }
        if (!validFormat) {
            return -1;
        }
        return totalSeconds;
    }
}
