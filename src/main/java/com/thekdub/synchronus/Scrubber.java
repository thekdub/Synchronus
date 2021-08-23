package com.thekdub.synchronus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sqlite.JDBC;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Scrubber implements Runnable {
	
	private final File path;
	private final ConfigurationSection section;
	
	private DBConnection connection;
	
	public Scrubber(String path) throws Exception {
		// Check for entry in plugin configuration
		if (!Synchronus.getInstance().getConfig().contains("target-directories." + path)) {
			throw new Exception("Path is not configured!");
		}
		
		// Check for directory
		File file = new File(path);
		if (!file.isDirectory()) {
			throw new IOException("Path is not a directory!");
		}
		
		// Set instance variables
		section = Synchronus.getInstance().getConfig().getConfigurationSection("target-directories." + path);
		this.path = file;
	}
	
	private void connect() throws SQLException {
		// Connect to database
		FileConfiguration sCfg = Synchronus.getInstance().getConfig();
		connection = new DBConnection(
					sCfg.getString("database.mysql-host", "localhost"),
					sCfg.getInt("database.mysql-port", 3306),
					sCfg.getString("database.mysql-database", "synchronus"),
					sCfg.getString("database.mysql-username", "root"),
					sCfg.getString("database.mysql-password", ""));
		connection.connect();
	}
	
	private void disconnect() throws SQLException {
		connection.disconnect();
		connection = null;
	}
	
	@Override
	public void run() {
		if (connection == null || !connection.isConnected()) {
			try {
				connect();
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
		
		StringBuilder builder = new StringBuilder(
					String.format("REPLACE INTO %s (filename", section.getString("table-name")));
		Objects.requireNonNull(section.getConfigurationSection("table-columns")).getKeys(false)
					.forEach((col) -> builder.append(",").append(col));
		builder.append(") VALUES (").append("?");
		Objects.requireNonNull(section.getConfigurationSection("table-columns")).getKeys(false)
					.forEach((col) -> builder.append(",").append("?"));
		builder.append(");");
		String statement = builder.toString();
		
		for (File file : path.listFiles((File f, String name) -> name.toLowerCase().endsWith(".yml"))) {
			YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
			
			List<DBConnection.Argument<?>> arguments = new ArrayList<>();
			
			arguments.add(new DBConnection.Argument<String>(file.getName().replace(".yml", "")));
			
			for (String key : Objects.requireNonNull(section.getConfigurationSection("table-columns"),
						"Section " + path.toURI() + " is improperly configured! Missing 'table-columns'")
						.getKeys(false)) {
				DBConnection.Argument<?> argument;
				
				argument = getArgument(yml, key);
				if (argument == null) {
					argument = getArgument(section, "table-columns." + key);
				}
				if (argument == null) {
					argument = new DBConnection.Argument<Integer>(0);
				}
				
				arguments.add(argument);
			}
			
			DBConnection.Argument<?>[] args = arguments.toArray(new DBConnection.Argument<?>[0]);
			
			try {
				connection.prepareStatement(statement, args);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		try {
			disconnect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private DBConnection.Argument<?> getArgument(ConfigurationSection config, String key) {
		if (!config.contains(key)) {
			return null;
		}
		if (config.isConfigurationSection(key)) {
			return new DBConnection.Argument<String>(sectionToJSON(config.getConfigurationSection(key)));
		}
		else if (config.isInt(key)) {
			return new DBConnection.Argument<Integer>(config.getInt(key));
		}
		else if (config.isLong(key)) {
			return new DBConnection.Argument<Long>(config.getLong(key));
		}
		else if (config.isDouble(key)) {
			return new DBConnection.Argument<Double>(config.getDouble(key));
		}
		else if (config.isBoolean(key)) {
			return new DBConnection.Argument<Boolean>(config.getBoolean(key));
		}
		else if (config.isList(key)) {
			return new DBConnection.Argument<String>(listToJSON(config.getStringList(key)));
		}
		else {
			return new DBConnection.Argument<String>(config.getString(key));
		}
	}
	
	private String sectionToJSON(ConfigurationSection section) {
		if (section == null) {
			return "{}";
		}
		StringBuilder output = new StringBuilder("{");
		
		boolean first = true;
		
		for (String key : section.getKeys(false)) {
			
			if (!first) {
				output.append(",");
			}
			
			output.append(key).append(":");
			
			if (section.isConfigurationSection(key)) {
				output.append(sectionToJSON(section.getConfigurationSection(key)));
			}
			else if (section.isList(key)) {
				output.append(listToJSON(section.getStringList(key)));
			}
			else {
				output.append(section.getString(key));
			}
			
			first = false;
		}
		
		output.append("}");
		
		return output.toString();
	}
	
	private String listToJSON(List<String> list) {
		StringBuilder output = new StringBuilder();
		output.append("[");
		boolean subFirst = true;
		
		for (String entry : list) {
			if (!subFirst) {
				output.append(",");
			}
			output.append(entry);
			subFirst = false;
		}
		
		output.append("]");
		
		return output.toString();
	}
	
}
