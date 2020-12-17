package com.noahhusby.lib.data.sql;

import com.noahhusby.lib.data.sql.actions.*;

import java.sql.Connection;

public interface ISQLDatabase {
    void setCredentials(Credentials credentials);
    Credentials getCredentials();

    Connection getConnection();

    boolean connect();
    boolean isConnected();
    boolean close();

    boolean execute(Query query);
    Result select(Select select);
}
