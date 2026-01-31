package com.codex.battleground;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatPlugin extends JavaPlugin {
    private CombatStateManager combatStateManager;
    private StunManager stunManager;
    private BlockManager blockManager;
    private HUDService hudService;
    private DummyManager dummyManager;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
            .verboseOutput(false)
            .silentLogs(true));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!ensureHardDependency("PacketEvents") || !ensureHardDependency("CommandAPI")) {
            getLogger().severe("Missing hard dependencies. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean nbtApiPresent = isPluginEnabled("NBTAPI");
        boolean guiEnginePresent = isPluginEnabled("GuiEngine");
        getLogger().info("Optional dependencies detected: NBTAPI=" + nbtApiPresent + ", GuiEngine=" + guiEnginePresent);

        combatStateManager = new CombatStateManager();
        stunManager = new StunManager(combatStateManager);
        blockManager = new BlockManager(combatStateManager);
        hudService = new HUDService(this, blockManager, stunManager);
        dummyManager = new DummyManager(this, stunManager);
        dummyManager.reloadConfig();

        CommandAPI.onEnable();
        registerCommands();

        Bukkit.getPluginManager().registerEvents(new CombatListener(this, blockManager, stunManager, dummyManager), this);

        hudService.start();
        dummyManager.start();

        getLogger().info("BattlegroundCombat enabled.");
    }

    @Override
    public void onDisable() {
        if (hudService != null) {
            hudService.stop();
        }
        if (dummyManager != null) {
            dummyManager.stop();
        }
        CommandAPI.onDisable();
        getLogger().info("BattlegroundCombat disabled.");
    }

    private boolean ensureHardDependency(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null || !plugin.isEnabled()) {
            getLogger().severe("Missing required plugin: " + pluginName);
            return false;
        }
        return true;
    }

    private boolean isPluginEnabled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    private void registerCommands() {
        new CommandAPICommand("combatdummy")
            .withSubcommand(new CommandAPICommand("spawn")
                .executesPlayer((player, args) -> {
                    dummyManager.spawnDummy(player.getLocation());
                    player.sendMessage(Component.text("Combat dummy spawned.", NamedTextColor.GREEN));
                }))
            .withSubcommand(new CommandAPICommand("removeNearest")
                .executesPlayer((player, args) -> {
                    boolean removed = dummyManager.removeNearest(player, 10.0);
                    if (removed) {
                        player.sendMessage(Component.text("Nearest dummy removed.", NamedTextColor.YELLOW));
                    } else {
                        player.sendMessage(Component.text("No dummy found within range.", NamedTextColor.RED));
                    }
                }))
            .register();

        new CommandAPICommand("combatdebug")
            .executesPlayer((player, args) -> {
                long blockingMs = blockManager.remainingBlockMs(player);
                long stunnedMs = stunManager.remainingPlayerStunMs(player);
                player.sendMessage(Component.text("Blocking: " + blockingMs + "ms", NamedTextColor.GOLD));
                player.sendMessage(Component.text("Stunned: " + stunnedMs + "ms", NamedTextColor.RED));
                player.sendMessage(Component.text("IsBlocking: " + blockManager.isBlocking(player), NamedTextColor.GRAY));
                player.sendMessage(Component.text("IsStunned: " + stunManager.isPlayerStunned(player), NamedTextColor.GRAY));
            })
            .register();
    }
}
