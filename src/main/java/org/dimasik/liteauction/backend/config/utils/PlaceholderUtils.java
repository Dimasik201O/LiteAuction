package org.dimasik.liteauction.backend.config.utils;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.utils.format.Parser;

@UtilityClass
public class PlaceholderUtils {
    @SafeVarargs
    public static String replace(String origin, boolean applyParser, Pair<String, String>... replaces){
        for(Pair<String, String> replace : replaces){
            while (origin.contains(replace.getLeft())) {
                origin = origin.replace(replace.getLeft(), replace.getRight());
            }
        }
        if(applyParser) origin = Parser.color(origin);
        return origin;
    }

    @SafeVarargs
    public static String replace(Player source, String origin, boolean applyParser, Pair<String, String>... replaces){
        for(Pair<String, String> replace : replaces){
            while (origin.contains(replace.getLeft())) {
                origin = origin.replace(replace.getLeft(), replace.getRight());
            }
        }
        if(applyParser) origin = Parser.color(origin);
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(source, origin);
        }
        return origin;
    }
}
