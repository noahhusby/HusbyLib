package com.noahhusby.lib.data.sql.actions;

import java.util.List;

public class Insert implements Query {
    private String table = "";
    private String columns = "";
    private String[] data = null;

    public Insert() {}

    public Insert(String table, String columns, String... data) {
        this.table = table;
        this.columns = columns;
        this.data = data;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setData(String... data) {
        this.data = data;
    }

    public void setData(List<String> data) {
        this.data = (String[]) data.toArray();
    }

    public String getColumns() {
        return columns;
    }

    public String getTable() {
        return
                table;
    }

    public String[] getData() {
        return data;
    }

    @Override
    public String query() {
        StringBuilder sqlData = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sqlData.append("'").append(data[i]).append("'");
            if(i != data.length - 1) {
                sqlData.append(", ");
            }
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s);", table, columns, sqlData);
    }
}
