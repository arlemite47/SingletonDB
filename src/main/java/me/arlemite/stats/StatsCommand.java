package me.arlemite.stats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String targetName;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Укажите ник игрока.");
                return true;
            }
            targetName = sender.getName();
        } else {
            targetName = args[0];
        }

        Player targetOnline = Bukkit.getPlayerExact(targetName);

        if (targetOnline != null) {
            // Игрок онлайн - собираем актуальные данные прямо сейчас (без задержек)
            int currentSessionMsgs = StatsListener.peekMessages(targetOnline.getUniqueId());
            PlayerStatsData liveData = StatsManager.collect(targetOnline, currentSessionMsgs);
            
            // Запрашиваем из БД прошлые сообщения чата, чтобы сложить их с текущими
            StatsPlugin.get().getDatabase().getPlayerStats(targetName).thenAccept(dbData -> {
                if (dbData != null) {
                    liveData.sessionMessages += dbData.sessionMessages;
                }
                sendStats(sender, liveData);
            });
        } else {
            // Игрок оффлайн - ищем только в БД
            StatsPlugin.get().getDatabase().getPlayerStats(targetName).thenAccept(data -> {
                if (data == null) {
                    sender.sendMessage("§cИгрок не найден или статистика еще не собрана.");
                    return;
                }
                sendStats(sender, data);
            });
        }

        return true;
    }

    private void sendStats(CommandSender sender, PlayerStatsData data) {
        long hours = data.playtime / 3600;
        long minutes = (data.playtime % 3600) / 60;

        sender.sendMessage("§6=== Статистика " + data.name + " ===");
        sender.sendMessage("⏱️ Игровое время: " + hours + " ч. " + minutes + " мин.");
        sender.sendMessage("🏆 Достижения: " + data.advancements);
        sender.sendMessage("⚔️ Убийства игроков: " + data.kills);
        sender.sendMessage("🗡️ Убийства мобов: " + data.mobKills);
        sender.sendMessage("💀 Смерти: " + data.deaths);
        sender.sendMessage("⛏️ Сломано блоков: " + data.blocksBroken);
        sender.sendMessage("🧱 Поставлено блоков: " + data.blocksPlaced);
        sender.sendMessage("🔨 Создано предметов: " + data.itemsCrafted);
        sender.sendMessage("🚶 Пройдено: " + data.walkedM + " м");
        sender.sendMessage("🏊 Проплыто: " + data.swumM + " м");
        sender.sendMessage("💬 Сообщения в чате: " + data.sessionMessages);
    }
}
