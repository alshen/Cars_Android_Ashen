package com.example.ashen.carfinder;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class Main extends Activity implements JsonLoader.OnTaskCompleted{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // if the network is unavailable, inform the user and ask to turn on wifi
        // exit otherwise
        if (!isNetworkAvailable()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(true);
            dialog.setTitle("Network Error");
            dialog.setMessage("This application requires a network connection, turn on wifi and" +
                    " try again. ");
            dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            dialog.show();
            return;
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ListingsFragment())
                    .commit();
        }

        init();
    }

    private void init() {
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        // TODO: We really shouldn't need to do this, but because of the way the JSON API is set up
        // we clear and re populate the database every time
        ListingDbHelper helper = new ListingDbHelper(this);
        //helper.clear();

        JsonLoader json = new JsonLoader(this, this);
        json.loadDatabase();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fragMgr = getFragmentManager();
        FragmentTransaction transaction = fragMgr.beginTransaction();

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_search) {
            SearchFragment newFragment;
            if (null == fragMgr.findFragmentByTag("search_frag")) {
                newFragment = new SearchFragment();
            } else {
                newFragment = (SearchFragment) fragMgr.findFragmentByTag("search_frag");
            }
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.container, newFragment, "search_frag");
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
            return true;
        } else if (id == R.id.action_starred) {
            FavoritesFragment newFragment;
            if (null == fragMgr.findFragmentByTag("fav_frag")) {
                newFragment = new FavoritesFragment();
            } else {
                newFragment = (FavoritesFragment) fragMgr.findFragmentByTag("fav_frag");
            }
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.container, newFragment, "fav_frag");
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTaskCompleted() {
        FragmentManager fragMgr = getFragmentManager();
        if (null == fragMgr.findFragmentByTag("main_frag")) {
                fragMgr.beginTransaction()
                    .replace(R.id.container, new ListingsFragment(), "main_frag")
                    .addToBackStack(null)
                    .commit();
        }
    }
}
