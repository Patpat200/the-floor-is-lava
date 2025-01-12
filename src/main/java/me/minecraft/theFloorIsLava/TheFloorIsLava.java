package me.minecraft.theFloorIsLava;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class TheFloorIsLava extends JavaPlugin {
    private int currentY = 1;
    private int taskId = -1;
    private int speed = 1;
    private final int BORDER_SIZE = 200;

    @Override
    public void onEnable() {
        getLogger().info("----------------------------------------");
        getLogger().info("The Floor Is Lava - Version 1.0");
        getLogger().info("Plugin created for Spigot 1.21.4");
        getLogger().info("----------------------------------------");
        getLogger().info("Initializing commands...");

        this.getCommand("lavaspeed").setExecutor(this);

        getLogger().info("Command 'lavaspeed' registered successfully!");
        getLogger().info("Plugin is ready to use!");
        getLogger().info("Use /lavaspeed <seconds> start to begin");
        getLogger().info("----------------------------------------");
    }

    private void setupWorldBorders() {
        for (World world : Bukkit.getWorlds()) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(BORDER_SIZE);
            border.setWarningDistance(5);
            border.setWarningTime(3);
            border.setDamageBuffer(0);
            border.setDamageAmount(1);
        }
    }

    private void removeWorldBorders() {
        for (World world : Bukkit.getWorlds()) {
            WorldBorder border = world.getWorldBorder();
            border.reset();
        }
        getLogger().info("World borders removed!");
    }

    @Override
    public void onDisable() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        removeWorldBorders();
        getLogger().info("----------------------------------------");
        getLogger().info("The Floor Is Lava - Shutting down");
        getLogger().info("Plugin disabled successfully!");
        getLogger().info("----------------------------------------");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lavaspeed")) {
            if (args.length < 1) {
                sender.sendMessage("§c[The Floor Is Lava] Usage: /lavaspeed <seconds> [start|stop]");
                return true;
            }

            try {
                speed = Integer.parseInt(args[0]);

                if (speed < 1) {
                    sender.sendMessage("§c[The Floor Is Lava] Speed must be greater than 0!");
                    return true;
                }

                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("start")) {
                        setupWorldBorders();
                        startLavaRise();
                        sender.sendMessage("§a[The Floor Is Lava] Lava rise started with speed of " + speed + " seconds!");
                        sender.sendMessage("§a[The Floor Is Lava] Play area limited to " + BORDER_SIZE + "x" + BORDER_SIZE + " blocks!");
                    } else if (args[1].equalsIgnoreCase("stop")) {
                        stopLavaRise();
                        removeWorldBorders();
                        sender.sendMessage("§a[The Floor Is Lava] Lava rise has been stopped!");
                    }
                } else {
                    sender.sendMessage("§a[The Floor Is Lava] Lava speed set to " + speed + " seconds");
                }

                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("§c[The Floor Is Lava] Speed must be a number!");
                return true;
            }
        }
        return false;
    }

    private void startLavaRise() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        currentY = 1;
        getLogger().info("Starting lava rise from Y=" + currentY);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentY >= 256) {
                    this.cancel();
                    stopLavaRise();
                    removeWorldBorders();
                    getLogger().info("Maximum height reached - Stopping lava rise");
                    return;
                }

                for (World world : Bukkit.getWorlds()) {
                    int borderSize = BORDER_SIZE / 2;
                    for (int x = -borderSize; x < borderSize; x++) {
                        for (int z = -borderSize; z < borderSize; z++) {
                            if (world.getBlockAt(x, currentY, z).getType() == Material.AIR) {
                                world.getBlockAt(x, currentY, z).setType(Material.LAVA);
                            }
                        }
                    }
                }

                getLogger().info("Lava generated at height Y=" + currentY);
                currentY++;
            }
        };

        taskId = task.runTaskTimer(this, 0L, speed * 20L).getTaskId();
    }

    private void stopLavaRise() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
            getLogger().info("Lava rise task stopped");
        }
    }
}