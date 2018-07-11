package eu.id2go.pets.data;

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
public final class PetContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.
     * A convenient string to use for the content authority is the package name for the app, which
     * is guaranteed to be unique on the device.
     */
    public static final String CONTENT_AUTHORITY = "eu.id2go.pets"; // ...id2go... in stead of: com.example.android.pets
    /**
     * BASE_CONTENT_URI
     * Concatenate the CONTENT_AUTHORITY constant with the scheme “content://”
     * & create the BASE_CONTENT_URI which will be shared by every URI associated with PetContract:
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
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PETS = "pets";

    private PetContract() {
    }

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    public static abstract class PetsEntry implements BaseColumns {

        /**
         * CONTENT_URI
         * Lastly, inside each of the Entry classes in the contract, create a full URI for the class
         * as a constant called CONTENT_URI. The Uri.withAppendedPath() method appends the BASE_CONTENT_URI
         * (which contains the scheme and the content authority) to the path segment.
         * <p>
         * The content URI to access the pet data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);


        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BREED = "breed";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_WEIGHT = "weight";

        /**
         * Possible values for the gender of the pets
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /**
         * Returns whether or not the given gender is {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         */
        public static boolean isValidGender(int gender) {
            return gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE;
        }

    }

}

