package com.thekdub.synchronus;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Synchronus extends JavaPlugin {
	
	private static Synchronus instance;
	
	private static ScheduledExecutorService scrubExecutor;
	
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		reloadConfig();
		
		scrubExecutor = Executors.newScheduledThreadPool(1);
		if (getConfig().contains("target-directories")) {
			for (String directory : getConfig().getConfigurationSection("target-directories").getKeys(false)) {
				try {
					scrubExecutor.scheduleAtFixedRate(
								new Scrubber(directory),
								getConfig().getLong("scrub-delay", 30),
								getConfig().getLong("scrub-frequency", 300),
								TimeUnit.SECONDS);
					getServer().getLogger().log(Level.INFO, String.format("[Synchronus] Registered target '%s' for scrubbing.",
								directory));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void onDisable() {
		if (scrubExecutor != null) {
			scrubExecutor.shutdownNow();
			scrubExecutor = null;
		}
	}
	
	public static Synchronus getInstance() {
		return instance;
	}
}