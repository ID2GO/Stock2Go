package eu.id2go.stock2go;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView nameTextView = view.findViewById(R.id.name);
        TextView summaryTextView = view.findViewById(R.id.summary);
        ImageView image = view.findViewById(R.id.get_image_button);
        // Extract properties from cursor
        int nameColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_NAME);
        int brandColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_BRAND);

        // Read the stock item attributes from the Cursor for the current stock
        String stockItemName = cursor.getString(nameColumnIndex);
        String stockItemBrand = cursor.getString(brandColumnIndex);

        image.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(StockItemEntry.COLUMN_IMAGE))));

                // If the stock item brand is empty string or null, then use some default text
        // that says "Unknown brand", so the TextView isn't blank.
        if (TextUtils.isEmpty(stockItemBrand)) {
            stockItemBrand = context.getString(R.string.unknown_brand);
        }

        // Populate or update TextViews with extracted stock item properties
        nameTextView.setText(stockItemName);
        summaryTextView.setText(String.valueOf(stockItemBrand));

    }
}