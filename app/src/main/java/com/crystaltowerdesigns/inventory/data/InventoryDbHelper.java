/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.crystaltowerdesigns.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.crystaltowerdesigns.inventory.data.InventoryContract.InventoryEntry;

/**
 * Database helper for inventory.
 * Handles database creation and version control.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory7.db";

    /**
     * Database version. Increment with each new version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     *
     * @param context app context
     */
    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Database creation the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL);";

//        db.execSQL("CREATE TABLE test (_ID INTEGER PRIMARY KEY AUTOINCREMENT, person TEXT);");
        // Execute the SQL statement
            db.execSQL(SQL_CREATE_INVENTORY_TABLE);
            Log.v("OUCH", SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * Called when a database upgrade is needed.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Database version 1 requires nothing to do here.
    }
}