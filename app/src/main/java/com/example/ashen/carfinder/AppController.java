package com.example.ashen.carfinder;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class to used manage network requests
 */
public class AppController {

    // request queue
    private RequestQueue mRequestQueue;

    private static Context mContext;
    private static AppController mInstance;

    private AppController(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    /**
     * @param context
     * @return an instance of the AppController
     */
    public static synchronized AppController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppController(context);
        }
        return mInstance;
    }

    /**
     * @return the request queue
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }

        return mRequestQueue;
    }

    /**
     * Appends a request to the request queue
     * @param req the request to add
     * @param <T> the type of the expected response
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}