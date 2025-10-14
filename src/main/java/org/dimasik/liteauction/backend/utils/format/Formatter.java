package org.dimasik.liteauction.backend.utils.format;

import lombok.experimental.UtilityClass;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.storage.models.UnsoldItem;

import java.text.SimpleDateFormat;
import java.util.Date;

@UtilityClass
public class Formatter {
    public static final String CURRENCY_SYMBOL = ConfigManager.getString("settings/settings.yml", "currency-symbol", "¤");

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
        long expirationTime = sellItem.getCreateTime() + (ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200) * 1000);
        long remainingTime = expirationTime - System.currentTimeMillis();
        return formatTime(remainingTime);
    }

    public static String getTimeUntilDeletion(UnsoldItem unsoldItem) {
        long deletionTime = unsoldItem.getCreateTime() + (ConfigManager.getLong("settings/settings.yml", "lifetime.unsold", 604800) * 1000);
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

    public static String formatDateTime(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM HH:mm");
        return formatter.format(date);
    }
}