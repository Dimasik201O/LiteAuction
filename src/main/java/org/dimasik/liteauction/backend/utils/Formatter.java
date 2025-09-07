package org.dimasik.liteauction.backend.utils;

import org.dimasik.liteauction.backend.mysql.models.BidItem;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.mysql.models.UnsoldItem;

public class Formatter {
    public static final String CURRENCY_SYMBOL = "¤";

    public static String formatPrice(int price) {
        if (price == 0) {
            return "0" + CURRENCY_SYMBOL;
        }

        String formatted = String.format("%,d", price)
                .replace(",", " ");

        return formatted + CURRENCY_SYMBOL;
    }

    public static String getTimeUntilExpiration(BidItem bidItem) {
        long remainingTime = bidItem.getExpiryTime() - System.currentTimeMillis();
        return formatTime(remainingTime);
    }

    public static String getTimeUntilExpiration(SellItem sellItem) {
        long expirationTime = sellItem.getCreateTime() + (12 * 60 * 60 * 1000);
        long remainingTime = expirationTime - System.currentTimeMillis();
        return formatTime(remainingTime);
    }

    public static String getTimeUntilDeletion(UnsoldItem unsoldItem) {
        long deletionTime = unsoldItem.getCreateTime() + (7 * 24 * 60 * 60 * 1000);
        long remainingTime = deletionTime - System.currentTimeMillis();
        return formatTime(remainingTime);
    }

    private static String formatTime(long remainingTime) {
        if (remainingTime <= 0) {
            return "0сек.";
        }

        long seconds = remainingTime / 1000;
        long days = seconds / 86400;
        seconds = seconds % 86400;
        long hours = seconds / 3600;
        seconds = seconds % 3600;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append("д. ");
        }
        if (hours > 0) {
            result.append(hours).append("ч. ");
        }
        if (minutes > 0) {
            result.append(minutes).append("мин. ");
        }
        if (seconds > 0 || result.isEmpty()) {
            result.append(seconds).append("сек.");
        }

        return result.toString().trim();
    }
}
