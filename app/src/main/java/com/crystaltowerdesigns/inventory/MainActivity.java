package com.crystaltowerdesigns.inventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.crystaltowerdesigns.inventory.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add our toolbar
        Toolbar myToolbar = findViewById(R.id.inventory_app_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER };

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to access the inventory data.
        Cursor cursor = getContentResolver().query(
                InventoryEntry.CONTENT_URI,   // The content URI of the words table
                projection,             // The columns to return for each row
                null,                   // Selection criteria
                null,                   // Selection criteria
                null);                  // The sort order for the returned rows

        TextView displayView = (TextView) findViewById(R.id.database_summary);

        try {
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column.
            displayView.setText("The inventory table contains " + cursor.getCount() + " items.\n\n");
            displayView.append(InventoryEntry._ID + " - " +
                    InventoryEntry.COLUMN_ITEM_NAME + " - " +
                    InventoryEntry.COLUMN_ITEM_PRICE + " - " +
                    InventoryEntry.COLUMN_ITEM_QUANTITY + " - " +
                    InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME + " - " +
                    InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER+ "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                Float currentPrice= cursor.getFloat(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplierName + " - " +
                        currentSupplierPhone));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    /**
     * Helper method to insert hardcoded item data into the database. For debugging purposes only.
     */
    private void insertItem() {
        // Create a ContentValues object where column names are the keys.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, "Widget One");
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, 8.42);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, 3);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, "Hansen Supplies");
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, "715-555-1212");
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        ContentValues values2 = new ContentValues();
        values2.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, "Widget Two");
        values2.put(InventoryEntry.COLUMN_ITEM_PRICE, 10.99);
        values2.put(InventoryEntry.COLUMN_ITEM_QUANTITY, 7);
        values2.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, "Peterson Warehouse");
        values2.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, "906-555-1212");
        Uri newUri2 = getContentResolver().insert(InventoryEntry.CONTENT_URI, values2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_add_dummy:
                insertItem();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            //case R.id.action_delete_all_entries:
                // Do nothing for now
              //  return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
