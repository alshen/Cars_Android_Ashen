package com.example.ashen.carfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

public class SearchResultsFragment extends ListingsFragment {
    private static final String KEY_MAKE      = "make";
    private static final String KEY_MODEL     = "model";
    private static final String KEY_MIN_YEAR  = "minYear";
    private static final String KEY_MAX_YEAR  = "maxYear";
    private static final String KEY_MIN_PRICE = "minPrice";
    private static final String KEY_MAX_PRICE = "maxPrice";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list,
                container, false);
        ListingDbHelper helper = new ListingDbHelper(view.getContext());

        String make  = getArguments().getString(KEY_MAKE);
        String model = getArguments().getString(KEY_MODEL);
        int minPrice = getArguments().getInt(KEY_MIN_YEAR);
        int maxPrice = getArguments().getInt(KEY_MAX_YEAR);
        int minYear  = getArguments().getInt(KEY_MIN_PRICE);
        int maxYear  = getArguments().getInt(KEY_MAX_PRICE);

        List<CarListing> carListings = helper.getCarListings( make, model, minPrice, maxPrice,
                minYear, maxYear );
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        ListingArrayAdapter adapter = new ListingArrayAdapter(view.getContext(), R.layout.list,
                                        carListings, this, this );
        listView.setAdapter(adapter);

        return view;
    }
}
