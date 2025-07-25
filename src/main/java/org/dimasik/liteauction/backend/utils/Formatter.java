package org.dimasik.liteauction.backend.utils;

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

    public static String getTimeUntilExpiration(SellItem sellItem) {
        long expirationTime = sellItem.getCreateTime() + (12 * 60 * 60 * 1000);
        long currentTime = System.currentTimeMillis();
        long remainingTime = expirationTime - currentTime;

        if (remainingTime <= 0) {
            return "0ч. 0мин. 0сек.";
        }

        long seconds = remainingTime / 1000;
        long hours = seconds / 3600;
        seconds = seconds % 3600;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%dч. %dмин. %dсек.", hours, minutes, seconds);
    }

    public static String getTimeUntilDeletion(UnsoldItem unsoldItem) {
        long deletionTime = unsoldItem.getCreateTime() + (7 * 24 * 60 * 60 * 1000);
        long currentTime = System.currentTimeMillis();
        long remainingTime = deletionTime - currentTime;

        if (remainingTime <= 0) {
            return "0д. 0ч. 0мин.";
        }

        long seconds = remainingTime / 1000;
        long days = seconds / 86400;
        seconds = seconds % 86400;
        long hours = seconds / 3600;
        seconds = seconds % 3600;
        long minutes = seconds / 60;

        return String.format("%dд. %dч. %dмин.", days, hours, minutes);
    }
}
