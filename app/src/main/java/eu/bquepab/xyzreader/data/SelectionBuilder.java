/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 * -Imported from AOSP frameworks/base/core/java/com/android/internal/content
 * -Changed package name
 */

package eu.bquepab.xyzreader.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Helper for building selection clauses for {@link SQLiteDatabase}. Each
 * appended clause is combined using {@code AND}. This class is <em>not</em>
 * thread safe.
 */
public class SelectionBuilder {
    private String table = null;
    private HashMap<String, String> projectionMap;
    private StringBuilder selection;
    private ArrayList<String> selectionArgs;

    /**
     * Reset any internal state, allowing this builder to be recycled.
     */
    public SelectionBuilder reset() {
        table = null;
        if (projectionMap != null) {
            projectionMap.clear();
        }
        if (selection != null) {
            selection.setLength(0);
        }
        if (selectionArgs != null) {
            selectionArgs.clear();
        }
        return this;
    }

    /**
     * Append the given selection clause to the internal state. Each clause is
     * surrounded with parenthesis and combined using {@code AND}.
     */
    public SelectionBuilder where(String selection, String... selectionArgs) {
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments=");
            }

            // Shortcut when clause is empty
            return this;
        }

        ensureSelection(selection.length());
        if (this.selection.length() > 0) {
            this.selection.append(" AND ");
        }

        this.selection.append("(")
                      .append(selection)
                      .append(")");
        if (selectionArgs != null) {
        	ensureSelectionArgs();
            for (String arg : selectionArgs) {
                this.selectionArgs.add(arg);
            }
        }

        return this;
    }

    public SelectionBuilder table(String table) {
        this.table = table;
        return this;
    }

    private void assertTable() {
        if (table == null) {
            throw new IllegalStateException("Table not specified");
        }
    }

    private void ensureProjectionMap() {
        if (projectionMap == null) {
            projectionMap = new HashMap<>();
        }
    }

    private void ensureSelection(int lengthHint) {
        if (selection == null) {
            selection = new StringBuilder(lengthHint + 8);
        }
    }

    private void ensureSelectionArgs() {
        if (selectionArgs == null) {
            selectionArgs = new ArrayList<>();
        }
    }

    public SelectionBuilder mapToTable(String column, String table) {
    	ensureProjectionMap();
        projectionMap.put(column, table + "." + column);
        return this;
    }

    public SelectionBuilder map(String fromColumn, String toClause) {
    	ensureProjectionMap();
        projectionMap.put(fromColumn, toClause + " AS " + fromColumn);
        return this;
    }

    /**
     * Return selection string for current internal state.
     *
     * @see #getSelectionArgs()
     */
    public String getSelection() {
        if (selection != null) {
            return selection.toString();
        } else {
            return null;
    	}
    }

    /**
     * Return selection arguments for current internal state.
     *
     * @see #getSelection()
     */
    public String[] getSelectionArgs() {
        if (selectionArgs != null) {
            return selectionArgs.toArray(new String[selectionArgs.size()]);
        } else {
            return null;
    	}
    }

    private void mapColumns(String[] columns) {
        if (projectionMap == null) {
            return;
        }
        for (int i = 0; i < columns.length; i++) {
            final String target = projectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
        }
    }

    @Override
    public String toString() {
        return "SelectionBuilder[table=" + table + ", selection=" + getSelection() + ", selectionArgs=" + Arrays.toString(getSelectionArgs()) + "]";
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
        return query(db, columns, null, null, orderBy, null);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(SQLiteDatabase db, String[] columns, String groupBy,
            String having, String orderBy, String limit) {
        assertTable();
        if (columns != null) mapColumns(columns);
        return db.query(table, columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy, limit);
    }

    /**
     * Execute update using the current internal state as {@code WHERE} clause.
     */
    public int update(SQLiteDatabase db, ContentValues values) {
        assertTable();
        return db.update(table, values, getSelection(), getSelectionArgs());
    }

    /**
     * Execute delete using the current internal state as {@code WHERE} clause.
     */
    public int delete(SQLiteDatabase db) {
        assertTable();
        return db.delete(table, getSelection(), getSelectionArgs());
    }
}
