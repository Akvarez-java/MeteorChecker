package com.akvarez.meteorchecker.listener;

import com.akvarez.meteorchecker.MeteorChecker;
import com.akvarez.meteorchecker.probe.ProbeManager;
import com.akvarez.meteorchecker.util.FoliaScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinListener implements Listener {

    private final MeteorChecker plugin;
    private final ProbeManager probeManager;

    public JoinListener(MeteorChecker plugin, ProbeManager probeManager) {
        this.plugin = plugin;
        this.probeManager = probeManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("meteorchecker.bypass")) return;

        // Wait 25 ticks (1.25s) after join to probe during terrain transition
        FoliaScheduler.runForPlayerDelayed(plugin, player, () -> {
            if (player.isOnline()) {
                probeManager.probe(player);
            }
        }, 25L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        probeManager.removeSession(event.getPlayer().getUniqueId());
    }
}
