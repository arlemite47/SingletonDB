package me.arlemite.stats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StatsPlugin extends JavaPlugin {

    private static StatsPlugin instance;
    private Database database;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        database = new Database();
        database.connect();

        getCommand("stats").setExecutor(new StatsCommand());
        getCommand("tonstats").setExecutor(new TonStatsCommand());
        
        getServer().getPluginManager().registerEvents(new StatsListener(), this);

        // Таймер теперь обычный (не Asynchronously), потому что Bukkit API запрещено трогать асинхронно.
        int intervalSeconds = getConfig().getInt("update-interval-seconds", 300);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                int msgs = StatsListener.popMessages(p.getUniqueId());
                PlayerStatsData data = StatsManager.collect(p, msgs);
                database.savePlayerStatsAsync(data); // База сохраняет в фоне
            }
        }, intervalSeconds * 20L, intervalSeconds * 20L);
    }

    @Override
    public void onDisable() {
        if (database != null) {
            // Финальное синхронное сохранение (Bukkit убивает асинхронные задачи при выключении)
            for (Player p : Bukkit.getOnlinePlayers()) {
                int msgs = StatsListener.popMessages(p.getUniqueId());
                PlayerStatsData data = StatsManager.collect(p, msgs);
                database.savePlayerStatsSync(data); 
            }
            database.close();
        }
    }

    public static StatsPlugin get() {
        return instance;
    }

    public Database getDatabase() {
        return database;
    }
}
