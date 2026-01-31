package com.codex.battleground;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatStateManager {
    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final Map<UUID, EntityState> entityStates = new ConcurrentHashMap<>();

    public PlayerState getPlayerState(UUID playerId) {
        return playerStates.computeIfAbsent(playerId, id -> new PlayerState());
    }

    public EntityState getEntityState(UUID entityId) {
        return entityStates.computeIfAbsent(entityId, id -> new EntityState());
    }

    public void clearEntityState(UUID entityId) {
        entityStates.remove(entityId);
    }

    public void clearPlayerState(UUID playerId) {
        playerStates.remove(playerId);
    }
}
