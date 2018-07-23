package eu.id2go.stock2go.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @Contracts exist of 3 parts:
 * Outer class named: BlankContract
 * Inner class named: BlankEntry that implements BaseColumns for each table in the database
 * String constants for each of the headings in the database
 * Data related CONSTANTS are reusable components that do not change and that are for purpose of
 * identifacation WRITEN IN CAPS
 */
public final class StockContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.
     * A convenient string to use for the content authority is the package name for the app, which
     * is guaranteed to be unique on the device.
     */
    public static final String CONTENT_AUTHORITY = "eu.id2go.stock2go"; // ...id2go... in stead of: com.example.android.stock2go
    /**
     * BASE_CONTENT_URI
     * Concatenate the CONTENT_AUTHORITY constant with the scheme “content://”
     * & create the BASE_CONTENT_URI which will be shared by every URI associated with StockContract:
     * "content://" + CONTENT_AUTHORITY
     * In order to make this a usable URI, use the parse method which takes in a URI string and returns a Uri.
     * <p>
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * PATH_TableName
     * This constants stores the path for each of the tables which will be appended to the base content URI.
     * <p>
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.stock2go/stock2go/ is a valid path for
     * looking at stock data. content://com.example.android.stock2go/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_STOCK = "stock2go";

    // To prevent someone from accidentally instantiating the contract class,
    // it has an empty constructor.
    private StockContract() {
    }

    /**
     * Inner class that defines constant values for the stock2go database table.
     * Each entry in the table represents a single stock item.
     */
    public static abstract class StockItemEntry implements BaseColumns {

        /**
         * CONTENT_URI
         * Lastly, inside each of the Entry classes in the contract, create a full URI for the class
         * as a constant called CONTENT_URI. The Uri.withAppendedPath() method appends the BASE_CONTENT_URI
         * (which contains the scheme and the content authority) to the path segment.
         * <p>
         * The content URI to access the stock data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCK);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of stock2go.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single stock item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;


        public static final String TABLE_NAME = "stock2go";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BRAND = "brand";
        public static final String COLUMN_SECTION = "section";
        public static final String COLUMN_PRICE = "price";

        /**
         * Possible values for the section of the stock2go (unknown, bread, cleaning materials, cosmetics, dairy products, dressings and sauces, electrical, frozen food, fruit, kitchen utensils, vegetables
         */
        public static final int SECTION_UNKNOWN = 0;
        public static final int SECTION_BREAD = 1;
        public static final int SECTION_CLEANING = 2;
        public static final int SECTION_COSMETICS = 3;
        public static final int SECTION_DAIRY = 4;
        public static final int SECTION_DRESSINGS_SAUCES = 5;
        public static final int SECTION_ELECTRICAL = 6;
        public static final int SECTION_FROZEN = 7;
        public static final int SECTION_FRUIT = 8;
        public static final int SECTION_KITCHEN_UTENSILS = 9;
        public static final int SECTION_VEGETABLES = 10;


        /**
         * Returns whether or not the given section is {@link #SECTION_UNKNOWN}, {@link #SECTION_FRUIT},
         * or {@link #SECTION_VEGETABLES}.
         */
        public static boolean isValidSection(int section) {
            return section == SECTION_UNKNOWN || section == SECTION_BREAD ||
                    section == SECTION_CLEANING || section == SECTION_COSMETICS ||
                    section == SECTION_DAIRY || section == SECTION_DRESSINGS_SAUCES ||
                    section == SECTION_ELECTRICAL || section == SECTION_FROZEN ||
                    section == SECTION_FRUIT || section == SECTION_KITCHEN_UTENSILS ||
                    section == SECTION_VEGETABLES;
        }

    }

}

