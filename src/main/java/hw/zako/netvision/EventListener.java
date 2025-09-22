package hw.zako.netvision;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EventListener implements Listener {
    @EventHandler
    public void on(PlayerMoveEvent event){
        if(event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            if (PlayerWarnHandler.getCheckingPlayers().containsKey(event.getPlayer().getUniqueId())) {
                PlayerWarnHandler.getCheckingPlayers().remove(event.getPlayer().getUniqueId()).cancel();
            }
        }
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent event){
        if(PlayerWarnHandler.getCheckingPlayers().containsKey(event.getPlayer().getUniqueId())){
            PlayerWarnHandler.getCheckingPlayers().remove(event.getPlayer().getUniqueId()).cancel();
        }
    }
}
