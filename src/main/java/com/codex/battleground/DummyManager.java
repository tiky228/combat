package com.codex.battleground;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class DummyManager {
    private final Plugin plugin;
    private final StunManager stunManager;
    private final NamespacedKey dummyKey;
    private final Map<UUID, DummyInfo> dummies = new HashMap<>();
    private BukkitTask task;
    private boolean indicatorsEnabled;
    private String indicatorBar;
    private double indicatorHeight;

    public DummyManager(Plugin plugin, StunManager stunManager) {
        this.plugin = plugin;
        this.stunManager = stunManager;
        this.dummyKey = new NamespacedKey(plugin, "combat_dummy");
    }

    public void reloadConfig() {
        indicatorsEnabled = plugin.getConfig().getBoolean("dummy.indicator.enabled", true);
        indicatorBar = plugin.getConfig().getString("dummy.indicator.bar", "██████████");
        indicatorHeight = plugin.getConfig().getDouble("dummy.indicator.height", 2.2);
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
        for (DummyInfo info : dummies.values()) {
            removeIndicator(info);
        }
        dummies.clear();
    }

    public LivingEntity spawnDummy(Location location) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setAI(false);
        villager.setCollidable(false);
        villager.setSilent(true);
        villager.setInvulnerable(false);
        villager.customName(Component.text("Combat Dummy", NamedTextColor.GRAY));
        villager.setCustomNameVisible(false);
        markAsDummy(villager);

        DummyInfo info = new DummyInfo(villager);
        if (indicatorsEnabled) {
            info.indicator = spawnIndicator(villager);
        }
        dummies.put(villager.getUniqueId(), info);
        return villager;
    }

    public boolean removeNearest(Player player, double radius) {
        Optional<DummyInfo> nearest = dummies.values().stream()
            .filter(info -> info.entity.isValid())
            .filter(info -> info.entity.getWorld().equals(player.getWorld()))
            .min(Comparator.comparingDouble(info -> info.entity.getLocation().distanceSquared(player.getLocation())));

        if (nearest.isEmpty()) {
            return false;
        }
        DummyInfo info = nearest.get();
        if (info.entity.getLocation().distanceSquared(player.getLocation()) > radius * radius) {
            return false;
        }
        removeDummy(info.entity.getUniqueId());
        return true;
    }

    public boolean isDummy(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        return container.has(dummyKey, PersistentDataType.BYTE);
    }

    public void removeDummy(UUID entityId) {
        DummyInfo info = dummies.remove(entityId);
        if (info != null) {
            removeIndicator(info);
            info.entity.remove();
        }
    }

    public boolean isRegistered(UUID entityId) {
        return dummies.containsKey(entityId);
    }

    private void tick() {
        Iterator<Map.Entry<UUID, DummyInfo>> iterator = dummies.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, DummyInfo> entry = iterator.next();
            DummyInfo info = entry.getValue();
            if (!info.entity.isValid()) {
                removeIndicator(info);
                iterator.remove();
                continue;
            }
            if (info.indicator != null) {
                updateIndicator(info);
            }
        }
    }

    private void updateIndicator(DummyInfo info) {
        boolean stunned = stunManager.isEntityStunned(info.entity);
        TextDisplay indicator = info.indicator;
        if (indicator == null || !indicator.isValid()) {
            return;
        }
        Component text = Component.text(indicatorBar, stunned ? NamedTextColor.RED : NamedTextColor.GREEN);
        indicator.text(text);
        Location target = info.entity.getLocation().add(0, indicatorHeight, 0);
        indicator.teleport(target);
    }

    private TextDisplay spawnIndicator(LivingEntity entity) {
        Location location = entity.getLocation().add(0, indicatorHeight, 0);
        TextDisplay display = (TextDisplay) entity.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        display.setBillboard(Billboard.CENTER);
        display.setSeeThrough(true);
        display.setPersistent(false);
        display.setGravity(false);
        display.setVelocity(new Vector());
        display.text(Component.text(indicatorBar, NamedTextColor.GREEN));
        return display;
    }

    private void removeIndicator(DummyInfo info) {
        if (info.indicator != null && info.indicator.isValid()) {
            info.indicator.remove();
        }
    }

    private void markAsDummy(LivingEntity entity) {
        entity.getPersistentDataContainer().set(dummyKey, PersistentDataType.BYTE, (byte) 1);
    }

    private static class DummyInfo {
        private final LivingEntity entity;
        private TextDisplay indicator;

        private DummyInfo(LivingEntity entity) {
            this.entity = entity;
        }
    }
}
