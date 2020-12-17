package com.noahhusby.lib.data.sql.actions;

public class Update implements Query {
    private String table = "";
    private UpdateValue value = null;
    private String filter = "";

    public Update() {}

    public Update(String table, UpdateValue value, String filter) {
        this.table = table;
        this.value = value;
        this.filter = filter;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setValue(UpdateValue value) {
        this.value = value;
    }

    public String getTable() {
        return table;
    }

    public String getFilter() {
        return filter;
    }

    public UpdateValue getValue() {
        return value;
    }

    @Override
    public String query() {
        StringBuilder change = new StringBuilder();
        int i = 1;
        for(String key : value.getKeys()) {
            change.append(key).append(" = '").append(value.get(key)).append("'");
            if(i != value.getKeys().size()) change.append(", ");
            i++;
        }

        return String.format("UPDATE %s SET %s WHERE %s;", table, change, filter);
    }
}
