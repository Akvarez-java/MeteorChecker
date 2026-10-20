package com.akvarez.meteorchecker;

import com.akvarez.meteorchecker.listener.MeteorPacketListener;
import com.akvarez.meteorchecker.probe.ProbeManager;
import com.akvarez.meteorchecker.util.FoliaScheduler;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class MeteorChecker extends JavaPlugin {

    private ProbeManager probeManager;
    private MeteorPacketListener packetListener;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PacketEvents.getAPI().init();

        this.probeManager = new ProbeManager(this);
        this.packetListener = new MeteorPacketListener(this, probeManager);

        PacketEvents.getAPI().getEventManager().registerListener(packetListener);

        // Sweeper task every 200 ticks (10s) using Folia global scheduler
        FoliaScheduler.runGlobalTimer(this, probeManager::sweepTimeouts, 200L, 200L);

        getLogger().info("MeteorChecker enabled with PacketEvents! Folia mode: " + FoliaScheduler.isFolia());
    }

    @Override
    public void onDisable() {
        if (packetListener != null) {
            try {
                PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
            } catch (Exception ignored) {}
        }
        PacketEvents.getAPI().terminate();

        if (probeManager != null) {
            probeManager.clearAll();
        }
        getLogger().info("MeteorChecker disabled.");
    }

    public ProbeManager getProbeManager() {
        return probeManager;
    }
}
