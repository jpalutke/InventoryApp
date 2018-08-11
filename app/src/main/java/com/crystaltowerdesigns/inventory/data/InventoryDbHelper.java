package com.crystaltowerdesigns.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.crystaltowerdesigns.inventory.data.InventoryContract.InventoryEntry;

/**
 * Database helper for inventory.
 * Handles database creation and version control.
 */
public class InventoryDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

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
            db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * Called when a database upgrade is needed.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Database version 1 requires nothing to do here.
    }

    public static String doubleQuote(String stringToPrepare) {
        String workingString = stringToPrepare;
        workingString = workingString.replace("'", "''");
        workingString = workingString.replace("\"", "\"\"");
        return workingString;
    }
}