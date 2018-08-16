package com.crystaltowerdesigns.inventory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crystaltowerdesigns.inventory.data.InventoryContract.InventoryEntry;
import com.crystaltowerdesigns.inventory.data.Validation;

import static com.crystaltowerdesigns.inventory.data.Validation.IS_NUMERIC;
import static com.crystaltowerdesigns.inventory.data.Validation.IS_WHOLE_NUMBER;
import static com.crystaltowerdesigns.inventory.data.Validation.NOT_EMPTY;
import static com.crystaltowerdesigns.inventory.data.Validation.NOT_NULL;
import static com.crystaltowerdesigns.inventory.data.Validation.isValid;

public class ItemEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int EDIT_ITEM_LOADER = 100;
    private EditText itemName;
    private EditText itemOnHand;
    private EditText itemPrice;
    private EditText itemSupplierName;
    private EditText itemSupplierPhoneNumber;
    private boolean changesDetected = false;
    private Uri currentItemUri;
    private int homeButtonCount = 0;

    /**
     * When any item is clicked on/selected, the changesDetected variable is set to true
     * and the counter homeButtonPressed is set to zero.
     */
    private final View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            changesDetected = true;
            homeButtonCount = 0;
            return false;
        }
    };

    /**
     * {link}
     * Method to close the soft keyboard. Only works within an Activity.
     *
     * @param activity The active Activity to which the keyboard is attached.
     */
    private static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null)
            view = new View(activity);
        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editor);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        currentItemUri = intent.getData();

        // Grab our views and set our touchListener(s)
        itemName = findViewById(R.id.editText_itemName);
        itemName.setOnTouchListener(touchListener);
        itemOnHand = findViewById(R.id.editText_itemOnHand);
        itemOnHand.setOnTouchListener(touchListener);
        itemPrice = findViewById(R.id.editText_itemPrice);
        itemPrice.setOnTouchListener(touchListener);
        itemSupplierName = findViewById(R.id.editText_itemSupplierName);
        itemSupplierName.setOnTouchListener(touchListener);
        itemSupplierPhoneNumber = findViewById(R.id.editText_itemSupplierPhone);
        itemSupplierPhoneNumber.setOnTouchListener(touchListener);

        // onHand Up Button Listener
        ImageButton itemOnHand_up = findViewById(R.id.itemOnHand_up);
        itemOnHand_up.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemOnHand_up_onClick();
            }
        });

        // onHand Down Button Listener
        ImageButton itemOnHand_down = findViewById(R.id.itemOnHand_down);
        itemOnHand_down.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemOnHand_down_onClick();
            }
        });

        // Is this a new or existing Item? Set Title accordingly
        if (currentItemUri != null) {
            setTitle(getString(R.string.edit_item_title));
            getSupportLoaderManager().initLoader(EDIT_ITEM_LOADER, null, this);
        } else {
            invalidateOptionsMenu();
            setTitle(getString(R.string.add_item_title));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // remove the delete button if this is a new item
        if (currentItemUri == null)
            menu.findItem(R.id.action_delete_item).setVisible(false);
        return true;
    }

    private boolean validateAndSaveItem() {
        // Is data valid? Use Validation method with toasts.
        // Check fields in reverse order so the 'invalid' warnings will appear from top on down
        if (!Validation.isValid(this, getString(R.string.item_supplierPhone), itemSupplierPhoneNumber.getText().toString(), NOT_NULL, NOT_EMPTY) |
                !Validation.isValid(this, getString(R.string.item_supplierName), itemSupplierName.getText().toString(), NOT_NULL, NOT_EMPTY) |
                !Validation.isValid(this, getString(R.string.item_price), itemPrice.getText().toString(), NOT_NULL, IS_NUMERIC) |
                !Validation.isValid(this, getString(R.string.item_onHand), itemOnHand.getText().toString(), NOT_NULL, IS_WHOLE_NUMBER) |
                !Validation.isValid(this, getString(R.string.item_name), itemName.getText().toString(), NOT_NULL, NOT_EMPTY))
            return false;
        else {
            // Yes, update/save the changes
            ContentValues contentValues = new ContentValues();
            contentValues.put(InventoryEntry.COLUMN_ITEM_NAME, itemName.getText().toString());
            contentValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, itemOnHand.getText().toString());
            contentValues.put(InventoryEntry.COLUMN_ITEM_PRICE, itemPrice.getText().toString());
            contentValues.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, itemSupplierName.getText().toString());
            contentValues.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER, itemSupplierPhoneNumber.getText().toString());
            // Is this a new item?
            if (currentItemUri == null)
                // Yes. Insert it and inform user of resulting operation.
                if (getContentResolver().insert(InventoryEntry.CONTENT_URI, contentValues) != null)
                    Toast.makeText(this, R.string.successful_save, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, R.string.failed_save, Toast.LENGTH_SHORT).show();
            else
                // No. Update the existing record and inform user of resulting operation.
                if (getContentResolver().update(currentItemUri, contentValues, null, null) == 0)
                    Toast.makeText(this, R.string.failed_update, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, R.string.successful_update, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ItemEditorActivity.this, MainActivity.class));
            finish();
            return true;
        }
    }

    /**
     * Method to handle menu clicks.
     *
     * @param item menuItem that was selected.
     *
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideKeyboard(this);
        switch (item.getItemId()) {
            case R.id.action_delete_item:
                deleteItemConfirmation();
                return true;
            case R.id.action_save_update_item:
                // if data is invalid, then abort the menu function
                return validateAndSaveItem();
            case R.id.action_call_to_reorder_item: {
                placeCallToReorder();
                return true;
            }
            case android.R.id.home:
                if (!changesDetected) {
                    super.onBackPressed();
                    startActivity(new Intent(ItemEditorActivity.this, MainActivity.class));
                    finish();
                } else {
                    if (homeButtonCount < 1) {
                        homeButtonCount += 1;
                        Toast.makeText(this, R.string.unsaved_changes, Toast.LENGTH_SHORT).show();
                    } else {
                        super.onBackPressed();
                        startActivity(new Intent(ItemEditorActivity.this, MainActivity.class));
                        finish();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Internal method to launch a call to the supplier to place an order.
     */
    private void placeCallToReorder() {
        Uri callUri;
        try {
            callUri = Uri.parse(String.format("tel:%s", itemSupplierPhoneNumber.getText().toString()));
        } catch (Exception e) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(callUri);
        startActivity(intent);
    }

    /**
     * Ask user to confirm the deletion
     */
    private void deleteItemConfirmation() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_item_prompt);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(ItemEditorActivity.this, MainActivity.class));
                deleteItem();
            }
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method called by our confirmation routine to do the actual delete.
     */
    private void deleteItem() {
        if (currentItemUri != null) {
            if (getContentResolver().delete(currentItemUri, null, null) == 0)
                // nothing was delete
                Toast.makeText(this, R.string.delete_cancelled, Toast.LENGTH_SHORT).show();
            else {
                // item was deleted.
                Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ItemEditorActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // do we have any changes?
        if (!changesDetected) {
            // If not, execute the BACK.
            super.onBackPressed();
            startActivity(new Intent(ItemEditorActivity.this, MainActivity.class));
            finish();
        } else {
            // If changes, see if back has already been pressed once since last change detected.
            if (homeButtonCount < 1) {
                // First press, Increment the counter and instruct user to press BACK again.
                homeButtonCount += 1;
                Toast.makeText(this, R.string.unsaved_changes, Toast.LENGTH_SHORT).show();
            } else {
                // Second BACK press since changes. Discard the changes
                super.onBackPressed();
                startActivity(new Intent(ItemEditorActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this, currentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        // if a record exists, set the fields. Method moveToFirst returns false if the cursor is empty.
        if (cursor.moveToFirst()) {
            itemName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME)));
            itemOnHand.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY)));
            itemPrice.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE)));
            itemSupplierName.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME)));
            itemSupplierPhoneNumber.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_PHONE_NUMBER)));
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // clear the data entry fields
        itemName.setText("");
        itemOnHand.setText(R.string.initial_onHand);
        itemPrice.setText("");
        itemSupplierName.setText("");
        itemSupplierPhoneNumber.setText("");
    }

    @SuppressLint("SetTextI18n")
    private void itemOnHand_down_onClick() {
        hideKeyboard(this);
        if (isValid(itemOnHand.getText().toString(), NOT_NULL, IS_WHOLE_NUMBER)) {
            int currentValue = Integer.parseInt(itemOnHand.getText().toString());
            if (currentValue > 0) {
                currentValue -= 1;
                itemOnHand.setText(Integer.toString(currentValue));
                changesDetected = true;
            }
        } else
            // If the onHand field is blank, set it to 0.
            itemOnHand.setText(Integer.toString(0));
    }

    @SuppressLint("SetTextI18n")
    private void itemOnHand_up_onClick() {
        hideKeyboard(this);
        if (isValid(itemOnHand.getText().toString(), NOT_NULL, IS_WHOLE_NUMBER)) {
            int currentValue = Integer.parseInt(itemOnHand.getText().toString());
            currentValue += 1;
            itemOnHand.setText(Integer.toString(currentValue));
            changesDetected = true;
        } else
            // If the onHand field is blank, set it to 1.
            itemOnHand.setText(Integer.toString(1));
    }
}
