package com.noahhusby.lib.data.sql;

import com.noahhusby.lib.data.sql.actions.Query;
import com.noahhusby.lib.data.sql.actions.Result;
import com.noahhusby.lib.data.sql.actions.Select;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

public interface ISQLDatabase {
    void setCredentials(Credentials credentials);

    Credentials getCredentials();

    Connection getConnection();

    boolean connect();

    boolean isConnected();

    boolean close();

    boolean execute(Query query);

    Result select(Select select);

    CompletableFuture<Result> asyncSelect(Select select);
}
