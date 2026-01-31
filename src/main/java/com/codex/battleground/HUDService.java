package com.codex.battleground;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class HUDService {
    private final Plugin plugin;
    private final BlockManager blockManager;
    private final StunManager stunManager;
    private BukkitTask task;

    public HUDService(Plugin plugin, BlockManager blockManager, StunManager stunManager) {
        this.plugin = plugin;
        this.blockManager = blockManager;
        this.stunManager = stunManager;
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 2L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            long stunMs = stunManager.remainingPlayerStunMs(player);
            if (stunMs > 0) {
                player.sendActionBar(Component.text("STUNNED (" + formatSeconds(stunMs) + ")", NamedTextColor.RED));
                continue;
            }
            long blockMs = blockManager.remainingBlockMs(player);
            if (blockMs > 0) {
                player.sendActionBar(Component.text("BLOCKING (" + formatSeconds(blockMs) + ")", NamedTextColor.GOLD));
            }
        }
    }

    private String formatSeconds(long millis) {
        double seconds = millis / 1000.0;
        return String.format("%.1fs", seconds);
    }
}
