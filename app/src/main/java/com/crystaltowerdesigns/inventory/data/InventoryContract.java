package com.crystaltowerdesigns.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public final class InventoryContract {

    // Empty constructor to prevent accidentally instantiating the contract class
    private InventoryContract() {}

    /**
     * "CONTENT_AUTHORITY" using the package name for the app, it is guaranteed to be unique on the device.
     */
    public static final String CONTENT_AUTHORITY = "com.crystaltowerdesigns.inventory";

    /**
     * CONTENT_AUTHORITY is used for the base URI's to contact the content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.crystaltowerdesigns.inventory/ is a valid path for
     * looking at inventory data.
     */
    public static final String PATH_INVENTORY = "inventory";

    /**
     * {@link BaseColumns}
     * Inner class that defines constant values for the inventory table.
     * Each entry in the table represents a single inventory item.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * Name of database table for inventory items
         */
        public final static String TABLE_NAME = "inventorytable";

        /**
         * Unique ID number for the inventory item (only use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_NAME = "name";

        /**
         * Price of the item.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_ITEM_PRICE = "price";

        /**
         * Quantity of the item.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_QUANTITY = "quantity";

        /**
         * Name of the supplier of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_SUPPLIER_NAME = "suppliername";

        /**
         * Phone Number of the supplier of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_SUPPLIER_PHONE_NUMBER = "supplierphone";
    }
}

