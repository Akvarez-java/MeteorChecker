package com.akvarez.meteorchecker.listener;

import com.akvarez.meteorchecker.MeteorChecker;
import com.akvarez.meteorchecker.probe.ProbeManager;
import com.akvarez.meteorchecker.probe.ProbeSession;
import com.akvarez.meteorchecker.util.FoliaScheduler;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class SignPacketListener extends PacketAdapter {

    private final MeteorChecker plugin;
    private final ProbeManager probeManager;

    private static final Component KICK_MESSAGE = Component.text("meteor client detect please remove and join back")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, false);

    public SignPacketListener(MeteorChecker plugin, ProbeManager probeManager) {
        super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.UPDATE_SIGN);
        this.plugin = plugin;
        this.probeManager = probeManager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        ProbeSession session = probeManager.getSession(uuid);
        if (session == null) return;

        BlockPosition pos = event.getPacket().getBlockPositionModifier().read(0);
        if (pos == null || !pos.equals(session.getPosition())) {
            return; // Normal world sign
        }

        // Virtual sign packet; prevent server from trying to edit real block
        event.setCancelled(true);

        if (!session.getHandled().compareAndSet(false, true)) {
            return;
        }

        probeManager.removeSession(uuid);

        String[] lines = event.getPacket().getStringArrays().read(0);
        if (lines == null) return;

        boolean detected = false;
        for (int i = 0; i < 4 && i < lines.length; i++) {
            String returned = lines[i] == null ? "" : lines[i].trim();
            String fallback = session.getFallbacks()[i];
            String key = session.getKeys()[i];

            // If the client resolved the Meteor key, the returned text is different from our fallback AND different from the raw key
            if (!returned.isEmpty() && !returned.equals(fallback) && !returned.equals(key)) {
                detected = true;
                break;
            }
        }

        if (detected) {
            // Folia-safe execution on player's region thread
            FoliaScheduler.runForPlayer(plugin, player, () -> {
                if (player.isOnline()) {
                    player.kick(KICK_MESSAGE);
                }
            });
        }
    }
}
