package org.dimasik.liteauction.economy.impl;

import org.bukkit.Bukkit;
import org.dimasik.liteauction.economy.EconomyEditor;
import org.dimasik.liteauction.frontend.commands.impl.Player;
import org.dimasik.stickeco.StickEcoAPI;

public class StickEco extends EconomyEditor {
    private StickEcoAPI economy;

    public StickEco(){
        setupEconomy();
    }

    private void setupEconomy(){
        if (Bukkit.getServer().getPluginManager().getPlugin("StickEco") == null) {
            return;
        }
        economy = new StickEcoAPI();
    }

    @Override
    public double getBalance(String player) {
        if (economy == null) return 0;
        return economy.getBalance(player.toLowerCase());
    }

    @Override
    public void addBalance(String player, double count) {
        if (economy == null || count <= 0) return;
        economy.addBalance(player.toLowerCase(), count);
    }

    @Override
    public void subtractBalance(String player, double count) {
        if (economy == null || count <= 0) return;
        economy.subtractBalance(player.toLowerCase(), count);
    }
}
