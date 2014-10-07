package com.example.ashen.carfinder;

import android.provider.BaseColumns;
/**
 * Constants class, representing the database schema
 */
public final class ListingContract {
    public ListingContract() {};

    /**
     * Constants class, representing the database schema
     */
    public static abstract class ListingEntry implements BaseColumns {
        public static final String TABLE_NAME         = "Listings";
        public static final String KEY_UUID           = "Uuid";
        public static final String KEY_MAKE           = "Make";
        public static final String KEY_MODEL          = "Model";
        public static final String KEY_IMAGE          = "Image";
        public static final String KEY_DESCRIPTION    = "Description";
        public static final String KEY_YEAR           = "Year";
        public static final String KEY_ASKING_PRICE   = "APrice";
        public static final String KEY_STANDARD_PRICE = "SPrice";
        public static final String KEY_BEST           = "IsBest";
        public static final String KEY_WORST          = "IsWorst";
        public static final String KEY_STARRED        = "IsStarred";
        public static final String KEY_RANKING        = "Ranking";
    }
}

