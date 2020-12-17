package com.noahhusby.lib.data.sql.actions;

public class Custom implements Query {
    private final String query;

    public Custom(String query) {
        this.query = query;
    }

    @Override
    public String query() {
        return query;
    }
}
