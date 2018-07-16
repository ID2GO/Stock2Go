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
package eu.id2go.pets;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import eu.id2go.pets.data.PetContract.PetsEntry;

//import eu.id2go.pets.data.PetContract;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_PET_LOADER = 0;

    /**
     * Instance variable
     * Content URI for the existing pet loader
     */
    private Uri mCurrentPetUri;



    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetsEntry.GENDER_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity in order to determine if we are
        // creating a new pet or if we are editing an existing one.
        // Use getIntent() and getData() to get the associated URI
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        // If the intent does not contain a pet URI, then we know that we are creating a new pet.
        // Set title of EditorActivity on which situation we have
        // if the EditorActivity was opened using the ListView item, then we will
        // have a uri of the pet to edit to change the app bar to say "Edit Pet"
        // Otherwise this is a new pet, the uri is null and so the app bar should change to say "Add a Pet"
        if (mCurrentPetUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_existing_pet));
        }

        // Initialize a loader to read the pet data from the database & display current values in editor
        getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name);
        mBreedEditText = findViewById(R.id.edit_pet_breed);
        mWeightEditText = findViewById(R.id.edit_pet_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    // Here make reference to OuterClassContract.InnerClassEntry.CONSTANT (BlankContract.BlankEntry.CONSTANT)
                    // Due to import statement OuterClassContract.InnerClassEntry the outerclass PetContract. can be omitted
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetsEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }

    // make new content value object, use key value pair where the key is the name column and the value is the name from the EditText field
    // converting a string into an integer use integer.parseInt method Integer.parseInt("1") -> 1. This will change strings into integers
    // Value for gender is stored in mGender
    // name, breed, gender, weight

    /**
     * Get user input from editor and save new pet into database.
     */
    private void insertPet() {
        // Read input from EditText fields
        // To avoid polluted output from string @.trim() to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weight = Integer.parseInt(weightString);



        //  Create a ContentValues object using key value pairs where the key is the name column and the value is the name from the EditText field
        ContentValues values = new ContentValues();
        values.put(PetsEntry.COLUMN_NAME, nameString);
        values.put(PetsEntry.COLUMN_BREED, breedString);
        values.put(PetsEntry.COLUMN_GENDER, mGender);
        values.put(PetsEntry.COLUMN_WEIGHT, weight);

        // Insert a new row in the pets database returning the id of that new row
        Uri newUri = getContentResolver().insert(PetsEntry.CONTENT_URI, values);

        // Show a toast message of either success saving or error saving
        if (newUri == null) {
            // If the row ID is -1, then saving resulted in an error
            Toast.makeText(this, getString(R.string.toast_error_editor_inserting_pet_data), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise saving was successful
            Toast.makeText(this, getString(R.string.toast_success_editor_inserting_pet_data), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save entries to database
                insertPet();
                // exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define the range of columns from the database to be used
        String[] projection = {
                PetsEntry._ID,
                PetsEntry.COLUMN_NAME,
                PetsEntry.COLUMN_BREED,
                PetsEntry.COLUMN_GENDER,
                PetsEntry.COLUMN_WEIGHT};

        // Perform a query on the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to access the pet data.
        return new CursorLoader(this,
                mCurrentPetUri,    // The Content URI of the pets Table of the db to query
                projection,               // The above range of columns to return for each row
                null,            // The column for the WHERE query
                null,         // Selection criteria
                null);           // The sort order for the returned rows
    }

    /**
     * Called when a previously created loader has finished its load.
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter (Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param cursor The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        // Exit early if the cursor == null or there is < 1 row
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed moving to the first row of the cursor & reading its data
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of the pet table attributes (the header)
            int nameColumnIndex = cursor.getColumnIndex(PetsEntry.COLUMN_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetsEntry.COLUMN_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetsEntry.COLUMN_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetsEntry.COLUMN_WEIGHT);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            // Gender is a dropdown spinner, so ma the constant value from the database
            // into one of the dropdown options (0 == Unknown, 1 == Male, 2 == Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (gender) {

                case PetsEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetsEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                default: // This is the situation that the spinner is in by default
                    mGenderSpinner.setSelection(0);
                    break;

            }
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0); // By default, set gender to "Unknown"
    }
}