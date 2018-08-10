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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.crystaltowerdesigns.inventory.data.InventoryContract.InventoryEntry;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class InventoryProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the inventory table */
    private static final int ITEMS = 100;

    /** URI matcher code for the content URI for a single item in the inventory table */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.crystaltowerdesigns.inventory/inventory" will map to the
        // integer code {@link #INVENTORY}. This URI is used to provide access to MULTIPLE rows
        // of the inventory table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEMS);

        // The content URI of the form "content://com.crystaltowerdesigns.inventory/inventory/#" will map to the
        // integer code {@link #INVENTORY_ID}. This URI is used to provide access to ONE single row
        // of the inventory table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.crystaltowerdesigns.inventory/inventory/3" matches, but
        // "content://com.crystaltowerdesigns.inventory/inventory" (without a number at the end) doesn't match.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    /** Database helper object */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // For the ITEMS code, query the inventory table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inventory table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.crystaltowerdesigns.inventory/inventory/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the inventory table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert an inventory item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // Check that the item name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null || (name.length()==0)) {
            throw new IllegalArgumentException("Item requires a name");
        }

        // Check that the price is valid
        Float price = values.getAsFloat(InventoryEntry.COLUMN_ITEM_PRICE);
        if (price != null && price < 0.00) {
            throw new IllegalArgumentException("Item requires a non-negative price");
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Item requires a non-negative quantity");
        }

        // Check that the supplier name is not null
        String supplierName = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME);
        if (supplierName == null || (supplierName.length()==0)) {
            throw new IllegalArgumentException("Item requires a supplier name");
        }

        // Check that the supplier phone is not null
        String supplierPhone = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME);
        if (supplierPhone == null || (supplierPhone.length()==0)) {
            throw new IllegalArgumentException("Item requires a supplier phone number");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new inventory item with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update inventory items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more inventory items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link InventoryEntry#COLUMN_ITEM_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_PRICE)) {
            Float price = values.getAsFloat(InventoryEntry.COLUMN_ITEM_PRICE);
            if (price == null || price < 0.00) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires valid quantity");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_SUPPLIER_NAME} key is present,
        // check that the name value is not null or blank.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME);
            if (supplierName == null || supplierName.length()==0) {
                throw new IllegalArgumentException("Item requires a supplier name");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_SUPPLIER_PHONE_NUMBER} key is present,
        // check that the phone value is not null or blank.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER)) {
            String supplierPhone = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER);
            if (supplierPhone == null || supplierPhone.length()==0) {
                throw new IllegalArgumentException("Item requires a supplier phone number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        return database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                return database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
