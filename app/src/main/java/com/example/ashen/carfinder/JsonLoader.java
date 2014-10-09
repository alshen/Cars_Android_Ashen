package com.example.ashen.carfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Helper class to load the database from the JSON API
 */
public class JsonLoader {

    private ListingDbHelper mCarsDbHelper;
    private Context         mContext;
    private OnTaskCompleted mOnTaskCompletedCallback;

    public JsonLoader(Context context, OnTaskCompleted callback) {
        this.mCarsDbHelper            = new ListingDbHelper(context);
        this.mContext                 = context;
        this.mOnTaskCompletedCallback = callback;
    }

    public void loadDatabase() {
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("az-hack.s3.amazonaws.com")
                .appendPath("CarFinder")
                .appendPath("available_cars");
        final String url = builder.build().toString();

        final ProgressDialog pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Loading...");
        pDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jObj = response.getJSONObject(i);

                                String uuid        = UUID.randomUUID().toString();

                                String make        = jObj.getString("make");
                                String model       = jObj.getString("model");
                                String image       = jObj.getString("image");
                                String description = jObj.getString("description");

                                int year           = jObj.getInt("year");
                                int askingPrice    = jObj.getInt("price");

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

                        }
                        pDialog.hide();
                        mOnTaskCompletedCallback.onTaskCompleted();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // TODO: dialog
                    }
                });
        AppController.getInstance(mContext).addToRequestQueue(jsonArrayRequest);
    }

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
