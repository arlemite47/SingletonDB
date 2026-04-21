package me.arlemite.stats;

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
        getServer().getPluginManager().registerEvents(new StatsListener(), this);
    }

    public static StatsPlugin get() {
        return instance;
    }

    public Database getDatabase() {
        return database;
    }
}
