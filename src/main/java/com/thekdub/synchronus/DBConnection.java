package com.thekdub.synchronus;

import java.sql.*;

public class DBConnection {
	
	private Connection connection;
	
	private final String host;
	private final int port;
	private final String database;
	private final String username;
	private final String password;
	
	public DBConnection(String host, int port, String database, String username, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}
	
	public DBConnection connect() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			disconnect();
		}
		connection = DriverManager.getConnection(
					String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&characterEncoding=utf8",
								host,
								port,
								database,
								username,
								password));
		return this;
	}
	
	public void disconnect() throws SQLException {
		if (connection == null || connection.isClosed()) {
			return;
		}
		connection.close();
		connection = null;
	}
	
	public boolean isConnected() {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}
	
	public DBConnection prepareStatement(String statement, Argument<?>... args) throws SQLException {
		new Transaction(statement, args).prepare(connection).execute();
		return this;
	}
	
	public ResultSet prepareQuery(String query, Argument<?>... args) throws SQLException {
		return new Transaction(query, args).prepare(connection).executeQuery();
	}
	
	public static class Transaction {
		
		private final String statement;
		private final Argument<?>[] args;
		
		private PreparedStatement ps;
		
		public Transaction(String statement, Argument<?>... args) {
			this.statement = statement;
			this.args = args;
		}
		
		public Transaction prepare(Connection connection) throws SQLException {
			ps = connection.prepareStatement(statement);
			for (int i = 0; i < args.length; i++) {
				args[i].setArgument(ps, i+1);
			}
			return this;
		}
		
		public Transaction execute() throws SQLException {
			ps.execute();
			ps.close();
			return this;
		}
		
		public ResultSet executeQuery() throws SQLException {
			ResultSet rs = ps.executeQuery();
			ps.close();
			return rs;
		}
		
	}
	
	public static class Argument<T> {
		private final T value;
		
		public Argument(T value) {
			this.value = value;
		}
		
		public T getValue() {
			return value;
		}
		
		public void setArgument(PreparedStatement ps, int i) throws SQLException {
			if (value instanceof String) {
				ps.setString(i, (String) value);
			}
			else if (value instanceof Integer) {
				ps.setInt(i, (Integer) value);
			}
			else if (value instanceof Long) {
				ps.setLong(i, (Long) value);
			}
			else if (value instanceof Double) {
				ps.setDouble(i, (Double) value);
			}
			else if (value instanceof Float) {
				ps.setFloat(i, (Float) value);
			}
			else if (value instanceof Boolean) {
				ps.setBoolean(i, (Boolean) value);
			}
			else {
				ps.setObject(i, value);
			}
		}
	}
	
	
}
