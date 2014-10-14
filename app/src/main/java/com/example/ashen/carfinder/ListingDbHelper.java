package com.example.ashen.carfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ashen.carfinder.ListingContract.ListingEntry;

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

    // query constants
    private static final String[] PROJECTION_CAR_LISTING = {
        ListingEntry.KEY_UUID,
        ListingEntry.KEY_MAKE,
        ListingEntry.KEY_MODEL,
        ListingEntry.KEY_IMAGE,
        ListingEntry.KEY_DESCRIPTION,
        ListingEntry.KEY_YEAR,
        ListingEntry.KEY_ASKING_PRICE,
        ListingEntry.KEY_STANDARD_PRICE,
        ListingEntry.KEY_BEST,
        ListingEntry.KEY_WORST,
        ListingEntry.KEY_STARRED,
        // we rank by Asking Price - Standard Price so we alias this to RANKING
        "( " + ListingEntry.KEY_ASKING_PRICE + "-" +
                ListingEntry.KEY_STANDARD_PRICE + " ) AS RANKING"
    };

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
        // CREATE TABLE Listings ( _id INTEGER PRIMARY KEY, Uuid TEXT, Make TEXT, Model TEXT,
        //                      Image TEXT, Description TEXT, Year INTEGER, APrice INTEGER,
        //                      SPrice INTEGER, IsBest INTEGER, IsWorst INTEGER, IsStarred INTEGER )

        final String SQL_CREATE_TABLE =
                "CREATE TABLE " + ListingEntry.TABLE_NAME + " (" +
                    ListingEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    ListingEntry.KEY_UUID + TEXT_TYPE + COMMA_SEP +
                    ListingEntry.KEY_MAKE + TEXT_TYPE + COMMA_SEP +
                    ListingEntry.KEY_MODEL + TEXT_TYPE + COMMA_SEP +
                    ListingEntry.KEY_IMAGE + TEXT_TYPE + COMMA_SEP +
                    ListingEntry.KEY_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    ListingEntry.KEY_YEAR + INTEGER_TYPE + COMMA_SEP +
                    ListingEntry.KEY_ASKING_PRICE + INTEGER_TYPE + COMMA_SEP +
                    ListingEntry.KEY_STANDARD_PRICE + INTEGER_TYPE + COMMA_SEP +
                    ListingEntry.KEY_BEST + INTEGER_TYPE + COMMA_SEP +
                    ListingEntry.KEY_WORST + INTEGER_TYPE + COMMA_SEP +
                    ListingEntry.KEY_STARRED + INTEGER_TYPE +
                    " )";

        db.execSQL(SQL_CREATE_TABLE);
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
     * Removes the listings table from the database
     */
    public void removeAllCarListings() {
        // DROP TABLE IF EXISTS Listings

        SQLiteDatabase db = getWritableDatabase();
        final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ListingEntry.TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);

        db.close();
    }

    private CarListing getCarListing(Cursor cursor) {
        if (cursor != null) {
            cursor.moveToNext();
            CarListing carListing = new CarListing(
                getString(cursor, ListingEntry.KEY_UUID),
                getString(cursor, ListingEntry.KEY_MAKE),
                getString(cursor, ListingEntry.KEY_MODEL),
                getString(cursor, ListingEntry.KEY_IMAGE),
                getString(cursor, ListingEntry.KEY_DESCRIPTION),
                getInt(cursor, ListingEntry.KEY_YEAR),
                getInt(cursor, ListingEntry.KEY_ASKING_PRICE),
                getInt(cursor, ListingEntry.KEY_STANDARD_PRICE),
                // since the database is storing boolean values as ints we convert it back to boolean
                // true == 1, false == 0
                getInt(cursor, ListingEntry.KEY_BEST) == 1,
                getInt(cursor, ListingEntry.KEY_WORST) == 1,
                getInt(cursor, ListingEntry.KEY_STARRED) == 1
            );
            return carListing;
        }
        return null;
    }

    private List<CarListing> getCarListings(Cursor cursor) {
        List<CarListing> carListings = new ArrayList<CarListing>();
        while (cursor.moveToNext()) {
            CarListing carListing = new CarListing(
                getString(cursor, ListingEntry.KEY_UUID),
                getString(cursor, ListingEntry.KEY_MAKE),
                getString(cursor, ListingEntry.KEY_MODEL),
                getString(cursor, ListingEntry.KEY_IMAGE),
                getString(cursor, ListingEntry.KEY_DESCRIPTION),
                getInt(cursor, ListingEntry.KEY_YEAR),
                getInt(cursor, ListingEntry.KEY_ASKING_PRICE),
                getInt(cursor, ListingEntry.KEY_STANDARD_PRICE),
                // since the database is storing boolean values as ints we convert it back to boolean
                // true == 1, false == 0
                getInt(cursor, ListingEntry.KEY_BEST) == 1,
                getInt(cursor, ListingEntry.KEY_WORST) == 1,
                getInt(cursor,ListingEntry.KEY_STARRED) == 1
            );
            carListings.add(carListing);
        }
        return carListings;
    }

    /**
     * Inserts a new CarListing into the database
     *
     * @param carListing the new entry to be inserted
     * @return the _Id of the table row
     */
    public long addCarListing(CarListing carListing) {
        SQLiteDatabase db = getWritableDatabase();

        // INSERT INTO Listings (Uuid, Make, Model, Image, Description, Year,  APrice, SPrice,
        //                  IsBest, IsWorst, IsStarred)
        // VALUES (...)
        ContentValues values = new ContentValues();
        values.put(ListingEntry.KEY_UUID, carListing.getUuid());
        values.put(ListingEntry.KEY_MAKE, carListing.getMake());
        values.put(ListingEntry.KEY_MODEL, carListing.getModel());
        values.put(ListingEntry.KEY_IMAGE, carListing.getImage());
        values.put(ListingEntry.KEY_DESCRIPTION, carListing.getDescription());
        values.put(ListingEntry.KEY_YEAR, carListing.getYear());
        values.put(ListingEntry.KEY_ASKING_PRICE, carListing.getAskingPrice());
        values.put(ListingEntry.KEY_STANDARD_PRICE, carListing.getStandardPrice());

        // Since the Sqlite database does not accept boolean values, we store them as ints
        values.put(ListingEntry.KEY_BEST, carListing.isBestInYear()? 1 : 0);
        values.put(ListingEntry.KEY_WORST, carListing.isWorstInYear()? 1 : 0);
        values.put(ListingEntry.KEY_STARRED, carListing.isStarred()? 1 : 0);

        long id = db.insert( ListingEntry.TABLE_NAME,
                             null,
                             values );

        db.close();

        return id;
    }

    /**
     * Removes the given car listing from the database based on its uuid
     * @param uuid the uuid of the entry to remove
     */
    public void removeCarListing(String uuid) {
        SQLiteDatabase db = getWritableDatabase();

        // DELETE FROM Listings
        // WHERE Uuid = ?
        db.delete( ListingEntry.TABLE_NAME,
                   ListingEntry.KEY_UUID + "=" + uuid,
                   null );

        db.close();
    }

    /**
     * Retrieves a car listing from the database base on its uuid
     * @param uuid the uuid of the car listing
     * @return the car listing
     */
    public CarListing getCarListing(String uuid) {
        SQLiteDatabase db = getReadableDatabase();

        // SELECT Uuid, Make, Model, Image, Description, Year, APrice, SPrice, IsBest, IsWorst,
        //        IsStarred
        // FROM Listings
        // WHERE Uuid = ?
        Cursor cursor = db.query( ListingEntry.TABLE_NAME,
                PROJECTION_CAR_LISTING,
                                  ListingEntry.KEY_UUID + "=?",
                                  new String[] { uuid },
                                  null, null, null, null );

        CarListing carListing = getCarListing(cursor);

        cursor.close();
        db.close();

        return carListing;
    }

    /**
     * Retrieves all the CarListings stored in the database
     * @return List containing all the CarListings
     */
    public List<CarListing> getAllCarListings() {
        SQLiteDatabase db = getReadableDatabase();

        // SELECT *, (APrice - SPrice) AS 'RANKING'
        // FROM Listings
        // ORDER BY 'RANKING'
        Cursor cursor = db.query( ListingEntry.TABLE_NAME,
                PROJECTION_CAR_LISTING,
                                  null, null, null, null,
                                  "RANKING ASC" );
        List<CarListing> carListings = getCarListings(cursor);

        cursor.close();
        db.close();

        return carListings;
    }

    /**
     * Returns all the CarListings that have been starred by the user
     * @return List containing all starred listings
     */
    public List<CarListing> getFavoriteCarListings() {
        SQLiteDatabase db = getReadableDatabase();

        // SELECT *, (APrice - SPrice) AS 'RANKING'
        // FROM Listings
        // WHERE IsStarred = 1
        // ORDER BY 'RANKING' ASC
        Cursor cursor = db.query( ListingEntry.TABLE_NAME,
                PROJECTION_CAR_LISTING,
                                  ListingEntry.KEY_STARRED + "=?",
                                  new String[] { "1" },
                                  null, null,
                                  "RANKING ASC" );

        List<CarListing> carListings = getCarListings(cursor);

        cursor.close();
        db.close();

        return carListings;
    }

    /**
     * @return a list containing all distinct makes
     */
    public List<String> getAllMakes() {
        SQLiteDatabase db = getReadableDatabase();

        // SELECT DISTINCT Make
        // FROM Listings
        Cursor cursor = db.query( true,
                                  ListingEntry.TABLE_NAME,
                                  new String[] { ListingEntry.KEY_MAKE },
                                  null, null, null, null, null, null );

        List<String> makes = new ArrayList<String>();
        while (cursor.moveToNext()) {
            makes.add(cursor.getString(cursor.getColumnIndex(ListingEntry.KEY_MAKE)));
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
        SQLiteDatabase db = getReadableDatabase();

        // SELECT DISTINCT Model
        // FROM Listings
        // WHERE Make  = ?
        Cursor cursor = db.query( true,
                                  ListingEntry.TABLE_NAME,
                                  new String[]{ListingEntry.KEY_MODEL},
                                  ListingEntry.KEY_MAKE + "=?",
                                  new String[]{ make },
                                  null, null, null, null );

        List<String> models = new ArrayList<String>();
        while (cursor.moveToNext()) {
            models.add(cursor.getString(cursor.getColumnIndex(ListingEntry.KEY_MODEL)));
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
    public List<CarListing> getCarListings(String make, String model, int minPrice, int maxPrice,
                                           int minYear, int maxYear) {
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder WHERE = new StringBuilder();

        WHERE.append(ListingEntry.KEY_ASKING_PRICE + " BETWEEN ? AND ?");
        WHERE.append(" AND " + ListingEntry.KEY_YEAR + " BETWEEN ? AND ?");

        // TODO: probably a better way to do this, but we can't make the Selection Args dynamic
        if (!make.equals("Any")) {
            WHERE.append(" AND " + ListingEntry.KEY_MAKE + "=\'"+ make + "\'");
            if (!model.equals("Any")) {
                WHERE.append(" AND " + ListingEntry.KEY_MODEL + "=\'" + model + "\'");
            }
        }

        Cursor cursor = db.query( ListingEntry.TABLE_NAME,
                PROJECTION_CAR_LISTING,
                                  WHERE.toString(),
                                  new String[] {Integer.toString(minPrice), Integer.toString(maxPrice),
                                                Integer.toString(minYear), Integer.toString(maxYear)},
                                  null, null,
                                  "RANKING ASC" );

        List<CarListing> carListings = getCarListings(cursor);

        cursor.close();
        db.close();

        return carListings;
    }

    /**
     * Updates the starred value of a CarListing
     * @param uuid uuid of the CarListing to update
     * @param starred new starred state
     */
    public void updateCarListingStarred(String uuid, boolean starred) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ListingEntry.KEY_STARRED, starred? 1 : 0);

        db.update( ListingEntry.TABLE_NAME,
                   values,
                   ListingEntry.KEY_UUID + "=?",
                   new String[] { uuid } );
        db.close();
    }

    /**
     * Updates the starred value of a CarListing
     * @param uuid uuid of the CarListing to update
     * @param standardPrice new standardPrice
     */
    public void updateCarListingStandardPrice(String uuid, int standardPrice) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ListingEntry.KEY_STANDARD_PRICE, standardPrice);

        db.update(ListingEntry.TABLE_NAME,
                values,
                ListingEntry.KEY_UUID + "=?",
                new String[]{ uuid } );

        db.close();
    }

    private static String getString(Cursor cursor, String columnName) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }

    private static int getInt(Cursor cursor, String columnName) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
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
