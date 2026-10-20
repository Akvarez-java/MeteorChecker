package com.akvarez.meteorchecker.probe;

import com.comphenix.protocol.wrappers.BlockPosition;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ProbeSession {

    private final UUID playerUuid;
    private final BlockPosition position;
    private final String[] keys;
    private final String[] fallbacks;
    private final long createdAtMillis;
    private final AtomicBoolean handled = new AtomicBoolean(false);

    public ProbeSession(UUID playerUuid, BlockPosition position, String[] keys, String[] fallbacks, long createdAtMillis) {
        this.playerUuid = playerUuid;
        this.position = position;
        this.keys = keys;
        this.fallbacks = fallbacks;
        this.createdAtMillis = createdAtMillis;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public BlockPosition getPosition() {
        return position;
    }

    public String[] getKeys() {
        return keys;
    }

    public String[] getFallbacks() {
        return fallbacks;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public AtomicBoolean getHandled() {
        return handled;
    }
}
