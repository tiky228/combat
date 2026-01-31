package com.codex.battleground;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
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
    public void onEnable() {
        saveDefaultConfig();

        boolean nbtApiPresent = isPluginEnabled("NBTAPI");
        boolean guiEnginePresent = isPluginEnabled("GuiEngine");
        boolean packetEventsPresent = isPluginEnabled("PacketEvents");
        getLogger().info("Optional dependencies detected: NBTAPI=" + nbtApiPresent + ", GuiEngine=" + guiEnginePresent);
        if (packetEventsPresent) {
            getLogger().info("Optional dependencies detected: PacketEvents=true");
        }

        combatStateManager = new CombatStateManager();
        stunManager = new StunManager(combatStateManager);
        blockManager = new BlockManager(combatStateManager);
        hudService = new HUDService(this, blockManager, stunManager);
        dummyManager = new DummyManager(this, stunManager);
        dummyManager.reloadConfig();

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
        getLogger().info("BattlegroundCombat disabled.");
    }

    private boolean isPluginEnabled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    private void registerCommands() {
        registerCommandExecutor("combatdummy");
        registerCommandExecutor("combatdebug");
        registerCommandExecutor("combatreload");
    }

    private void registerCommandExecutor(String commandName) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().warning("Command not registered in plugin.yml: " + commandName);
            return;
        }
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        return switch (commandName) {
            case "combatdummy" -> handleCombatDummy(sender, args);
            case "combatdebug" -> handleCombatDebug(sender);
            case "combatreload" -> handleCombatReload(sender);
            default -> false;
        };
    }

    private boolean handleCombatDummy(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "battleground.combatdummy")) {
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use /combatdummy.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /combatdummy <spawn|removeNearest>", NamedTextColor.YELLOW));
            return true;
        }
        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "spawn" -> {
                if (!hasPermission(sender, "battleground.combatdummy.spawn")) {
                    return true;
                }
                dummyManager.spawnDummy(player.getLocation());
                player.sendMessage(Component.text("Combat dummy spawned.", NamedTextColor.GREEN));
            }
            case "removenearest" -> {
                if (!hasPermission(sender, "battleground.combatdummy.remove")) {
                    return true;
                }
                boolean removed = dummyManager.removeNearest(player, 10.0);
                if (removed) {
                    player.sendMessage(Component.text("Nearest dummy removed.", NamedTextColor.YELLOW));
                } else {
                    player.sendMessage(Component.text("No dummy found within range.", NamedTextColor.RED));
                }
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use /combatdummy <spawn|removeNearest>.", NamedTextColor.YELLOW));
        }
        return true;
    }

    private boolean handleCombatDebug(CommandSender sender) {
        if (!hasPermission(sender, "battleground.combatdebug")) {
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use /combatdebug.", NamedTextColor.RED));
            return true;
        }
        long blockingMs = blockManager.remainingBlockMs(player);
        long stunnedMs = stunManager.remainingPlayerStunMs(player);
        player.sendMessage(Component.text("Blocking: " + blockingMs + "ms", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Stunned: " + stunnedMs + "ms", NamedTextColor.RED));
        player.sendMessage(Component.text("IsBlocking: " + blockManager.isBlocking(player), NamedTextColor.GRAY));
        player.sendMessage(Component.text("IsStunned: " + stunManager.isPlayerStunned(player), NamedTextColor.GRAY));
        return true;
    }

    private boolean handleCombatReload(CommandSender sender) {
        if (!hasPermission(sender, "battleground.combatreload")) {
            return true;
        }
        reloadConfig();
        dummyManager.reloadConfig();
        sender.sendMessage(Component.text("BattlegroundCombat configuration reloaded.", NamedTextColor.GREEN));
        return true;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
        return false;
    }
}
