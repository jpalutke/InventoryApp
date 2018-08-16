package com.crystaltowerdesigns.inventory;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.crystaltowerdesigns.inventory.data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // INVENTORY_LOADER ID
    private static final int INVENTORY_LOADER = 0;
    private final Random randomNumberClass = new Random(); // Initialize the randomNumberClass
    /**
     * Temporary Helper Array for dummy inserts
     */
    @SuppressWarnings("SpellCheckingInspection")
    private final String[] itemTypeString = {"Widget", "Gizmo", "Whatchamacallit", "Gadget", "Whatsit", "Doodad", "Thingamabob", "Thingamajig", "Contraption", "Contrivance", "Object"};
    // ListView Adapter
    private InventoryCursorAdapter inventoryCursorAdapter;

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

        FloatingActionButton fabButton = findViewById(R.id.FloatingActionButton_Add);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to View/Edit item
                Intent intent = new Intent(MainActivity.this, ItemEditorActivity.class);
                // Launch the {@link EditorActivity} to edit a new item.
                startActivity(intent);
            }
        });

        ListView inventoryListView = findViewById(R.id.database_list_view);

        View emptyListView = findViewById(R.id.empty_inventory_view);
        inventoryListView.setEmptyView(emptyListView);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(inventoryCursorAdapter);

        // item click listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Intent to View/Edit item
                Intent intent = new Intent(MainActivity.this, ItemEditorActivity.class);

                // Append the "id" on to the {@link InventoryEntry#CONTENT_URI}.
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(currentInventoryUri);

                // Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
            }
        });

        // Launch the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this).forceLoad();
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
     * Temporary Helper Method
     * <p>
     * Randomly creates an item entry with details and then inserts it into the database.
     */
    private void insertItem() {
        // Create a ContentValues object where column names are the keys.
        ContentValues values = new ContentValues();

        int itemTypeIndex = getRandom(itemTypeString.length, false) - 1;

        // Pick a random dummy supplier and phone
        switch (getRandom(1, true)) {
            case 0:
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, getString(R.string.dummy_supplier_1_name));
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, getString(R.string.dummy_supplier_1_phone));
                break;
            default:
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, getString(R.string.dummy_supplier_2_name));
                values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, getString(R.string.dummy_supplier_2_phone));
        }

        values.put(InventoryEntry.COLUMN_ITEM_NAME, itemTypeString[itemTypeIndex]);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, getRandom(1000, false) / 100.0);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, getRandom(5, true));

        @SuppressWarnings("unused") Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        Toast.makeText(this, R.string.item_added, Toast.LENGTH_SHORT).show();
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
            selection = String.format("%s=\"%s\"", InventoryEntry.COLUMN_ITEM_NAME, targetName);

        // Query a cursor from getContentResolver
        // Use the {@link InventoryEntry#CONTENT_URI} to access the inventory data.
        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI, projection, selection, null, null);

        Double totalPrice = 0.0;
        if (cursor != null) {
            try {
                // get column indexes
                int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
                int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
                int totalIndividualItemCount = 0;

                // Loop through all rows in the cursor and add them up
                while (cursor.moveToNext()) {
                    totalPrice = totalPrice + cursor.getFloat(priceColumnIndex) * cursor.getInt(quantityColumnIndex);
                    totalIndividualItemCount += cursor.getInt(quantityColumnIndex);
                }
                // Apply proper string to the header depending on whether or not targetName was specified
                if (targetName == null)
                    summaryAlert(String.format(getString(R.string.all_items_query_result), totalIndividualItemCount, currency(totalPrice)));
                else {
                    summaryAlert(String.format(getString(R.string.items_query_result), totalIndividualItemCount, targetName.toUpperCase(), currency(totalPrice)));
                }
            } finally {
                // Always close cursor to release resources.
                cursor.close();
            }
        }
    }

    /**
     * Temporary Helper Method
     */
    @SuppressLint("DefaultLocale")
    private void deleteAllItems() {
        int result = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Toast.makeText(this, String.format("%d %s", result, getString(R.string.items_deleted)), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu to app bar.
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);

        // Create our Summary subMenu
        Menu subMenu = menu.addSubMenu(0, menu.size(), Menu.NONE, R.string.summary_for_title);

        // Add an menu item for 'All Inventory'
        subMenu.add(0, menu.size(), Menu.NONE, getString(R.string.all_items_title));

        // Add our item price summaries to the menu
        for (String anItemTypeString : itemTypeString) {
            subMenu.add(0, menu.size(), Menu.NONE, String.format("%s %s", anItemTypeString, getString(R.string.summary_search_key)));
        }

        return true;
    }

    private void summaryAlert(String alertMessage) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(alertMessage);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Menu option item was clicked in the app bar
        // Menu option item ID's are self explanatory
        // Take appropriate action(s)
        switch (item.getItemId()) {
            case R.id.action_add_dummy_item:
                insertItem();
                return true;
            case R.id.action_delete_all_inventory:
                deleteAllItems();
                return true;
            default: {
                // Handles the SUMMARY items that we added via onCreateOptionsMenu
                String itemTitle = "" + item.getTitle();
                if (itemTitle.equals(getString(R.string.all_items_title))) {
                    totalItemPrices(null);
                    return true;
                } else {
                    if (itemTitle.endsWith(getString(R.string.summary_search_key))) {
                        itemTitle = itemTitle.replace(String.format(" %s", getString(R.string.summary_search_key)), "");
                        totalItemPrices(itemTitle);
                        return true;
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        inventoryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        inventoryCursorAdapter.swapCursor(null);
    }
}
