// --- TonStatsCommand.java ---
package me.arlemite.stats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TonStatsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tonstats.admin")) {
                sender.sendMessage("§cНет прав.");
                return true;
            }
            StatsPlugin.get().reloadConfig();
            StatsPlugin.get().getDatabase().close();
            StatsPlugin.get().getDatabase().connect();
            sender.sendMessage("§a[TonStats] Конфиг и база данных перезагружены!");
            return true;
        }
        return false;
    }
}

