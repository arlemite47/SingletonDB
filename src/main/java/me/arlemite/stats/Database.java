package me.arlemite.stats;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private Connection connection;

    public void connect() {
        try {
            String host = StatsPlugin.get().getConfig().getString("database.host");
            String db = StatsPlugin.get().getConfig().getString("database.database");
            String user = StatsPlugin.get().getConfig().getString("database.user");
            String pass = StatsPlugin.get().getConfig().getString("database.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + "/" + db,
                    user,
                    pass
            );

            System.out.println("Database connected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
