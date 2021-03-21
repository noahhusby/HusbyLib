package com.noahhusby.lib.data.sql;

import com.zaxxer.hikari.HikariConfig;

public class Credentials {
    private String host = "";
    private int port = 3306;
    private String user = "";
    private String password = "";
    private String database = "";

    public Credentials() {
    }

    public Credentials(String host, int port, String user, String password, String database) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.user = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public HikariConfig toHikariConfig(String base) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("%s%s:%s/%s", base, host, port, database));
        config.setUsername(user);
        config.setPassword(password);
        return config;
    }
}
