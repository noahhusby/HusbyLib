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

public class Update implements Query {
    private String table = "";
    private UpdateValue value = null;
    private String filter = "";

    public Update() {
    }

    public Update(String table, UpdateValue value, String filter) {
        this.table = table;
        this.value = value;
        this.filter = filter;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public UpdateValue getValue() {
        return value;
    }

    public void setValue(UpdateValue value) {
        this.value = value;
    }

    @Override
    public String query() {
        StringBuilder change = new StringBuilder();
        int i = 1;
        for (String key : value.getKeys()) {
            change.append(key).append(" = '").append(value.get(key)).append("'");
            if (i != value.getKeys().size()) {
                change.append(", ");
            }
            i++;
        }

        return String.format("UPDATE %s SET %s WHERE %s;", table, change, filter);
    }
}
