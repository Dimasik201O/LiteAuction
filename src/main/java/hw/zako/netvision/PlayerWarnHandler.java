package hw.zako.netvision;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.utils.format.Parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class PlayerWarnHandler {
    private static final HashMap<UUID, Integer> playerWarnings = new HashMap<>();
    @Getter
    private static final HashMap<UUID, BukkitTask> checkingPlayers = new HashMap<>();
    private static final int WARNING_LIMIT = 2;

    public static void addWarning(Player player){
        playerWarnings.put(
                player.getUniqueId(),
                playerWarnings.getOrDefault(
                        player.getUniqueId(),
                        0
                )
                        + 1
        );

        if(playerWarnings.get(player.getUniqueId()) >= WARNING_LIMIT){
            playerWarnings.remove(player.getUniqueId());
            if(checkingPlayers.containsKey(player.getUniqueId())){
                onPunishment(player);
                return;
            }
            checkingPlayers.put(
                    player.getUniqueId(),
                    new BukkitRunnable(){
                        @Override
                        public void run(){
                            onPunishment(player);
                        }
                    }.runTaskLater(LiteAuction.getInstance(), 100)
            );
            int random = new Random().nextInt(3);
            switch (random) {
                case 0 -> {
                    new BukkitRunnable() {
                        int q = new Random().nextInt(4) + 2;

                        @Override
                        public void run() {
                            if (q <= 0) {
                                super.cancel();
                                return;
                            }
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_WEAK, 1, 1);
                            q--;
                        }
                    }.runTaskTimer(LiteAuction.getInstance(), 2, 4);
                }
                case 1 -> {
                    player.sendMessage(Parser.color("&6[&cNetVision &6-> &cя&6] &fпривет"));
                }
                default -> {
                    player.teleport(player.getLocation().clone().add(0, 4, 0));
                }
            }
        }
    }

    private static void onPunishment(Player player){
        if(player == null){
            return;
        }
        checkingPlayers.remove(player.getUniqueId());

        player.sendMessage("");
        player.sendMessage("  -----  -----  -----  ");
        player.sendMessage("");
        player.sendMessage("   ВЫ ПОДОЗРЕВАЕТЕСЬ");
        player.sendMessage("    В ИСПОЛЬЗОВАНИИ");
        player.sendMessage("  АВТОБАЯ ЕБАНОГО БЛЯТЬ");
        player.sendMessage("");
        player.sendMessage("  -----  -----  -----  ");
        player.sendMessage("");
    }
}
