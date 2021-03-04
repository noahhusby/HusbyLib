package com.noahhusby.lib.data.sql.actions;

public class Select implements Query {
    private String table = "";
    private String columns = "";
    private String filter = "";

    public Select() {
    }

    public Select(String table, String columns, String filter) {
        this.table = table;
        this.columns = columns;
        this.filter = filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getFilter() {
        return filter;
    }

    public String getTable() {
        return table;
    }

    public String getColumns() {
        if (columns == null) {
            return "*";
        } else {
            return columns;
        }
    }


    @Override
    public String query() {
        if (columns == null || columns.equals("")) {
            columns = "*";
        }

        String sql = String.format("SELECT %s FROM %s", columns, table);
        if (filter != null && !filter.equals("")) {
            sql += " WHERE " + filter;
        }

        return sql + ";";
    }
}
