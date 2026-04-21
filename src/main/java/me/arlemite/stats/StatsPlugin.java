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

        // Авто-синхронизация онлайн игроков в БД
        int intervalSeconds = getConfig().getInt("update-interval-seconds", 300);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                int msgs = StatsListener.popMessages(p.getUniqueId());
                PlayerStatsData data = StatsManager.collect(p, msgs);
                database.savePlayerStatsAsync(data);
            }
        }, intervalSeconds * 20L, intervalSeconds * 20L);
    }

    @Override
    public void onDisable() {
        // Финальное сохранение при выключении сервера
        for (Player p : Bukkit.getOnlinePlayers()) {
            int msgs = StatsListener.popMessages(p.getUniqueId());
            PlayerStatsData data = StatsManager.collect(p, msgs);
            database.savePlayerStatsAsync(data); // База закроется чуть позже
        }
        if (database != null) {
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
