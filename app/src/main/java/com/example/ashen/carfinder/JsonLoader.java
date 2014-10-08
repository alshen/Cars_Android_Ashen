package com.example.ashen.carfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
import java.util.UUID;

/**
 * Helper class to load the database from the JSON API
 */
public class JsonLoader {
    private static final String URL_AVAILABLE_CARS = "https://az-hack.s3.amazonaws.com/CarFinder/available_cars";
    private static final String URL_BEST_CARS = "https://az-hack.s3.amazonaws.com/CarFinder/best";
    private static final String URL_WORST_CARS = "https://az-hack.s3.amazonaws.com/CarFinder/worst";
    private static final String URL_STANDARD_PRICE = "https://az-hack.s3.amazonaws.com/CarFinder/price";

    private ListingDbHelper mCarsDbHelper;
    private Context mContext;
    private OnTaskCompleted mOnTaskCompletedCallback;

    public JsonLoader(Context context, OnTaskCompleted callback) {
        this.mCarsDbHelper  = new ListingDbHelper(context);
        this.mContext = context;
        this.mOnTaskCompletedCallback = callback;
    }

    public void loadDatabase() {
        getJson(URL_AVAILABLE_CARS);
    }

    private void getJson(String uri) {
        new HttpAsyncTask().execute(uri);
    }

    // TODO: http requests need refactoring, a lot of code duplication
    private class HttpAsyncTask extends AsyncTask<String, Integer, Boolean> {
        // caches the best/worst cars of each year
        private HashMap<Integer, HashSet<String>> mBestCache;
        private HashMap<Integer, HashSet<String>> mWorstCache;
        ProgressDialog progressDialog;

        protected HttpAsyncTask() {
            super();
            this.mBestCache = new HashMap<Integer, HashSet<String>>();
            this.mWorstCache = new HashMap<Integer, HashSet<String>>();
        }

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("Processing...");
            progressDialog.setMessage("Please wait.");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        protected Boolean doInBackground(String... urls) {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);

            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String payload = EntityUtils.toString(entity);
                    JSONArray jArr = new JSONArray(payload);
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jObj = jArr.getJSONObject(i);

                        String  make          = jObj.getString("make");
                        String  model         = jObj.getString("model");
                        String  image         = jObj.getString("image");
                        String  description   = jObj.getString("description");
                        int     year          = jObj.getInt("year");
                        int     askingPrice   = jObj.getInt("price");
                        int     standardPrice = requestStandardPrice(make, model, year);

                        // TODO: these a hard coded to false because the queries take too long
                        // and the information is of little value at the moment
                        boolean bestInYear    = false;//requestBestInYear(make, model, year);
                        boolean worstInYear   = false;//requestWorstInYear(make, model, year);

                        // Insert the new row, returning the primary key value of the new row
                        String uuid = UUID.randomUUID().toString();
                        CarListing carListing = new CarListing(uuid, make, model, image, description,
                                year, askingPrice, standardPrice, bestInYear, worstInYear, false);
                        long id = mCarsDbHelper.addCarListing(carListing);
                    }
                    return true;
                } else {
                    //Toast.makeText(mContext, "Connection Error, could nto get results", Toast.LENGTH_LONG)
                            //.show();
                    return false;
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            } catch (JSONException e) {
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            mOnTaskCompletedCallback.onTaskCompleted();
        }

        private int requestStandardPrice(String make, String model, int year) {
            try {
                String url = URL_STANDARD_PRICE + "/" + URLEncoder.encode(make, "utf-8")
                        + "/" + URLEncoder.encode(model, "utf-8")
                        + "/" + year;

                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String payload = EntityUtils.toString(entity);
                    return Integer.parseInt(payload.trim());
                } else {
                    return -1;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
            return -1;
        }

        private boolean requestBestInYear(String make, String model, int year) {
            // check cache before making a a new request
            if (mBestCache.containsKey(year) && mBestCache.get(year).contains(make + " " + model)) {
                return true;
            }

            String url = URL_BEST_CARS + "/" + year;

            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String payload = EntityUtils.toString(entity);
                    HashSet<String> bestCars = new HashSet<String>();
                    JSONArray jArr = new JSONArray(payload);
                    for (int i = 0; i < jArr.length(); i++) {
                        String car = jArr.getString(i);
                        bestCars.add(car);
                    }
                    mBestCache.put(year, bestCars);
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return (mBestCache.containsKey(year)
                    && mBestCache.get(year).contains(make + " " + model));
        }

        private boolean requestWorstInYear(String make, String model, int year) {
            // check cache before making a a new request
            if (mWorstCache.containsKey(year) && mWorstCache.get(year).contains(make + " " + model)) {
                return true;
            }

            String url = URL_WORST_CARS + "/" + year;

            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String payload = EntityUtils.toString(entity);
                    HashSet<String> bestCars = new HashSet<String>();
                    JSONArray jArr = new JSONArray(payload);
                    for (int i = 0; i < jArr.length(); i++) {
                        String car = jArr.getString(i);
                        bestCars.add(car);
                    }
                    mWorstCache.put(year, bestCars);
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return (mBestCache.containsKey(year)
                    && mBestCache.get(year).contains(make + " " + model));
        }
    }

    public interface OnTaskCompleted {
        public void onTaskCompleted();
    }
}
