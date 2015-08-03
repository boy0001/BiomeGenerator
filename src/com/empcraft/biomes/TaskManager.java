package com.empcraft.biomes;

import java.util.HashMap;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;

public class TaskManager {
    public static int taskRepeat(final Runnable r, final int interval) {
        return Main.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, r, interval, interval);
    }

    public static MutableInt index = new MutableInt(0);
    public static HashMap<Integer, Integer> tasks = new HashMap<>();

    public static void taskAsync(final Runnable r) {
        if (r == null) {
            return;
        }
        Main.plugin.getServer().getScheduler().runTaskAsynchronously(Main.plugin, r).getTaskId();
    }

    public static void task(final Runnable r) {
        if (r == null) {
            return;
        }
        Main.plugin.getServer().getScheduler().runTask(Main.plugin, r).getTaskId();
    }

    public static void taskLater(final Runnable r, final int delay) {
        if (r == null) {
            return;
        }
        Main.plugin.getServer().getScheduler().runTaskLater(Main.plugin, r, delay).getTaskId();
    }

    public static void taskLaterAsync(final Runnable r, final int delay) {
        Main.plugin.getServer().getScheduler().runTaskLaterAsynchronously(Main.plugin, r, delay);
    }

    public static void cancelTask(final int task) {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }
}
