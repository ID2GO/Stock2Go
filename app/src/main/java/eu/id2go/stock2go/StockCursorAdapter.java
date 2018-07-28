package eu.id2go.stock2go;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import eu.id2go.stock2go.data.StockContract.StockItemEntry;

/**
 * {@link StockCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of stock data as its data source. This adapter knows
 * how to create list items for each row of stock data in the {@link Cursor}.
 */
public class StockCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link StockCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public StockCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the stock data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current stock item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        ImageButton buyItemBtn = view.findViewById(R.id.buy_btn);
        ImageView image = view.findViewById(R.id.list_item_image_view);

        // Extract properties from cursor
        final int id = cursor.getInt(cursor.getColumnIndex(StockItemEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_PRICE);
        final int stockItemQty = cursor.getInt(cursor.getColumnIndex(StockItemEntry.COLUMN_STOCK_QTY));

        // Read the stock item attributes from the Cursor for the current stock
        String stockItemName = cursor.getString(nameColumnIndex);
        String stockItemPrice = cursor.getString(priceColumnIndex);
        // final String stockItemQty = cursor.getString(qtyColumnIndex);


        image.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(StockItemEntry.COLUMN_IMAGE))));



        // Populate or update TextViews with extracted stock item properties
        nameTextView.setText(stockItemName);
        priceTextView.setText(String.valueOf(stockItemPrice));
        quantityTextView.setText(String.valueOf(stockItemQty));


        buyItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri mCurrentStockItemUri = ContentUris.withAppendedId(StockItemEntry.CONTENT_URI, id);
                addToCart(context, mCurrentStockItemUri, stockItemQty);

            }
        });

    }

    private void addToCart(Context context, Uri uri, int stockItemQty) {
        if (stockItemQty > 0) {
            int newAvailableQuantityValue = stockItemQty - 1;

            ContentValues values = new ContentValues();
            values.put(StockItemEntry.COLUMN_STOCK_QTY, newAvailableQuantityValue);


            int rowsAffected = context.getContentResolver().update(uri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(context, (R.string.toast_error_adding_item_to_cart), Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(context, (R.string.toast_success_adding_item_to_cart), Toast.LENGTH_SHORT).show();

            }

        }
    }
}