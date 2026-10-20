package com.akvarez.meteorchecker.listener;

import com.akvarez.meteorchecker.MeteorChecker;
import com.akvarez.meteorchecker.probe.ProbeManager;
import com.akvarez.meteorchecker.probe.ProbeSession;
import com.akvarez.meteorchecker.util.FoliaScheduler;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MeteorPacketListener extends PacketListenerAbstract {

    private final MeteorChecker plugin;
    private final ProbeManager probeManager;

    public MeteorPacketListener(MeteorChecker plugin, ProbeManager probeManager) {
        super(PacketListenerPriority.HIGHEST);
        this.plugin = plugin;
        this.probeManager = probeManager;
    }

    @Override
    public void onUserLogin(UserLoginEvent event) {
        UUID uuid = event.getUser().getUUID();
        long delay = plugin.getConfig().getLong("probe-delay", 25L);

        // Run delayed probe on server scheduler after player enters world
        FoliaScheduler.runGlobalDelayed(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && !player.hasPermission("meteorchecker.bypass")) {
                probeManager.probe(player);
            }
        }, delay);
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        probeManager.removeSession(event.getUser().getUUID());
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.UPDATE_SIGN) {
            return;
        }

        UUID uuid = event.getUser().getUUID();
        ProbeSession session = probeManager.getSession(uuid);
        if (session == null) return;

        WrapperPlayClientUpdateSign packet = new WrapperPlayClientUpdateSign(event);
        Vector3i pos = packet.getBlockPosition();

        if (pos == null || !pos.equals(session.getPosition())) {
            return; // Normal world sign
        }

        // Cancel packet so server doesn't process text on fake sign block
        event.setCancelled(true);

        if (!session.getHandled().compareAndSet(false, true)) {
            return;
        }

        probeManager.removeSession(uuid);

        String[] lines = packet.getTextLines();
        if (lines == null) return;

        boolean detected = false;
        for (int i = 0; i < 4 && i < lines.length; i++) {
            String returned = lines[i] == null ? "" : lines[i].trim();
            String fallback = session.getFallbacks()[i];
            String key = session.getKeys()[i];

            // If client resolved Meteor translation key, text != fallback AND text != raw key
            if (!returned.isEmpty() && !returned.equals(fallback) && !returned.equals(key)) {
                detected = true;
                break;
            }
        }

        if (detected) {
            Player player = (Player) event.getPlayer();
            if (player != null) {
                String rawMsg = plugin.getConfig().getString("kick-message", "<red>meteor client detect please remove and join back");
                Component kickMsg = MiniMessage.miniMessage().deserialize(rawMsg);
                FoliaScheduler.runForPlayer(plugin, player, () -> {
                    if (player.isOnline()) {
                        player.kick(kickMsg);
                    }
                });
            }
        }
    }
}
