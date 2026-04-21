package me.arlemite.stats;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsListener implements Listener {

    private static final ConcurrentHashMap<UUID, Integer> sessionMessages = new ConcurrentHashMap<>();

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        sessionMessages.put(id, sessionMessages.getOrDefault(id, 0) + 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        int messages = sessionMessages.getOrDefault(id, 0);
        
        PlayerStatsData data = StatsManager.collect(e.getPlayer(), messages);
        StatsPlugin.get().getDatabase().savePlayerStatsAsync(data);
        
        sessionMessages.remove(id);
    }

    // Извлекает и обнуляет счетчик (для таймера)
    public static int popMessages(UUID uuid) {
        return sessionMessages.remove(uuid) != null ? sessionMessages.get(uuid) : 0;
    }

    // Просто смотрит счетчик (для команды /stats)
    public static int peekMessages(UUID uuid) {
        return sessionMessages.getOrDefault(uuid, 0);
    }
}
