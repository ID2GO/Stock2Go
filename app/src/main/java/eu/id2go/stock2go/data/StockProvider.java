package eu.id2go.stock2go.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import eu.id2go.stock2go.data.StockContract.StockItemEntry;

import static eu.id2go.stock2go.data.StockContract.CONTENT_AUTHORITY;
import static eu.id2go.stock2go.data.StockContract.PATH_STOCK;

/**
 * {@link ContentProvider} for Stock2Go app.
 */
public class StockProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = StockProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the stock2go table
     */
    private static final int STOCK = 100;
    /**
     * URI matcher code for the content URI for a single stock item in the stock2go table
     */
    private static final int STOCK_ID = 101;
//    private static final int STOCK_NAME = 102;
//    private static final int STOCK_BRAND = 103;
//    private static final int STOCK_SECTION = 104;
//    private static final int STOCK_PRICE = 105;


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
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_STOCK, STOCK); // Alternative but less nice because of hard coded ContentAuthority & path: sUriMatcher.addURI("eu.id2go.stock2go", "stock2go", STOCK);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_STOCK + "/#", STOCK_ID); // Alternative but less nice because of hard coded ContentAuthority & path: sUriMatcher.addURI("eu.id2go.stock2go", "stock2go/#", STOCK_ID);
//      sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK +"/#", STOCK_NAME);   // Alternative but less nice because of hard coded ContentAuthority & path: sUriMatcher.addURI("eu.id2go.stock2go", "stock2go/#", STOCK_NAME);
//      sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK +"/#", STOCK_BRAND);  //Alternative but less nice because of hard coded ContentAuthority & path: sUriMatcher.addURI("eu.id2go.stock2go", "stock2go/#", STOCK_BRAND);
//      sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK +"/#", STOCK_SECTION); //Alternative but less nice because of hard coded ContentAuthority & path: sUriMatcher.addURI("eu.id2go.stock2go", "stock2go/#", STOCK_SECTION);
//      sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK +"/#", STOCK_PRICE); //Alternative but less nice because of hard coded ContentAuthority & path: sUriMatcher.addURI("eu.id2go.stock2go", "stock2go/#", STOCK_PRICE);

    }

    /**
     * Database helper object
     * Creating and initializing a StockDbHelper object secures access to the stock2go database.
     */
    private StockDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // The variable is to be a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new StockDbHelper(getContext());
        return true;
    }

    /**
     * Retrieve data from your provider. Use the arguments to select the table to query, the rows and
     * columns to return, and the sort order of the result. Return the data as a Cursor object.
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                // For the Stock code, query the stock2go table directly with the given projection,
                // selection, selection arguments, and sort order.
                // The cursor could contain multiple rows of the stock2go table.
                cursor = database.query(StockItemEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case STOCK_ID:
                // For the STOCK_ID code, extract out the _ID from the URI.
                // For an example URI such as " content://com.example.android.stock2go/stock2go/3",
                // the selection will be " _id=?" and the selection argument wil be a
                // String array containing the actual _ID of 3 in this case.
                //
                //For every "?" in the selection, we need to have an element in the selection arguments
                // that will fill in the "?". Since we have 1 question mark in the selection,
                // we hae 1 string in the selection arguments String array.
                selection = StockItemEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                // This will perform a query on the stock2go table where the _id equals 3 to return a
                // Cursor containing the selected row of data of the table.
                cursor = database.query(StockItemEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor, so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor
        return cursor;
    }

    /**
     * Returns the MIME type of data corresponding to the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return StockItemEntry.CONTENT_LIST_TYPE;
            case STOCK_ID:
                return StockItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert a new row of data into the provider with the given ContentValues.
     * Use the arguments to select the destination table and to get the column values to use.
     * Return a content URI for the newly-inserted row.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return insertStockItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    /**
     * Insert a stock item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertStockItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(StockItemEntry.COLUMN_NAME);
        // check brand is not null
        String brand = values.getAsString(StockItemEntry.COLUMN_BRAND);
        // check section if it is null or invalid
        Integer section = values.getAsInteger(StockItemEntry.COLUMN_SECTION);
        // check price to be equal or greater than 0 kg
        Integer price = values.getAsInteger(StockItemEntry.COLUMN_PRICE);


        // sanity check name
        // using TextUtils.isEmpty(name) {} instead of using (name==null || name.isEmpty() ){}
        // It's faster and will return true if the String is empty or null.
        if (TextUtils.isEmpty(name)) {
//            Toast.makeText(getContext(), (R.string.toast_insert_stock_item_name),Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Stock item requires a name");
        }
        // check brand
        if (brand == null || brand.isEmpty()) {
//            Toast.makeText(getContext(), (R.string.toast_insert_stock_item_brand),Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Stock item requires valid brand");

        }
        // check section with either/or check
        if (section == null || !StockItemEntry.isValidSection(section)) {
            throw new IllegalArgumentException("Stock item requires valid section");
        }
        // check price checking both conditions with &&
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Stock item requires valid price");
        }

        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new stock item with the given values
        long newRowId = db.insert(StockItemEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);

            return null;
        }

        // Notify all listeners that the data has changed for the stock item content URI
        // uri: content://eu.id2go.stock2go/stock2go
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, newRowId);
    }


    /**
     * Updates the data of existing rows at the given selection and selection arguments, with the new ContentValues.
     * Use the arguments to select the table and rows to update and to get the updated column values.
     * Return the number of rows updated.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return updateStockItem(uri, contentValues, selection, selectionArgs);
            case STOCK_ID:
                // For the STOCK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = StockItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateStockItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update stock2go in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more stock2go).
     * Return the number of rows that were successfully updated.
     */
    private int updateStockItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that the name is not null or empty
        String name = values.getAsString(StockItemEntry.COLUMN_NAME);
        // check brand is not null or empty
        String brand = values.getAsString(StockItemEntry.COLUMN_BRAND);
        // check section if it is null or invalid
        Integer section = values.getAsInteger(StockItemEntry.COLUMN_SECTION);
        // check price to be equal or greater than 0 kg
        Integer price = values.getAsInteger(StockItemEntry.COLUMN_PRICE);

        // If the {@link StockItemEntry#COLUMN_NAME} key is present,
        // check that the name value is not null or empty.
        if (values.containsKey(StockItemEntry.COLUMN_NAME)) {
            // sanity check name
            // using TextUtils.isEmpty(name) {} instead of using (name==null || name.isEmpty() ){}
            // It's faster and will return true if the String is empty or null.
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Stock item requires a name");
//            Toast.makeText(this, getString(R.string.toast_insert_stock_item_name),Toast.LENGTH_SHORT).show();
            }
        }
        // If the {@link StockItemEntry#COLUMN_BRAND} key is present,
        // check that the brand value is not null or empty.
        if (values.containsKey(StockItemEntry.COLUMN_BRAND)) {
            // sanity check brand
            if (brand == null || brand.isEmpty()) {
                throw new IllegalArgumentException("Stock item requires valid brand");
//            Toast.makeText(this, getString(R.string.toast_insert_stock_item_brand),Toast.LENGTH_SHORT).show();
            }
        }
        // If the {@link StockItemEntry#COLUMN_SECTION} key is present,
        // check that the section value is not null or !StockItemEntry.isValidSection
        if (values.containsKey(StockItemEntry.COLUMN_SECTION)) {
            // sanity check section with either/or check
            if (section == null || !StockItemEntry.isValidSection(section)) {
                throw new IllegalArgumentException("Stock item requires valid section");
            }
        }
        // If the {@link StockItemEntry#COLUMN_PRICE} key is present,
        // check that the price value is not null && price < 0
        if (values.containsKey(StockItemEntry.COLUMN_PRICE)) {
            // sanity check price checking both conditions with &&
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Stock item requires valid price");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }


        // Otherwise get database in writing mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();


        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(StockItemEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


    /**
     * Delete the rows of data at the given selection and selection arguments.
     * Use the arguments to select the table and the rows to delete. Return the number of rows deleted.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                // Delete all rows that match the selection and selection args for case StockItem
                rowsDeleted = db.delete(StockItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STOCK_ID:
                // Delete a single row given by the ID in the URI
                selection = StockItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(StockItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed.
        if (rowsDeleted != 0) {

            // Notify all listeners that the data has changed for the stock item content URI
            // uri: content://eu.id2go.stock2go/stock2go
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }
}