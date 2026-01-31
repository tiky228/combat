package com.codex.battleground;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StunManager {
    private final CombatStateManager combatStateManager;

    public StunManager(CombatStateManager combatStateManager) {
        this.combatStateManager = combatStateManager;
    }

    public void applyStun(Player player, long durationMs) {
        long until = System.currentTimeMillis() + durationMs;
        combatStateManager.getPlayerState(player.getUniqueId()).setStunnedUntil(until);
    }

    public void applyStun(LivingEntity entity, long durationMs) {
        long until = System.currentTimeMillis() + durationMs;
        combatStateManager.getEntityState(entity.getUniqueId()).setStunnedUntil(until);
    }

    public boolean isPlayerStunned(Player player) {
        return remainingPlayerStunMs(player) > 0;
    }

    public long remainingPlayerStunMs(Player player) {
        long until = combatStateManager.getPlayerState(player.getUniqueId()).getStunnedUntil();
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public boolean isEntityStunned(LivingEntity entity) {
        return remainingEntityStunMs(entity) > 0;
    }

    public long remainingEntityStunMs(LivingEntity entity) {
        long until = combatStateManager.getEntityState(entity.getUniqueId()).getStunnedUntil();
        return Math.max(0L, until - System.currentTimeMillis());
    }
}
