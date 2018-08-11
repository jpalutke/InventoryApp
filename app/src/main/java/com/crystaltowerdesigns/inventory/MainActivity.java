
/**
 * Created by Jeff Palutke on 8/10/2018
 */


package com.crystaltowerdesigns.inventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.crystaltowerdesigns.inventory.data.InventoryContract.InventoryEntry;
import com.crystaltowerdesigns.inventory.data.InventoryDbHelper;

import java.text.NumberFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final Random randomNumberClass = new Random(); // Initialize the randomNumberClass

    /**
     * {@LINK} getRandom
     *
     * @param upperBound int, representing the highest int to return.
     * @param zeroBased  boolean indicating if result should be zero based.
     *
     * @return int
     * if zeroBased then the random number returned is 0 to upperBound.
     * if !zeroBased then the random number returned is 1 to upperBound.
     */
    private int getRandom(int upperBound, boolean zeroBased) {
        if (zeroBased)
            return randomNumberClass.nextInt(upperBound + 1);
        else
            return randomNumberClass.nextInt(upperBound) + 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add our toolbar
        Toolbar myToolbar = findViewById(R.id.inventory_app_toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseRawItems();
    }

    /**
     * {@LINK currency}
     *
     * @param value a number to convert to currency format
     *
     * @return String containing the formatted 'value'
     */
    private String currency(Object value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseRawItems() {
        // projection specifying what columns to include in query
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER};

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to access the inventory data.
        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI, projection, null, null, null);

        TextView displayView = findViewById(R.id.database_summary);
        TextView displayViewHeader = findViewById(R.id.database_summary_header);

        if (cursor != null) {
            try {
                displayViewHeader.setText(String.format(getString(R.string.rawInventoryViewHeader), cursor.getCount()));
                displayViewHeader.append(InventoryEntry._ID + " - " +
                        InventoryEntry.COLUMN_ITEM_NAME + " - " +
                        InventoryEntry.COLUMN_ITEM_PRICE + " - " +
                        InventoryEntry.COLUMN_ITEM_QUANTITY + " - " +
                        InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME + " - " +
                        InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER + "\n");

                // Figure out the index of each column
                int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
                int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
                int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
                int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
                int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME);
                int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER);

                displayView.setText("");
                // Loop through all rows in the cursor and display them
                while (cursor.moveToNext()) {
                    int currentID = cursor.getInt(idColumnIndex);
                    String currentName = cursor.getString(nameColumnIndex);
                    Float currentPrice = cursor.getFloat(priceColumnIndex);
                    int currentQuantity = cursor.getInt(quantityColumnIndex);
                    String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                    String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);
                    // Add values to our TextView
                    displayView.append(("\n" + currentID + " - " +
                            currentName + " - " +
                            currency(currentPrice) + " - " +
                            currentQuantity + " - " +
                            currentSupplierName + " - " +
                            currentSupplierPhone));
                }
            } finally {
                // Always close cursor to release resources.
                cursor.close();
            }
        }
    }

    /**
     * Temporary Helper Method
     * <p>
     * Randomly select one of two sets of item entries/names along with
     * random prices and quantities and then inserts it into the database.
     */
    private void insertItem() {
        // Create a ContentValues object where column names are the keys.
        ContentValues values = new ContentValues();
        switch (getRandom(1, true)) {
            case 0:
                values.put(InventoryEntry.COLUMN_ITEM_NAME, "Widget One");
                values.put(InventoryEntry.COLUMN_ITEM_PRICE, getRandom(1000, false) / 100.0);
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, getRandom(15, false));
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, "Hansen Supplies");
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, "715-555-1212");
                break;
            default:
                values.put(InventoryEntry.COLUMN_ITEM_NAME, "Widget Two");
                values.put(InventoryEntry.COLUMN_ITEM_PRICE, getRandom(1000, false) / 100.0);
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, getRandom(15, true));
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, "Peterson Warehouse");
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, "906-555-1212");
                break;
        }
        @SuppressWarnings("unused") Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        Toast.makeText(this, "Item added to inventory", Toast.LENGTH_SHORT).show();
    }

    /**
     * {@LINK totalItemPrices}
     *
     * @param targetName @Nullable String containing the name of the items to total. A null value specifies 'all' items.
     *                   <p>
     *                   Temporary Helper Method
     */
    private void totalItemPrices(@Nullable String targetName) {
        // Specify what columns to include in query
        String[] projection = {InventoryEntry.COLUMN_ITEM_PRICE, InventoryEntry.COLUMN_ITEM_QUANTITY};

        // determine proper selection string per item name specification
        String selection;
        if (targetName == null)
            selection = null;
        else
            selection = InventoryEntry.COLUMN_ITEM_NAME + "=\"" + InventoryDbHelper.doubleQuote(targetName) + "\"";

        // Query a cursor from getContentResolver
        // Use the {@link InventoryEntry#CONTENT_URI} to access the inventory data.
        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI, projection, selection, null, null);

        TextView displayViewHeader = findViewById(R.id.database_summary_header);

        Double totalPrice = 0.0;
        if (cursor != null) {
            try {
                // get column indexes
                int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
                int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

                // Loop through all rows in the cursor and add them up
                while (cursor.moveToNext())
                    totalPrice = totalPrice + cursor.getFloat(priceColumnIndex) * cursor.getInt(quantityColumnIndex);

                // Apply proper string to the header depending on whether or not targetName was specified
                if (targetName == null)
                    displayViewHeader.setText(String.format(getString(R.string.sumPricesViewHeader), cursor.getCount(), "", currency(totalPrice)));
                else
                    displayViewHeader.setText(String.format(getString(R.string.sumPricesViewHeader), cursor.getCount(), targetName + " ", currency(totalPrice)));
            } finally {
                // Always close cursor to release resources.
                cursor.close();
            }
        }
    }

    /**
     * Temporary Helper Method
     */
    private void deleteAllItems() {
        int result = getContentResolver().delete(InventoryEntry.CONTENT_URI, "", new String[]{});
        Toast.makeText(this, result + getString(R.string.items_deleted), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu to app bar.
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Menu option item was clicked in the app bar
        // Menu option item ID's are self explanatory
        // Take appropriate action(s)
        switch (item.getItemId()) {
            case R.id.action_add_2_dummy_items:
                insertItem();
                displayDatabaseRawItems();
                return true;
            case R.id.action_delete_all_inventory:
                deleteAllItems();
                displayDatabaseRawItems();
                return true;
            case R.id.action_subtotal_prices_of_all_items:
                totalItemPrices(null);
                return true;
            case R.id.action_subtotal_prices_of_all_widget_one_items:
                totalItemPrices("Widget One");
                return true;
            case R.id.action_subtotal_prices_of_all_widget_two_items:
                totalItemPrices("Widget Two");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
