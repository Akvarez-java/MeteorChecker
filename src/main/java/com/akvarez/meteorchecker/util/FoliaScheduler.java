package com.akvarez.meteorchecker.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class FoliaScheduler {

    private static final boolean IS_FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    private FoliaScheduler() {}

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runForPlayer(Plugin plugin, Player player, Runnable task) {
        if (!player.isOnline()) return;
        if (IS_FOLIA) {
            player.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runForPlayerDelayed(Plugin plugin, Player player, Runnable task, long delayTicks) {
        if (!player.isOnline()) return;
        if (IS_FOLIA) {
            player.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runGlobalTimer(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelayTicks, periodTicks);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelayTicks, periodTicks);
        }
    }
}
