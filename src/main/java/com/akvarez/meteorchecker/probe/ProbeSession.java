package com.akvarez.meteorchecker.probe;

import com.github.retrooper.packetevents.util.Vector3i;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ProbeSession {

    private final UUID playerUuid;
    private final Vector3i position;
    private final String[] keys;
    private final String[] fallbacks;
    private final long createdAtMillis;
    private final AtomicBoolean handled = new AtomicBoolean(false);

    public ProbeSession(UUID playerUuid, Vector3i position, String[] keys, String[] fallbacks, long createdAtMillis) {
        this.playerUuid = playerUuid;
        this.position = position;
        this.keys = keys;
        this.fallbacks = fallbacks;
        this.createdAtMillis = createdAtMillis;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Vector3i getPosition() {
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
