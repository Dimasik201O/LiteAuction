package org.dimasik.liteauction.economy.impl;

import org.dimasik.liteauction.economy.EconomyEditor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEco extends EconomyEditor {
    private Economy economy;

    public VaultEco() {
        setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
    }

    @Override
    public double getBalance(String player) {
        if (economy == null) return 0;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        return economy.getBalance(offlinePlayer);
    }

    @Override
    public void addBalance(String player, double count) {
        if (economy == null || count <= 0) return;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        economy.depositPlayer(offlinePlayer, count);
    }

    @Override
    public void subtractBalance(String player, double count) {
        if (economy == null || count <= 0) return;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
        economy.withdrawPlayer(offlinePlayer, count);
    }
}