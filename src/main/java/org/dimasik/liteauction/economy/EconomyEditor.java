package org.dimasik.liteauction.economy;

public abstract class EconomyEditor {
    public abstract double getBalance(String player);
    public abstract void addBalance(String player, double count);
    public abstract void subtractBalance(String player, double count);
}
