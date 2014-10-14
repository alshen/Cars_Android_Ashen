package com.example.ashen.carfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Helper class to load the database from the JSON API
 */
public class JsonLoader {
    private static final String KEY_MAKE        = "make";
    private static final String KEY_MODEL       = "model";
    private static final String KEY_IMAGE       = "image";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_YEAR        = "year";
    private static final String KEY_PRICE       = "price";

    private ListingDbHelper mCarsDbHelper;
    private Context         mContext;
    private OnTaskCompleted mOnTaskCompletedCallback;
    private ProgressDialog  mProgressDialog;

    public JsonLoader(Context context, OnTaskCompleted callback) {
        this.mCarsDbHelper            = new ListingDbHelper(context);
        this.mContext                 = context;
        this.mOnTaskCompletedCallback = callback;
    }

    /**
     * Queries the API for car listings, and loads them into a database
     */
    public void loadDatabase() {
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("az-hack.s3.amazonaws.com")
                .appendPath("CarFinder")
                .appendPath("available_cars");
        final String url = builder.build().toString();

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, mJsonArrayResponseListener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("JsonLoader", "Network Error: " + volleyError.getMessage());
                        mProgressDialog.hide();
                    }
                });
        AppController.getInstance(mContext).addToRequestQueue(jsonArrayRequest);
    }

    private final Response.Listener mJsonArrayResponseListener = new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject jObj = response.getJSONObject(i);

                String uuid        = UUID.randomUUID().toString();

                String make        = jObj.getString(KEY_MAKE);
                String model       = jObj.getString(KEY_MODEL);
                String image       = jObj.getString(KEY_IMAGE);
                String description = jObj.getString(KEY_DESCRIPTION);

                int year           = jObj.getInt(KEY_YEAR);
                int askingPrice    = jObj.getInt(KEY_PRICE);

                // TODO: these a hard coded to false because the queries take too long
                // and the information is of little value at the moment
                // boolean bestInYear = false;//requestBestInYear(make, model, year);
                // boolean worstInYear = false;//requestWorstInYear(make, model, year);

                // Insert the new row
                CarListing carListing = new CarListing(uuid, make, model, image,
                        description, year, askingPrice, 0, false, false, false);
                mCarsDbHelper.addCarListing(carListing);

                // the standard price is updated asynchronously, this way we can
                // perform multiple requests at a time
                // TODO: the initial rankings may be off depending on when updates
                // are completed
                requestStandardPrice(uuid, make, model, year);
            }
        } catch (JSONException e) {
            // this probably happened because of some network error, or the
            // server produced some error, the UI should display "NO RESULTS"
            Log.e("JsonLoader", "Network Error: " + e.getMessage());
        }
        mProgressDialog.hide();
        mOnTaskCompletedCallback.onTaskCompleted();
        }
    };

    /**
     * Queries the API for the standard price of a used car
     *
     * @param uuid of the CarListing
     * @param make of the used car
     * @param model of the used car
     * @param year of the used car
     */
    private void requestStandardPrice(final String uuid, String make, String model, int year) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("az-hack.s3.amazonaws.com")
                .appendPath("CarFinder")
                .appendPath("price")
                .appendPath(make)
                .appendPath(model)
                .appendPath(Integer.toString(year));
        final String url = builder.build().toString();

        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // WARNING: parseInt doesn't handle the response string well, probably
                        // due to the EOF token so we trim the response string
                        // TODO: NumberFormatException if some garbage data is returned from API?
                        int standardPrice = Integer.parseInt(response.trim());
                        mCarsDbHelper.updateCarListingStandardPrice(uuid, standardPrice);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("JsonLoader", "Network Error: Standard Price " + volleyError.getMessage());
                    }
                });
        AppController.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    /*private void requestBestInYear(final String uuid, String make, String model, int year,
                                   boolean best) {

        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("az-hack.s3.amazonaws.com")
                .appendPath("CarFinder")
                .appendPath(best? "best" : "worst")
                .appendPath(Integer.toString(year));
        final String url = builder.build().toString();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //response.
                        mCarsDbHelper.updateCarListingStandardPrice(uuid, 0);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        AppController.getInstance(mContext).addToRequestQueue(jsonArrayRequest);
    }*/

    public interface OnTaskCompleted {
        public void onTaskCompleted();
    }
}
