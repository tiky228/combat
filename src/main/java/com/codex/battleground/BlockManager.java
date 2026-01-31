package com.codex.battleground;

import org.bukkit.entity.Player;

public class BlockManager {
    private final CombatStateManager combatStateManager;

    public BlockManager(CombatStateManager combatStateManager) {
        this.combatStateManager = combatStateManager;
    }

    public void applyBlock(Player player, long durationMs) {
        long until = System.currentTimeMillis() + durationMs;
        combatStateManager.getPlayerState(player.getUniqueId()).setBlockingUntil(until);
    }

    public boolean isBlocking(Player player) {
        return remainingBlockMs(player) > 0;
    }

    public long remainingBlockMs(Player player) {
        long until = combatStateManager.getPlayerState(player.getUniqueId()).getBlockingUntil();
        return Math.max(0L, until - System.currentTimeMillis());
    }
}
