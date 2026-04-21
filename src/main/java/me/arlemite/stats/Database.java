package me.arlemite.stats;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class Database {

    private HikariDataSource dataSource;

    public void connect() {
        HikariConfig config = new HikariConfig();
        String host = StatsPlugin.get().getConfig().getString("database.host");
        int port = StatsPlugin.get().getConfig().getInt("database.port");
        String db = StatsPlugin.get().getConfig().getString("database.database");
        
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false");
        config.setUsername(StatsPlugin.get().getConfig().getString("database.user"));
        config.setPassword(StatsPlugin.get().getConfig().getString("database.password"));
        config.setMaximumPoolSize(10);
        
        dataSource = new HikariDataSource(config);
        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16),
                playtime BIGINT,
                advancements INT,
                kills INT,
                mob_kills INT,
                deaths INT,
                blocks_broken INT,
                blocks_placed INT,
                items_crafted INT,
                walked_m INT,
                swum_m INT,
                messages INT
            );
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Синхронное сохранение (основная логика)
    public void savePlayerStatsSync(PlayerStatsData data) {
        String sql = """
            INSERT INTO player_stats (uuid, name, playtime, advancements, kills, mob_kills, deaths, blocks_broken, blocks_placed, items_crafted, walked_m, swum_m, messages)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            name=VALUES(name), playtime=VALUES(playtime), advancements=VALUES(advancements),
            kills=VALUES(kills), mob_kills=VALUES(mob_kills), deaths=VALUES(deaths),
            blocks_broken=VALUES(blocks_broken), blocks_placed=VALUES(blocks_placed),
            items_crafted=VALUES(items_crafted), walked_m=VALUES(walked_m),
            swum_m=VALUES(swum_m), messages=messages + VALUES(messages);
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.uuid);
            ps.setString(2, data.name);
            ps.setLong(3, data.playtime);
            ps.setInt(4, data.advancements);
            ps.setInt(5, data.kills);
            ps.setInt(6, data.mobKills);
            ps.setInt(7, data.deaths);
            ps.setInt(8, data.blocksBroken);
            ps.setInt(9, data.blocksPlaced);
            ps.setInt(10, data.itemsCrafted);
            ps.setInt(11, data.walkedM);
            ps.setInt(12, data.swumM);
            ps.setInt(13, data.sessionMessages);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Асинхронная обертка для обычных сохранений (выход с сервера, таймер)
    public void savePlayerStatsAsync(PlayerStatsData data) {
        Bukkit.getScheduler().runTaskAsynchronously(StatsPlugin.get(), () -> savePlayerStatsSync(data));
    }

    public CompletableFuture<PlayerStatsData> getPlayerStats(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM player_stats WHERE name = ? LIMIT 1;";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PlayerStatsData data = new PlayerStatsData();
                    data.name = rs.getString("name");
                    data.playtime = rs.getLong("playtime");
                    data.advancements = rs.getInt("advancements");
                    data.kills = rs.getInt("kills");
                    data.mobKills = rs.getInt("mob_kills");
                    data.deaths = rs.getInt("deaths");
                    data.blocksBroken = rs.getInt("blocks_broken");
                    data.blocksPlaced = rs.getInt("blocks_placed");
                    data.itemsCrafted = rs.getInt("items_crafted");
                    data.walkedM = rs.getInt("walked_m");
                    data.swumM = rs.getInt("swum_m");
                    data.sessionMessages = rs.getInt("messages");
                    return data;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public void close() {
        if (dataSource != null) dataSource.close();
    }
}

