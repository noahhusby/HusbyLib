package com.noahhusby.lib.data.sql.actions;

import java.util.ArrayList;
import java.util.List;

public class Result {
    private List<Row> rows = new ArrayList<>();

    public void addRow(Row row) {
        rows.add(row);
    }

    public List<Row> getRows() {
        return rows;
    }
}
