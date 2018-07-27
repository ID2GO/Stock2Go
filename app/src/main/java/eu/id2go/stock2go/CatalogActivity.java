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

package eu.id2go.stock2go;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import eu.id2go.stock2go.data.StockContract.StockItemEntry;


/**
 * Displays list of stock2go that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_LOADER = 0;

    StockCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the stockItem data
        ListView stockItemListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        stockItemListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of stockItem data in the Cursor.
        // There is no stockItem data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new StockCursorAdapter(this, null);
        stockItemListView.setAdapter(mCursorAdapter);

        // Setup item click listener
        stockItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // The ContentUris.withAppendedId method forms the content URI to represent the specific
                // stockItem that was clicked on, by appending the "id" to it (id passed as input to this method) onto the
                // {@link StockItemEntry#CONTENT_URI}
                // Example URI: content://eu.id2go.stock2go/stock2go/<StockItem_ID> // Instead of: URI: content://com.example.android.stock2go/stock2go/<StockItem_ID>
                // if the stockItem with ID was clicked on
                Uri currentStockItemUri = ContentUris.withAppendedId(StockItemEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentStockItemUri);

                // Launch the {@link EditorActivity} to display the data for the current stockItem.
                startActivity(intent);
            }
        });
        /*
         * Initializes the CursorLoader. The STOCK_Loader value is eventually passed to onCreateLoader().
         */
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
    }


    /**
     * Helper method to insert hardcoded stockItem data into the database. For debugging purposes only.
     */
    private void insertStockItem() {

        // Create a ContentValues object where column names are the keys,
        // and dummy stockItem attributes are the values.
        ContentValues values = new ContentValues();
        values.put(StockItemEntry.COLUMN_NAME, "Knife");
        values.put(StockItemEntry.COLUMN_BRAND, "Gero Young");
        values.put(StockItemEntry.COLUMN_STOCK_QTY, 12);
        values.put(StockItemEntry.COLUMN_NAME_SUPPLIER, "Gero");
        values.put(StockItemEntry.COLUMN_PHONE_SUPPLIER, "+31650620159");
        values.put(StockItemEntry.COLUMN_EMAIL_SUPPLIER, "info@gero.nl");
        values.put(StockItemEntry.COLUMN_SECTION, StockItemEntry.SECTION_KITCHEN_UTENSILS);
        values.put(StockItemEntry.COLUMN_PRICE, 17);
        values.put(StockItemEntry.COLUMN_IMAGE, "android.resource://eu.id2go.stock2go/drawable/kaas.jpg");

        // Insert a new row of dummy data in the database, returning the ID of that new row.
        // The first argument for db.insert() is the stock2go table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        Uri newUri = getContentResolver().insert(StockItemEntry.CONTENT_URI, values);

        // Show a toast message of either success saving or error saving
        if (newUri == null) {
            // If the row ID is -1, then saving resulted in an error
            Toast.makeText(this, getString(R.string.toast_error_inserting_dummy_stock_item_data), Toast.LENGTH_LONG).show();
        } else {
            // Otherwise saving was successful and a toast displays showing a row ID
            Toast.makeText(this, getString(R.string.toast_success_inserting_dummy_stock_item_data), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertStockItem();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllStockItem();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all stock2go in the database for Developer database testing purposes only!
     */
    private void deleteAllStockItem() {
        int rowsDeleted = getContentResolver().delete(StockItemEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            // If no rows were affected, then there was an error deleting the Table in the database.
            Log.v("CatalogActivity", rowsDeleted + getString(R.string.error_deleting_all_entries));

            Toast.makeText(this, getString(R.string.error_deleting_all_entries),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, deletion was successful
            Log.v("CatalogActivity", rowsDeleted + getString(R.string.confirmation_deletion_all_entries));

            Toast.makeText(this, getString(R.string.confirmation_deletion_all_entries) + mCursorAdapter.getCount(), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define the range of columns from the database to be used
        String[] projection = {
                StockItemEntry._ID,
                StockItemEntry.COLUMN_NAME,
                StockItemEntry.COLUMN_BRAND,
                StockItemEntry.COLUMN_STOCK_QTY,
                StockItemEntry.COLUMN_NAME_SUPPLIER,
                StockItemEntry.COLUMN_PHONE_SUPPLIER,
                StockItemEntry.COLUMN_EMAIL_SUPPLIER,
                StockItemEntry.COLUMN_SECTION,
                StockItemEntry.COLUMN_PRICE,
                StockItemEntry.COLUMN_IMAGE};

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link StockItemEntry#CONTENT_URI} to access the stockItem data.
        return new CursorLoader(this,
                StockItemEntry.CONTENT_URI,    // The Content URI of the stock2go Table of the db to query
                projection,               // The above range of columns to return for each row
                null,            // The column for the WHERE query
                null,         // Selection criteria
                null);           // The sort order for the returned rows

    }

    /**
     * Called when a previously created loader has finished its load.
     * <p>
     * This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter (Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).
     * This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        /*
         * Moves the query results into the adapter, causing the ListView fronting this adapter to
         * re-display the updated data
         */
        mCursorAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        /*
         * Clears out the adapter's reference to the Cursor.
         * This prevents memory leaks.
         */
        mCursorAdapter.swapCursor(null);
    }
}


