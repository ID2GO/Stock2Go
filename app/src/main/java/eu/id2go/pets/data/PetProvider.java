package eu.id2go.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import eu.id2go.pets.data.PetContract.PetsEntry;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PETS = 100;
    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int PET_ID = 101;
//    private static final int PET_NAME = 102;
//    private static final int PET_BREED = 103;
//    private static final int PET_GENDER = 104;
//    private static final int PET_WEIGHT = 105;


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
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS); // sUriMatcher.addURI("pets", "pets", PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID); // sUriMatcher.addURI("pets", "pets/#", PET_ID);
//        sUriMatcher.addURI("pets", "pets/#", PET_NAME);     // sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS +"/#", PET_NAME);
//        sUriMatcher.addURI("pets", "pets/#", PET_BREED);    // sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS +"/#", PET_BREED);
//        sUriMatcher.addURI("pets", "pets/#", PET_GENDER);   // sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS +"/#", PET_GENDER);
//        sUriMatcher.addURI("pets", "pets/#", PET_WEIGHT);   // sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS +"/#", PET_WEIGHT);

    }

    /**
     * Database helper object
     * Creating and initializing a PetDbHelper object secures access to the pets database.
     */
    private PetDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // The variable is to be a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new PetDbHelper(getContext());
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
            case PETS:
                // For the Pets code, query the pets table directly with the given projection,
                // selection, selection arguments, and sort order.
                // The cursor could contain multiple rows of the pets table.
                cursor = database.query(PetsEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the _ID from the URI.
                // For an example URI such as " content://com.example.android.pets/pets/3",
                // the selection will be " _id=?" and the selection argument wil be a
                // String array containing the actual _ID of 3 in this case.
                //
                //For every "?" in the selection, we need to have an element in the selection arguments
                // that will fill in the "?". Since we have 1 question mark in the selection,
                // we hae 1 string in the selection arguments String array.
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing the selected row of data of the table.
                cursor = database.query(PetsEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    /**
     * Returns the MIME type of data corresponding to the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
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
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(PetsEntry.COLUMN_NAME);
        // check breed is not null
        String breed = values.getAsString(PetsEntry.COLUMN_BREED);
        // check gender if it is null or invalid
        Integer gender = values.getAsInteger(PetsEntry.COLUMN_GENDER);
        // check weight to be equal or greater than 0 kg
        Integer weight = values.getAsInteger(PetsEntry.COLUMN_WEIGHT);


        // check name
        // using TextUtils.isEmpty(name) {} instead of using (name==null || name.isEmpty() ){}
        // It's faster and will return true if the String is empty or null.
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Pet requires a name");
//            Toast.makeText(this, getString(R.string.toast_insert_pet_name),Toast.LENGTH_SHORT).show();
        }
        // check breed
        if (breed == null || breed.isEmpty()) {
            throw new IllegalArgumentException("Pet requires valid breed");
//            Toast.makeText(this, getString(R.string.toast_insert_pet_breed),Toast.LENGTH_SHORT).show();
        }
        // check gender with either/or check
        if (gender == null || !PetsEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        // check weight checking both conditions with &&
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long newRowId = db.insert(PetsEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);

            return null;
        }

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
        return 0;
    }

    /**
     * Delete the rows of data at the given selection and selection arguments.
     * Use the arguments to select the table and the rows to delete. Return the number of rows deleted.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }


}