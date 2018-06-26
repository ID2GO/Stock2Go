package eu.id2go.pets.data;

import android.provider.BaseColumns;

/**
 * @Contracts exist of 3 parts:
 * Outer class named: BlankContract
 * Inner class named: BlankEntry that implements BaseColumns for each table in the database
 * String constants for each of the headings in the database
 */
public final class PetContract {

    public static abstract class PetsEntry implements BaseColumns {

        public static final String TABLE_NAME = "pets";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BREED = "breed";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_weight = "weight";

        /**
         * Possible values for the gender of the pets
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}
