package com.akvarez.meteorchecker;

import com.akvarez.meteorchecker.listener.JoinListener;
import com.akvarez.meteorchecker.listener.SignPacketListener;
import com.akvarez.meteorchecker.probe.ProbeManager;
import com.akvarez.meteorchecker.util.FoliaScheduler;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MeteorChecker extends JavaPlugin {

    private ProbeManager probeManager;
    private SignPacketListener packetListener;

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("ProtocolLib is required for MeteorChecker to function! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        this.probeManager = new ProbeManager(this, protocolManager);

        // Register Bukkit listeners
        getServer().getPluginManager().registerEvents(new JoinListener(this, probeManager), this);

        // Register ProtocolLib packet listener
        this.packetListener = new SignPacketListener(this, probeManager);
        protocolManager.addPacketListener(packetListener);

        // Sweeper task every 200 ticks (10s) using Folia global scheduler
        FoliaScheduler.runGlobalTimer(this, probeManager::sweepTimeouts, 200L, 200L);

        getLogger().info("MeteorChecker enabled successfully! Folia mode: " + FoliaScheduler.isFolia());
    }

    @Override
    public void onDisable() {
        if (packetListener != null) {
            try {
                ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
            } catch (Exception ignored) {}
        }
        if (probeManager != null) {
            probeManager.clearAll();
        }
        getLogger().info("MeteorChecker disabled.");
    }

    public ProbeManager getProbeManager() {
        return probeManager;
    }
}
