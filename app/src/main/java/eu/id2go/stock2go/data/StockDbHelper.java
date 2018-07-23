package eu.id2go.stock2go.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import eu.id2go.stock2go.data.StockContract.StockItemEntry;

// Reflection on SQLiteOpenHelper class, what should it do?
// 1 Create a SQLite database when first accessed
// 2 Connecting to created database
// 3 Managing updating database schema if version changes

/**
 * StockDbHelper class should extend the SQLiteOpenHelper
 */

public class StockDbHelper extends SQLiteOpenHelper {


    /**
     * Constants for the database name and database version
     * If the database schema changes (i.e. when adding columns for extra data),
     * than increment the database version number!
     */
    private static final String DATABASE_NAME = "shelter.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Construct a new instance of StockDbHelper
     *
     * @param context of the app
     */
    public StockDbHelper(Context context) {
        // database name, cursor factory is set to null to use the default setting, database version number
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Because of subClassing the abstract class (SQLiteOpenHelper) we need to implement the (onCreate() method & onUpgrade() method
     *
     * @param @{onCreate()}  method - this method is for when the database is first created
     * @param @{onUpgrade()} method - this method is for when the database schema of the database changes (ex: adding a different column)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE stock2go (_id, INTEGER PRIMARY KEY (add the AUTOINCREMENT to automatically increment new unique _id numbers)
        // Create a String that contains the SQL statement to create the stock2go table
        String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + StockItemEntry.TABLE_NAME + "("
                + StockItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StockItemEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + StockItemEntry.COLUMN_BRAND + " TEXT NOT NULL, "
                + StockItemEntry.COLUMN_STOCK_QTY + " INTEGER NOT NULL, "
                + StockItemEntry.COLUMN_NAME_SUPPLIER + " TEXT, "
                + StockItemEntry.COLUMN_PHONE_SUPPLIER + " INTEGER NOT NULL, "
                + StockItemEntry.COLUMN_EMAIL_SUPPLIER + " TEXT NOT NULL, "
                + StockItemEntry.COLUMN_SECTION + " INTEGER NOT NULL, "
                + StockItemEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0);";

        // To execute the SQL statement
        db.execSQL(SQL_CREATE_STOCK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still @ version 1, so command below is not needed yet
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + StockItemEntry.TABLE_NAME;

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);

    }
//    This method should be called into action when downgrading the database version
//    @Override
//    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        onUpgrade(db, oldVersion, newVersion);
//    }

}
