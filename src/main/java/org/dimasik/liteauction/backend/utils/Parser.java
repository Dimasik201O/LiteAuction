package org.dimasik.liteauction.backend.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final boolean SUPPORTS_RGB;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        int subVersion = Integer.parseInt(version.replace("v", "").replace("1_", "").replaceAll("_R\\d", ""));
        SUPPORTS_RGB = subVersion >= 16;
    }

    public static String color(String message) {
        if (message == null) return "";

        if (SUPPORTS_RGB) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuilder buffer = new StringBuilder();

            while (matcher.find()) {
                String hex = matcher.group(1);
                try {
                    String replacement = ChatColor.of("#" + hex).toString();
                    matcher.appendReplacement(buffer, replacement);
                } catch (Exception e) {
                    matcher.appendReplacement(buffer, "");
                }
            }

            matcher.appendTail(buffer);
            message = buffer.toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendClickableMessage(Player player, List<String> lines, String url, String hover) {
        for (String line : lines) {
            hover = color(hover);
            BaseComponent[] components = parseColoredText(line);
            for (BaseComponent comp : components) {
                comp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                comp.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(hover).create()
                ));
            }
            player.spigot().sendMessage(components);
        }
    }

    public static String stripColor(String text) {
        if (text == null) return "";

        String result = text;
        if (SUPPORTS_RGB) {
            result = HEX_PATTERN.matcher(result).replaceAll("");
        }

        return ChatColor.stripColor(result);
    }

    private static BaseComponent[] parseColoredText(String message) {
        List<BaseComponent> components = new java.util.ArrayList<>();

        if (message == null) return new BaseComponent[0];

        message = ChatColor.translateAlternateColorCodes('&', message);

        StringBuilder builder = new StringBuilder();
        ChatColor currentColor = null;
        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        int lastIndex = 0;

        Matcher matcher = HEX_PATTERN.matcher(message);

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                String preceding = message.substring(lastIndex, matcher.start());
                builder.append(preceding);
            }

            String hex = matcher.group(1);
            try {
                currentColor = ChatColor.of("#" + hex);
            } catch (Exception e) {
                currentColor = null;
            }

            lastIndex = matcher.end();
        }

        builder.append(message.substring(lastIndex));

        if (!builder.isEmpty()) {
            TextComponent comp = new TextComponent(builder.toString());
            applyFormatting(comp, bold, italic, underlined, strikethrough, obfuscated, currentColor);
            components.add(comp);
        }

        return components.toArray(new BaseComponent[0]);
    }

    public static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "Не указано";
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm:ss", new Locale("ru", "RU"));
        String formattedDate = sdf.format(timestamp);

        formattedDate = formattedDate.replaceFirst("([А-Я])", "$1".toLowerCase());

        formattedDate = formattedDate
                .replaceAll("янв\\.?", "янв.")
                .replaceAll("фев\\.?", "фев.")
                .replaceAll("мар\\.?", "мар.")
                .replaceAll("апр\\.?", "апр.")
                .replaceAll("май\\.?", "май.")
                .replaceAll("июн\\.?", "июн.")
                .replaceAll("июл\\.?", "июл.")
                .replaceAll("авг\\.?", "авг.")
                .replaceAll("сен\\.?", "сен.")
                .replaceAll("окт\\.?", "окт.")
                .replaceAll("ноя\\.?", "ноя.")
                .replaceAll("дек\\.?", "дек.");

        return formattedDate;
    }

    private static void applyFormatting(TextComponent component, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated, ChatColor color) {
        if (bold) component.setBold(true);
        if (italic) component.setItalic(true);
        if (underlined) component.setUnderlined(true);
        if (strikethrough) component.setStrikethrough(true);
        if (obfuscated) component.setObfuscated(true);
        if (color != null) component.setColor(color);
    }

    public static String formatNumber(int number) {
        return String.format(Locale.US, "%,d", number);
    }
}