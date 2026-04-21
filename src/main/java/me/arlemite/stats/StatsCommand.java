package me.arlemite.stats;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) return true;
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("Игрок не найден");
                return true;
            }
        }

        long playTime = target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;

        sender.sendMessage("§6=== Статистика " + target.getName() + " ===");
        sender.sendMessage("⏱️ Время: " + playTime + " сек");
        sender.sendMessage("⚔️ PvP: " + target.getStatistic(Statistic.PLAYER_KILLS));
        sender.sendMessage("🗡️ Мобы: " + target.getStatistic(Statistic.MOB_KILLS));
        sender.sendMessage("💀 Смерти: " + target.getStatistic(Statistic.DEATHS));
        sender.sendMessage("⛏️ Сломано: " + target.getStatistic(Statistic.MINE_BLOCK));
        sender.sendMessage("🧱 Поставлено: " + target.getStatistic(Statistic.USE_ITEM));
        sender.sendMessage("🔨 Крафт: " + target.getStatistic(Statistic.CRAFT_ITEM));
        sender.sendMessage("🚶 Пройдено: " + target.getStatistic(Statistic.WALK_ONE_CM) / 100 + " м");
        sender.sendMessage("🏊 Проплыто: " + target.getStatistic(Statistic.SWIM_ONE_CM) / 100 + " м");

        return true;
    }
}
