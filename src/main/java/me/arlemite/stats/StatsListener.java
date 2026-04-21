package me.arlemite.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.UUID;

public class StatsListener implements Listener {

    private static final HashMap<UUID, Integer> messages = new HashMap<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        messages.put(id, messages.getOrDefault(id, 0) + 1);
    }

    public static int getMessages(UUID uuid) {
        return messages.getOrDefault(uuid, 0);
    }
}
