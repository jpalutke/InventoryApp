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
package com.crystaltowerdesigns.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.crystaltowerdesigns.inventory.data.InventoryContract;

import java.text.NumberFormat;

/**
 * {@link InventoryCursorAdapter} is an adapter that displays a {@link Cursor} of inventory items.
 */
class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context App context.
     * @param cursor  Cursor containing inventory data.
     */
    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * Creates a blank item view.
     *
     * @param context App context.
     * @param cursor  Cursor containing inventory data.
     * @param parent  The parent to which the new view is attached to
     *
     * @return New recycler item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate the layout specified in recycler_item_view
        return LayoutInflater.from(context).inflate(R.layout.recycler_item_view, parent, false);
    }

    /**
     * Binds the inventory row designated by the cursor
     *
     * @param view    Existing view.
     * @param context App context.
     * @param cursor  Cursor containing inventory data.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView itemNameTextView = view.findViewById(R.id.itemName);
        TextView itemOnHandTextView = view.findViewById(R.id.itemOnHand);
        TextView itemPriceTextView = view.findViewById(R.id.itemPrice);
        Button itemSellButton = view.findViewById(R.id.sell_button);
        TextView itemOutOfStockText = view.findViewById(R.id.out_of_stock);

        // Get column indexes
        int itemNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        int itemOnHandColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
        int itemPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        int itemIDColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);

        // Read the pet attributes from the Cursor for the current pet
        String itemName = cursor.getString(itemNameColumnIndex);
        int itemOnHand = cursor.getInt(itemOnHandColumnIndex);
        Float itemPrice = cursor.getFloat(itemPriceColumnIndex);
        final int id = cursor.getInt(itemIDColumnIndex);
        final int itemOnHandIfSold = itemOnHand - 1;
        // Update the TextViews with the attributes for the current pet
        itemNameTextView.setText(itemName);
        itemOnHandTextView.setText(String.valueOf(itemOnHand));
        itemPriceTextView.setText(NumberFormat.getCurrencyInstance().format(itemPrice));

        if (itemOnHand > 0) {
            itemSellButton.setVisibility(View.VISIBLE);
            itemOutOfStockText.setVisibility(View.GONE);
            itemSellButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    Uri currentInventoryItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                    ContentValues values = new ContentValues();

                    values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, String.valueOf(itemOnHandIfSold));
                    context.getContentResolver().update(currentInventoryItemUri, values, null, null);
                }
            });
        } else {
            itemSellButton.setVisibility(View.GONE);
            itemOutOfStockText.setVisibility(View.VISIBLE);
        }
    }
}
