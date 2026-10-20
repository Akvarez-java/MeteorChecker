package com.akvarez.meteorchecker.probe;

import com.akvarez.meteorchecker.MeteorChecker;
import com.akvarez.meteorchecker.util.FoliaScheduler;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ProbeManager {

    public static final String[] METEOR_KEYS = {
        "key.category.meteor-client",
        "key.meteor-client.open-gui",
        "key.meteor-client.open-commands",
        "meteor-client.category.combat"
    };

    private static final BlockData SIGN_DATA = Material.OAK_SIGN.createBlockData();

    private final MeteorChecker plugin;
    private final ConcurrentHashMap<UUID, ProbeSession> activeSessions = new ConcurrentHashMap<>();

    public ProbeManager(MeteorChecker plugin) {
        this.plugin = plugin;
    }

    public ProbeSession getSession(UUID playerUuid) {
        return activeSessions.get(playerUuid);
    }

    public void removeSession(UUID playerUuid) {
        activeSessions.remove(playerUuid);
    }

    public void probe(Player player) {
        if (!player.isOnline()) return;

        World world = player.getWorld();
        Location loc = player.getLocation();

        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int y = Math.max(world.getMinHeight(), Math.min(world.getMaxHeight() - 1, loc.getBlockY() - 3));

        Vector3i pos = new Vector3i(x, y, z);
        Location signLoc = new Location(world, x, y, z);
        BlockData original = world.getBlockAt(x, y, z).getBlockData();

        String[] fallbacks = new String[4];
        List<Component> lines = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            String fb = "m" + Long.toString(ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE), 36);
            fallbacks[i] = fb;
            lines.add(Component.translatable()
                    .key(METEOR_KEYS[i])
                    .fallback(fb)
                    .build());
        }

        ProbeSession session = new ProbeSession(player.getUniqueId(), pos, METEOR_KEYS, fallbacks, System.currentTimeMillis());
        activeSessions.put(player.getUniqueId(), session);

        // 1. Send virtual sign block & translatable text to player
        player.sendBlockChange(signLoc, SIGN_DATA);
        player.sendSignChange(signLoc, lines, DyeColor.BLACK, false);

        // 2. Send Open Sign Editor packet via PacketEvents
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerOpenSignEditor(pos, true));

        // 3. Force container close after 2 ticks
        FoliaScheduler.runForPlayerDelayed(plugin, player, () -> {
            if (player.isOnline()) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerCloseWindow(0));
            }
        }, 2L);

        // 4. Revert fake sign block after 6 ticks
        FoliaScheduler.runForPlayerDelayed(plugin, player, () -> {
            if (player.isOnline()) {
                player.sendBlockChange(signLoc, original);
            }
        }, 6L);
    }

    public void sweepTimeouts() {
        long now = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> now - entry.getValue().getCreatedAtMillis() > 10000L);
    }

    public void clearAll() {
        activeSessions.clear();
    }
}
