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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import eu.id2go.stock2go.data.StockContract.StockItemEntry;

import static eu.id2go.stock2go.data.StockProvider.LOG_TAG;

//import eu.id2go.stock2go.data.StockContract;

/**
 * Allows user to create a new stock item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the stock data loader
     */
    private static final int EXISTING_STOCK_LOADER = 0;

    /**
     * Identifier for read access
     */
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    /**
     * Identifier for image loader
     */
    static final int REQUEST_STOCK_IMAGE = 0;

//    name, brand, in stock, supplier, phone, e-mail, section, price, image

    /**
     * Instance variable
     * Content URI for the existing stock item loader
     */
    private Uri mCurrentStockItemUri;



    /**
     * EditText field to enter the stock item name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the stock item brand
     */
    private EditText mBrandEditText;

    /**
     * EditText field to enter the stock item Qty in stock
     */
    private EditText mStockQtyEditText;

    /**
     * EditText field to enter the Name of the Supplier
     */
    private EditText mNameSupplierEditText;

    /**
     * EditText field to enter the phone number of the Supplier
     */
    private EditText mPhoneSupplierEditText;

    /**
     * EditText field to enter the e-mail address of the Supplier
     */
    private EditText mEmailSupplierEditText;

    /**
     * EditText field to enter the stock item section
     */
    private Spinner mSectionSpinner;

    /**
     * EditText field to enter the stock item price
     */
    private EditText mPriceEditText;

    /**
     * ImageView field to show the stock item image
     */
    private ImageView mStockItemImageView;



    /**
     * Image buttons
     */
    ImageButton mGetStockItemImageBtn;

    Uri actualImageUri;

    ImageButton mDecreaseStockQty;
    ImageButton mIncreaseStockQty;

    /**
     * Section of the stock item. The possible valid values are in the StockContract.java file:
     * {@link StockItemEntry#SECTION_UNKNOWN}, {@link StockItemEntry#SECTION_FRUIT}, etc. or
     * {@link StockItemEntry#SECTION_VEGETABLES}.
     */
    private int mSection = StockItemEntry.SECTION_UNKNOWN;

    /**
     * Identifier for detecting changes in the editor stock item data fields
     */
    private boolean mStockItemHasChanged = false;


    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mStockItemHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mStockItemHasChanged = true;
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity in order to determine if we are
        // creating a new stock item or if we are editing an existing one.
        // Use getIntent() and getData() to get the associated URI
        Intent intent = getIntent();
        mCurrentStockItemUri = intent.getData();

        // If the intent does not contain a stock item URI, then we know that we are creating a new stock item.
        // Set title of EditorActivity on which situation we have
        // if the EditorActivity was opened using the ListView item, then we will
        // have a uri of the stock item to edit to change the app bar to say "Edit StockItem"
        // Otherwise this is a new stock item, the uri is null and so the app bar should change to say "Add a StockItem"
        if (mCurrentStockItemUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_stock_item));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a stock item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing stock item, so change app bar to say "Edit StockItem"
            setTitle(getString(R.string.editor_activity_title_edit_existing_stock_item));

            // Initialize a loader to read the stock item data from the database & display current values in editor
            getLoaderManager().initLoader(EXISTING_STOCK_LOADER, null, this);
        }


        // Assign all fields to relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_stock_item_name);
        mBrandEditText = findViewById(R.id.edit_stock_item_brand);
        mStockQtyEditText = findViewById(R.id.edit_stock_item_Qty);
        mNameSupplierEditText = findViewById(R.id.edit_stock_item_Supplier);
        mPhoneSupplierEditText = findViewById(R.id.edit_stock_item_supplier_phone);
        mEmailSupplierEditText = findViewById(R.id.edit_stock_item_supplier_email);
        mPriceEditText = findViewById(R.id.edit_stock_item_price);
        mSectionSpinner = findViewById(R.id.spinner_section);

        mStockItemImageView = findViewById(R.id.stock_item_image_view);

        mDecreaseStockQty = findViewById(R.id.stock_qty_minus);
        mIncreaseStockQty = findViewById(R.id.stock_qty_plus);
        mGetStockItemImageBtn = findViewById(R.id.get_image_button);

        // Attaching a TouchListener & Checking on changes in edit fields to avoid data loss by accidental closing
        mNameEditText.setOnTouchListener(mTouchListener);
        mBrandEditText.setOnTouchListener(mTouchListener);
        mStockQtyEditText.setOnTouchListener(mTouchListener);
        mNameSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneSupplierEditText.setOnTouchListener(mTouchListener);
        mEmailSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSectionSpinner.setOnTouchListener(mTouchListener);

        mStockItemImageView.setOnTouchListener(mTouchListener);

        mDecreaseStockQty.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                minOneFromStockQty();
                mStockItemHasChanged = true;
            }
        });

        mIncreaseStockQty.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                plusOneToStockQty();
                mStockItemHasChanged = true;
            }
        });

        mGetStockItemImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToOpenImageSelector();
                mStockItemHasChanged = true;
            }
        });




        setupSpinner();
    }


    /**
     * Setup the dropdown spinner that allows the user to select the section of the stock item
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter sectionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_section_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        sectionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSectionSpinner.setAdapter(sectionSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    // Here make reference to OuterClassContract.InnerClassEntry.CONSTANT (BlankContract.BlankEntry.CONSTANT)
                    // Due to import statement OuterClassContract.InnerClassEntry the outerclass StockContract. can be omitted
                    // (unknown, bread, cleaning materials, cosmetics, dairy products, dressings and
                    // sauces, electrical, frozen food, fruit, kitchen utensils, vegetables)
                    if (selection.equals(getString(R.string.section_bread))) {
                        mSection = StockItemEntry.SECTION_BREAD; // BREAD
                    } else if (selection.equals(getString(R.string.section_cleaning))) {
                        mSection = StockItemEntry.SECTION_CLEANING; // CLEANING
                    } else if (selection.equals(getString(R.string.section_cosmetics))) {
                        mSection = StockItemEntry.SECTION_COSMETICS; // COSMETICS
                    } else if (selection.equals(getString(R.string.section_dairy))) {
                        mSection = StockItemEntry.SECTION_DAIRY; // DAIRY
                    } else if (selection.equals(getString(R.string.section_dressings_sauces))) {
                        mSection = StockItemEntry.SECTION_DRESSINGS_SAUCES; // DRESSINGS & SAUCES
                    } else if (selection.equals(getString(R.string.section_electrical))) {
                        mSection = StockItemEntry.SECTION_ELECTRICAL; // ELECTRICAL
                    } else if (selection.equals(getString(R.string.section_frozen))) {
                        mSection = StockItemEntry.SECTION_FROZEN; //FROZEN
                    } else if (selection.equals(getString(R.string.section_fruit))) {
                        mSection = StockItemEntry.SECTION_FRUIT; // FRUIT
                    } else if (selection.equals(getString(R.string.section_vegetables))) {
                        mSection = StockItemEntry.SECTION_VEGETABLES; // VEGETABLES
                    } else if (selection.equals(getString(R.string.section_kitchen_utensils))) {
                        mSection = StockItemEntry.SECTION_KITCHEN_UTENSILS; // KITCHEN UTENSILS
                    } else {
                        mSection = StockItemEntry.SECTION_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSection = StockItemEntry.SECTION_UNKNOWN; // Unknown
            }
        });
    }

    // make new content value object, use key value pair where the key is the name column and the value is the name from the EditText field
    // converting a string into an integer use integer.parseInt method Integer.parseInt("1") -> 1. This will change strings into integers
    // Value for section is stored in mSection
    // name, brand, section, price

    /**
     * Get user input from editor and save stock item into database.
     */
    private void saveStockItem() {

        // Read input from EditText fields
        // To avoid polluted output from string use trim() to eliminate leading or trailing white space

        String nameString = mNameEditText.getText().toString().trim();
        String brandString = mBrandEditText.getText().toString().trim();
        String stockQtyString = mStockQtyEditText.getText().toString().trim();
        String nameSupplier = mNameSupplierEditText.getText().toString().trim();
        String phoneSupplier = mPhoneSupplierEditText.getText().toString().trim();
        String emailSupplier = mEmailSupplierEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String stockItemImage = mStockItemImageView.setImageURI();

        String imageUri = mGetStockItemImageBtn.toString();
        // Section is left out because of spinner pre-defined values


        if (mCurrentStockItemUri == null && (TextUtils.isEmpty(nameString) ||
                TextUtils.isEmpty(brandString) || TextUtils.isEmpty(stockQtyString) ||
                TextUtils.isEmpty(nameSupplier) || TextUtils.isEmpty(phoneSupplier) ||
                TextUtils.isEmpty(emailSupplier) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(stockItemImage) || TextUtils.isEmpty(imageUri))) {

            Toast.makeText(this, getString(R.string.toast_error_editor_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }

        //  Create a ContentValues object using key value pairs where the key is the name column and the value is the name from the EditText field
        ContentValues values = new ContentValues();
        values.put(StockItemEntry.COLUMN_NAME, nameString);
        values.put(StockItemEntry.COLUMN_BRAND, brandString);
        values.put(StockItemEntry.COLUMN_STOCK_QTY, stockQtyString);
        values.put(StockItemEntry.COLUMN_NAME_SUPPLIER, nameSupplier);
        values.put(StockItemEntry.COLUMN_PHONE_SUPPLIER, phoneSupplier);
        values.put(StockItemEntry.COLUMN_EMAIL_SUPPLIER, emailSupplier);
        values.put(StockItemEntry.COLUMN_SECTION, mSection);
        values.put(StockItemEntry.COLUMN_PRICE, price);
        values.put(StockItemEntry.COLUMN_IMAGE, imageUri);


        // Determine if this is a new or existing stock item by checking if mCurrentStockItemUri is null or not
        if (mCurrentStockItemUri == null) {
            // This is a NEW stock item, so insert a new stock item into the provider,
            // returning the content URI for the new stock item.
            Uri newUri = getContentResolver().insert(StockItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.toast_error_editor_inserting_stock_item_data), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.toast_success_editor_inserting_stock_item_data), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING stock item, so update the stock item with content URI: mCurrentStockItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentStockItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentStockItemUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.toast_error_editor_updating_stock_item_data),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.toast_success_editor_updating_stock_item_data),
                        Toast.LENGTH_SHORT).show();
            }

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new stock item, hide the "Delete" menu item.
        if (mCurrentStockItemUri == null) {
            Log.e(LOG_TAG, getString(R.string.log_prepared_options_menu));
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            deleteMenuItem.setVisible(false);
            orderMenuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save entries to database
                saveStockItem();
                // exit activity
                finish();
                return true;

            // Respond showing a dialog with phone & e-mail
            case R.id.action_order:
                showOrderConfirmationDialog();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the stock item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mStockItemHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);

                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                Log.e(LOG_TAG, getString(R.string.log_unsaved_changes_dialog));

                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the stock item hasn't changed, continue with handling back button press
        if (!mStockItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void minOneFromStockQty() {
        String changedValuesString = mStockQtyEditText.getText().toString();
        int changedValue;
        if (changedValuesString.isEmpty()) {
            return;
        } else if (changedValuesString.equals("0")) {
            return;
        } else {
            changedValue = Integer.parseInt(changedValuesString);
            mStockQtyEditText.setText(String.valueOf(changedValue - 1));
        }
    }

    private void plusOneToStockQty() {
        String changedValueString = mStockQtyEditText.getText().toString();
        int changedValue;
        if (changedValueString.isEmpty()) {
            changedValue = 0;
        } else {
            changedValue = Integer.parseInt(changedValueString);
        }
        mStockQtyEditText.setText(String.valueOf(changedValue + 1));
    }

    private void showOrderConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.action_order_confirmation));
        builder.setPositiveButton(R.string.phone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Intent to make a phone call
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mPhoneSupplierEditText.getText().toString().trim()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton(R.string.email, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Intent to write an e-mail
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + mEmailSupplierEditText.getText().toString().trim()));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.recurring_order_title)
                        + " " + mNameEditText.getText().toString().trim() + ", " +
                        getString(R.string.recurring_order_title_brand) + " " +
                        mBrandEditText.getText().toString().trim());
                String bodyMessage = getString(R.string.recurring_order_txt_body) + " " +
                        mNameEditText.getText().toString().trim() + ", " +
                        getString(R.string.recurring_order_txt_brand) + " " +
                        mBrandEditText.getText().toString().trim();
                intent.putExtra(Intent.EXTRA_TEXT, bodyMessage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"), REQUEST_STOCK_IMAGE);
    }

    @Override
    public void onRequestPermissionResult(int requestCode,
                                          String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    openImageSelector();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == REQUEST_STOCK_IMAGE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultCode != null) {

            }

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
        // Use the {@link StockItemEntry#CONTENT_URI} to access the stock item data.
        return new CursorLoader(this,
                mCurrentStockItemUri,    // The Content URI of the stock2go Table of the db to query
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Exit early if the cursor == null or there is < 1 row
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed moving to the first row of the cursor & reading its data
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of the stock table attributes (the header)
            int nameColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_NAME);
            int brandColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_BRAND);
            int stockQtyColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_STOCK_QTY);
            int nameSupplierColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_NAME_SUPPLIER);
            int phoneSupplierColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_PHONE_SUPPLIER);
            int emailSupplierColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_EMAIL_SUPPLIER);
            int sectionColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_SECTION);
            int priceColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(StockItemEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String brand = cursor.getString(brandColumnIndex);
            int stockQty = cursor.getInt(stockQtyColumnIndex);
            String nameSupplier = cursor.getString(nameSupplierColumnIndex);
            String phoneSupplier = cursor.getString(phoneSupplierColumnIndex);
            String emailSupplier = cursor.getString(emailSupplierColumnIndex);
            int section = cursor.getInt(sectionColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            mNameEditText.setText(name);
            mBrandEditText.setText(brand);
            mStockQtyEditText.setText(Integer.toString(stockQty));
            mNameSupplierEditText.setText(nameSupplier);
            mPhoneSupplierEditText.setText(phoneSupplier);
            mEmailSupplierEditText.setText(emailSupplier);
            mPriceEditText.setText(Integer.toString(price));
            mStockItemImageView.setImageURI(image);

            // Section is a dropdown spinner, so ma the constant value from the database
            // into one of the dropdown options (0 == Unknown, 1 == FRUIT, 2 == VEGETABLES).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            // unknown, bread, cleaning materials, cosmetics, dairy products, dressings and sauces,
            // electrical, frozen food, fruit, kitchen utensils, vegetables)
            switch (section) {

                case StockItemEntry.SECTION_BREAD:
                    mSectionSpinner.setSelection(1);
                    break;
                case StockItemEntry.SECTION_CLEANING:
                    mSectionSpinner.setSelection(2);
                    break;
                case StockItemEntry.SECTION_COSMETICS:
                    mSectionSpinner.setSelection(3);
                    break;
                case StockItemEntry.SECTION_DAIRY:
                    mSectionSpinner.setSelection(4);
                    break;
                case StockItemEntry.SECTION_DRESSINGS_SAUCES:
                    mSectionSpinner.setSelection(5);
                    break;
                case StockItemEntry.SECTION_ELECTRICAL:
                    mSectionSpinner.setSelection(6);
                    break;
                case StockItemEntry.SECTION_FROZEN:
                    mSectionSpinner.setSelection(7);
                    break;
                case StockItemEntry.SECTION_FRUIT:
                    mSectionSpinner.setSelection(8);
                    break;
                case StockItemEntry.SECTION_KITCHEN_UTENSILS:
                    mSectionSpinner.setSelection(9);
                    break;
                case StockItemEntry.SECTION_VEGETABLES:
                    mSectionSpinner.setSelection(10);
                    break;
                default: // This is the situation that the spinner is in by default
                    mSectionSpinner.setSelection(0);
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
        mBrandEditText.setText("");
        mStockQtyEditText.setText("");
        mNameSupplierEditText.setText("");
        mPhoneSupplierEditText.setText("");
        mEmailSupplierEditText.setText("");
        mPriceEditText.setText("");
        mStockItemImageView.setImageURI();
//        mStockItemImageView.setImageURI();
        mSectionSpinner.setSelection(0); // By default, set section to "Unknown"
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the stock item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the stock item.
                deleteStockItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the stock item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the stock item in the database.
     */
    private void deleteStockItem() {
        if (mCurrentStockItemUri != null) {
            // Call on ContentResolver to delete selected row of stock item data.
            // Passing null for where & args as the mCurrentStockItemUri is configured to handle these
            int rowsDeleted = getContentResolver().delete(mCurrentStockItemUri, null, null);
            // In case of failure
            if (rowsDeleted == 0) {
                // Show message depending of success or failure of deleting
                Toast.makeText(EditorActivity.this, R.string.editor_delete_stock_item_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete instruction was successful.
                // Show toast success
                Toast.makeText(EditorActivity.this, R.string.editor_delete_stock_item_successful, Toast.LENGTH_SHORT).show();

            }
        }
        // Close the activity
        finish();
    }

}