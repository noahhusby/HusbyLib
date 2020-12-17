package com.noahhusby.lib.data.sql;

public abstract class SQLDatabase implements ISQLDatabase {
    private Credentials credentials;

    public SQLDatabase() {
        credentials = new Credentials();
    }

    public SQLDatabase(Credentials credentials) {
        setCredentials(credentials);
    }

    @Override
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }
}
