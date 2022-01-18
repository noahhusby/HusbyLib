/*
 * MIT License
 *
 * Copyright 2020-2021 noahhusby
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.noahhusby.lib.data.sql.actions;

import java.util.List;

public class Insert implements Query {
    private String table = "";
    private String columns = "";
    private String[] data = null;

    public Insert() {
    }

    public Insert(String table, String columns, String... data) {
        this.table = table;
        this.columns = columns;
        this.data = data;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getTable() {
        return
                table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String[] getData() {
        return data;
    }

    public void setData(String... data) {
        this.data = data;
    }

    public void setData(List<String> data) {
        this.data = (String[]) data.toArray();
    }

    @Override
    public String query() {
        StringBuilder sqlData = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sqlData.append("'").append(data[i]).append("'");
            if (i != data.length - 1) {
                sqlData.append(", ");
            }
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s);", table, columns, sqlData);
    }
}
