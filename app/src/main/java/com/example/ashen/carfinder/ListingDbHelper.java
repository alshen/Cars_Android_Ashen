package com.example.ashen.carfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for managing out database
 */
public class ListingDbHelper extends SQLiteOpenHelper {

    // database constants
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Listings.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public ListingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates a new table in our database
     * @param db the database to write too
     *
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ListingContract.ListingEntry.TABLE_NAME + " (" +
                        ListingContract.ListingEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_UUID + TEXT_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_MAKE + TEXT_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_MODEL + TEXT_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_IMAGE + TEXT_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_YEAR + INTEGER_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_ASKING_PRICE + INTEGER_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_STANDARD_PRICE + INTEGER_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_BEST + INTEGER_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_WORST + INTEGER_TYPE + COMMA_SEP +
                        ListingContract.ListingEntry.KEY_STARRED + INTEGER_TYPE + COMMA_SEP +
                        " )";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Called when the database version is changed, this version is changed anytime
     * the database schema is changed
     * @param db the database to write too
     * @param oldVersion old database version
     * @param newVersion new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        removeAllCarListings();
        onCreate(db);
    }

    /**
     * Removes the listings table from the database, @see clear()
     */
    public void removeAllCarListings() {
        SQLiteDatabase db = getWritableDatabase();
        final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ListingContract.ListingEntry.TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        db.close();
    }

    /**
     * Removes all entries from the listings table
     */
    public void clear() {
        SQLiteDatabase db = getWritableDatabase();
        removeAllCarListings();
        onCreate(db);
        db.close();
    }

    /**
     * Inserts a new CarListing into the database
     *
     * @param carListing the new entry to be inserted
     * @return the _Id of the table row
     */
    public long addCarListing(CarListing carListing) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ListingContract.ListingEntry.KEY_UUID, carListing.getUuid());
        values.put(ListingContract.ListingEntry.KEY_MAKE, carListing.getMake());
        values.put(ListingContract.ListingEntry.KEY_MODEL, carListing.getModel());
        values.put(ListingContract.ListingEntry.KEY_IMAGE, carListing.getImage());
        values.put(ListingContract.ListingEntry.KEY_DESCRIPTION, carListing.getDescription());
        values.put(ListingContract.ListingEntry.KEY_YEAR, carListing.getYear());
        values.put(ListingContract.ListingEntry.KEY_ASKING_PRICE, carListing.getAskingPrice());
        values.put(ListingContract.ListingEntry.KEY_STANDARD_PRICE, carListing.getStandardPrice());

        // Since the Sqlite database does not accept boolean values, we store them as ints
        values.put(ListingContract.ListingEntry.KEY_BEST, carListing.isBestInYear()? 1 : 0);
        values.put(ListingContract.ListingEntry.KEY_WORST, carListing.isWorstInYear()? 1 : 0);
        values.put(ListingContract.ListingEntry.KEY_STARRED, carListing.isStarred()? 1 : 0);

        long id = db.insert(
                ListingContract.ListingEntry.TABLE_NAME,
                null,
                values);
        db.close();
        return id;
    }

    /**
     * Removes the given car listing from the database based on its uuid
     * @param uuid the uuid of the entry to remove
     */
    public void removeCarListing(String uuid) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(
                ListingContract.ListingEntry.TABLE_NAME,
                ListingContract.ListingEntry.KEY_UUID + "=" + uuid,
                null);
        db.close();
    }

    /**
     * Retrieves a car listing from the database base on its uuid
     * @param uuid the uuid of the car listing
     * @return the car listing
     */
    public CarListing getCarListing(String uuid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                ListingContract.ListingEntry.TABLE_NAME,
                new String[] {
                    ListingContract.ListingEntry.KEY_UUID,
                    ListingContract.ListingEntry.KEY_MAKE,
                    ListingContract.ListingEntry.KEY_MODEL,
                    ListingContract.ListingEntry.KEY_IMAGE,
                    ListingContract.ListingEntry.KEY_DESCRIPTION,
                    ListingContract.ListingEntry.KEY_YEAR,
                    ListingContract.ListingEntry.KEY_ASKING_PRICE,
                    ListingContract.ListingEntry.KEY_STANDARD_PRICE,
                    ListingContract.ListingEntry.KEY_BEST,
                    ListingContract.ListingEntry.KEY_WORST,
                    ListingContract.ListingEntry.KEY_STARRED,
                },
                ListingContract.ListingEntry.KEY_UUID + "=?",
                new String[] { uuid },
                null,
                null,
                null,
                null);

        if (cursor != null) cursor.moveToFirst();
        else return null;

        CarListing carListing = new CarListing(
            // TODO: improve
            // we can guess the column numbers, but they are retrieved explicitly
            cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_UUID)),
            cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MAKE)),
            cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MODEL)),
            cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_IMAGE)),
            cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_DESCRIPTION)),
            cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_YEAR)),
            cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_ASKING_PRICE)),
            cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STANDARD_PRICE)),
            // since the database is storing boolean values as ints we convert it back to boolean
            // true == 1, false == 0
            cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_BEST)) == 1,
            cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_WORST)) == 1,
            cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STARRED)) == 1
                );
        db.close();
        return carListing;
    }

    /**
     * Retrieves all the CarListings stored in the database
     * @return List containing all the CarListings
     */
    public List<CarListing> getAllCarListings() {
        final String SQL_SELECT_ALL_ENTRIES_QUERY =
                "SELECT *," + " (" + ListingContract.ListingEntry.KEY_ASKING_PRICE + "-" +
                        ListingContract.ListingEntry.KEY_STANDARD_PRICE + ") AS \'ranking\'" + "FROM "
                        + ListingContract.ListingEntry.TABLE_NAME +
                " ORDER BY \'ranking\' ASC";
        List<CarListing> carListings = new ArrayList<CarListing>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_ALL_ENTRIES_QUERY, null);
        while (cursor.moveToNext()) {
            CarListing carListing = new CarListing(
                // TODO: improve
                // we can guess the column numbers, but they are retrieved explicitly
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_UUID)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MAKE)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MODEL)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_IMAGE)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_YEAR)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_ASKING_PRICE)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STANDARD_PRICE)),
                // since the database is storing boolean values as ints we convert it back to boolean
                // true == 1, false == 0
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_BEST)) == 1,
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_WORST)) == 1,
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STARRED)) == 1
            );
            carListings.add(carListing);
        }
        cursor.close();
        db.close();
        return carListings;
    }

    /**
     * Returns all the CarListings that have been starred by the user
     * @return List containing all starred listings
     */
    public List<CarListing> getFavoriteCarListings() {
        final String SQL_SELECT_FAVORITE_ENTRIES_QUERY =
                "SELECT *," + " (" + ListingContract.ListingEntry.KEY_ASKING_PRICE + "-" +
                        ListingContract.ListingEntry.KEY_STANDARD_PRICE + ") AS \'ranking\'" +
                        " FROM " + ListingContract.ListingEntry.TABLE_NAME +
                        " WHERE " + ListingContract.ListingEntry.KEY_STARRED + " = 1"  +
                        " ORDER BY \'ranking' ASC";
        List<CarListing> carListings = new ArrayList<CarListing>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_FAVORITE_ENTRIES_QUERY, null);
        while (cursor.moveToNext()) {
            CarListing carListing = new CarListing(
                // TODO: improve
                // we can guess the column numbers, but they are retrieved explicitly
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_UUID)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MAKE)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MODEL)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_IMAGE)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_YEAR)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_ASKING_PRICE)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STANDARD_PRICE)),
                // since the database is storing boolean values as ints we convert it back to boolean
                // true == 1, false == 0
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_BEST)) == 1,
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_WORST)) == 1,
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STARRED)) == 1
            );
            carListings.add(carListing);
        }
        cursor.close();
        db.close();
        return carListings;
    }

    /**
     * @return a list containing all distinct makes
     */
    public List<String> getAllMakes() {
        final String SQL_SELECT_ALL_MAKES_QUERY =
                "SELECT DISTINCT " + ListingContract.ListingEntry.KEY_MAKE +
                        " FROM " + ListingContract.ListingEntry.TABLE_NAME;
        List<String> makes = new ArrayList<String>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_ALL_MAKES_QUERY, null);
        while (cursor.moveToNext()) {
            makes.add(cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MAKE)));
        }
        cursor.close();
        db.close();
        return makes;
    }

    /**
     * Retrieves all CarListing models for a given make
     * @param make the make the models are based off
     * @return a Listing containing all distinct models for a given make
     */
    public List<String> getAllModels(String make) {
        final String SQL_SELECT_ALL_MODELS_QUERY =
                "SELECT DISTINCT " + ListingContract.ListingEntry.KEY_MODEL +
                        " FROM " + ListingContract.ListingEntry.TABLE_NAME +
                        " WHERE " + ListingContract.ListingEntry.KEY_MAKE + "=\'" + make + "\'";
        List<String> models = new ArrayList<String>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_ALL_MODELS_QUERY, null);
        while (cursor.moveToNext()) {
            models.add(cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MODEL)));
        }
        cursor.close();
        db.close();
        return models;
    }

    /**
     * Quries for CarListings fitting the provided criteria
     * @param make the make of the used car
     * @param model the model of the used car
     * @param minPrice the minimum price of the used car
     * @param maxPrice the maximum price of the used car
     * @return a list containing all the CarListings fitting the criteria
     */
    public List<CarListing> getCarListings(String make, String model, int minPrice, int maxPrice) {
        // TODO: BUG this query does not handle the ALL case for makes/models
        String SQL_SELECT_CAR_LISTINGS_QUERY;
        if (make.equals("ALL")) {
            // if make == All then model must also == ALL
            SQL_SELECT_CAR_LISTINGS_QUERY =
                    "SELECT *," + " (" + ListingContract.ListingEntry.KEY_ASKING_PRICE + "-" +
                        ListingContract.ListingEntry.KEY_STANDARD_PRICE + ") AS \'ranking\'" +
                            " FROM " + ListingContract.ListingEntry.TABLE_NAME +
                            " WHERE " + ListingContract.ListingEntry.KEY_ASKING_PRICE +
                            " BETWEEN " + minPrice + " AND " + maxPrice +
                            " ORDER BY \'ranking' ASC";
        } else if (model.equals("ALL")) {
            SQL_SELECT_CAR_LISTINGS_QUERY =
                    "SELECT *," + " (" + ListingContract.ListingEntry.KEY_ASKING_PRICE + "-" +
                            ListingContract.ListingEntry.KEY_STANDARD_PRICE + ") AS \'ranking\'" +
                            " FROM " + ListingContract.ListingEntry.TABLE_NAME +
                            " WHERE " + ListingContract.ListingEntry.KEY_MAKE + " = \'" + make + "\'" +
                            " AND " + ListingContract.ListingEntry.KEY_ASKING_PRICE +
                            " BETWEEN " + minPrice + " AND " + maxPrice +
                            " ORDER BY \'ranking' ASC";
        } else {
            SQL_SELECT_CAR_LISTINGS_QUERY =
                    "SELECT *," + " (" + ListingContract.ListingEntry.KEY_ASKING_PRICE + "-" +
                            ListingContract.ListingEntry.KEY_STANDARD_PRICE + ") AS \'ranking\'" +
                            " FROM " + ListingContract.ListingEntry.TABLE_NAME +
                            " WHERE " + ListingContract.ListingEntry.KEY_MAKE + " = \'" + make + "\'" +
                            " AND " + ListingContract.ListingEntry.KEY_MODEL + " = \'" + model + "\'" +
                            " AND " + ListingContract.ListingEntry.KEY_ASKING_PRICE +
                            " BETWEEN " + minPrice + " AND " + maxPrice +
                            " ORDER BY \'ranking' ASC";
        }
        List<CarListing> carListings = new ArrayList<CarListing>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_CAR_LISTINGS_QUERY, null);
        while (cursor.moveToNext()) {
            CarListing carListing = new CarListing(
                // TODO: improve
                // we can guess the column numbers, but they are retrieved explicitly
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_UUID)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MAKE)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_MODEL)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_IMAGE)),
                cursor.getString(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_YEAR)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_ASKING_PRICE)),
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STANDARD_PRICE)),
                // since the database is storing boolean values as ints we convert it back to boolean
                // true == 1, false == 0
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_BEST)) == 1,
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_WORST)) == 1,
                cursor.getInt(cursor.getColumnIndex(ListingContract.ListingEntry.KEY_STARRED)) == 1
            );
            carListings.add(carListing);
        }
        cursor.close();
        db.close();
        return carListings;
    }

    /**
     * Updates the starred value of a CarListing
     * @param uuid uuid of the CarListing to update
     * @param starred whether the CarListing is starred
     */
    public void updateCarListingStarred(String uuid, boolean starred) {
        SQLiteDatabase db = getWritableDatabase();
        final String SQL_TOGGLE_FAVORITE =
                "UPDATE " + ListingContract.ListingEntry.TABLE_NAME +
                        " SET " + ListingContract.ListingEntry.KEY_STARRED +
                        " = " + ((starred)? 1 : 0) + " WHERE " + ListingContract.ListingEntry.KEY_UUID +
                        "=\'" + uuid +"\'";
        db.execSQL(SQL_TOGGLE_FAVORITE);
        db.close();
    }

    public void updateCarListingStandardPrice(String uuid, int standardPrice) {
        SQLiteDatabase db = getWritableDatabase();
        final String SQL_UPDATE_STANDARD_PRICE =
                "UPDATE " + ListingContract.ListingEntry.TABLE_NAME +
                        " SET " + ListingContract.ListingEntry.KEY_STANDARD_PRICE +
                        " = " + standardPrice + " WHERE " + ListingContract.ListingEntry.KEY_UUID +
                        "=\'" + uuid +"\'";
        db.execSQL(SQL_UPDATE_STANDARD_PRICE);
        Log.e("Updated", "UUID: " + uuid + " price: " + standardPrice + "\n" + SQL_UPDATE_STANDARD_PRICE);
        db.close();
    }

    /*public void updateCarListingXInYear(String uuid, boolean best, ) {
        SQLiteDatabase db = getReadableDatabase();
        final String SQL_UPDATE_STANDARD_PRICE =
                "UPDATE " + ListingContract.ListingEntry.TABLE_NAME +
                        " SET " + ListingContract.ListingEntry.KEY_STANDARD_PRICE +
                        " = " + standardPrice + " WHERE " + ListingContract.ListingEntry.KEY_UUID +
                        "=\'" + uuid +"\'";
        db.execSQL(SQL_UPDATE_STANDARD_PRICE);
        db.close();
    }*/
}
