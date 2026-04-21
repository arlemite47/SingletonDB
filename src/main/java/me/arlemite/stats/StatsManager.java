package me.arlemite.stats;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class StatsManager {

    public static PlayerStatsData collect(Player p, int sessionMessages) {
        PlayerStatsData data = new PlayerStatsData();
        data.uuid = p.getUniqueId().toString();
        data.name = p.getName();
        
        data.playtime = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20; // в секундах
        data.kills = p.getStatistic(Statistic.PLAYER_KILLS);
        data.mobKills = p.getStatistic(Statistic.MOB_KILLS);
        data.deaths = p.getStatistic(Statistic.DEATHS);
        data.walkedM = p.getStatistic(Statistic.WALK_ONE_CM) / 100;
        data.swumM = p.getStatistic(Statistic.SWIM_ONE_CM) / 100;
        
        data.advancements = countAdvancements(p);
        data.blocksBroken = sumStatistic(p, Statistic.MINE_BLOCK);
        data.blocksPlaced = sumStatistic(p, Statistic.USE_ITEM, true); // Для блоков
        data.itemsCrafted = sumStatistic(p, Statistic.CRAFT_ITEM);
        data.sessionMessages = sessionMessages;

        return data;
    }

    private static int sumStatistic(Player p, Statistic stat) {
        return sumStatistic(p, stat, false);
    }

    private static int sumStatistic(Player p, Statistic stat, boolean onlyBlocks) {
        int total = 0;
        for (Material m : Material.values()) {
            if (onlyBlocks && !m.isBlock()) continue;
            try {
                total += p.getStatistic(stat, m);
            } catch (IllegalArgumentException ignored) {
                // Если статистика не применима к этому материалу
            }
        }
        return total;
    }

    private static int countAdvancements(Player p) {
        int count = 0;
        Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
        while (it.hasNext()) {
            Advancement adv = it.next();
            // Пропускаем технические достижения (рецепты)
            if (adv.getKey().getKey().startsWith("recipes/")) continue;
            if (p.getAdvancementProgress(adv).isDone()) count++;
        }
        return count;
    }
}

