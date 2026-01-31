package com.codex.battleground;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class CombatListener implements Listener {
    private final BlockManager blockManager;
    private final StunManager stunManager;
    private final DummyManager dummyManager;
    private final long blockDurationMs;
    private final long stunDurationMs;
    private final double blockDamageMultiplier;
    private final boolean cancelRealDamage;

    public CombatListener(CombatPlugin plugin,
                          BlockManager blockManager,
                          StunManager stunManager,
                          DummyManager dummyManager) {
        this.blockManager = blockManager;
        this.stunManager = stunManager;
        this.dummyManager = dummyManager;
        this.blockDurationMs = plugin.getConfig().getLong("block.duration_ms", 900L);
        this.stunDurationMs = plugin.getConfig().getLong("stun.duration_ms", 1200L);
        this.blockDamageMultiplier = plugin.getConfig().getDouble("block.damage_multiplier", 0.3);
        this.cancelRealDamage = plugin.getConfig().getBoolean("debug.cancel_real_damage", true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
        blockManager.applyBlock(event.getPlayer(), blockDurationMs);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (stunManager.isPlayerStunned(player)) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (dummyManager.isDummy(target)) {
            stunManager.applyStun(target, stunDurationMs);
            if (cancelRealDamage) {
                event.setCancelled(true);
            } else {
                event.setDamage(0.0);
            }
        }
        if (target instanceof Player targetPlayer && blockManager.isBlocking(targetPlayer)) {
            event.setDamage(event.getDamage() * blockDamageMultiplier);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!stunManager.isPlayerStunned(player)) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to != null && from.distanceSquared(to) > 0.001) {
            event.setTo(from);
        }
    }
}
