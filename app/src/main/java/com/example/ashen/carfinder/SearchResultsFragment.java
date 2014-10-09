package com.example.ashen.carfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

public class SearchResultsFragment extends ListingsFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list,
                container, false);
        ListingDbHelper helper = new ListingDbHelper(view.getContext());

        String make  = getArguments().getString("make");
        String model = getArguments().getString("model");
        int minPrice = getArguments().getInt("minPrice");
        int maxPrice = getArguments().getInt("maxPrice");
        int minYear  = getArguments().getInt("minYear");
        int maxYear  = getArguments().getInt("maxYear");

        List<CarListing> carListings = helper.getCarListings( make, model, minPrice, maxPrice,
                minYear, maxYear );
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        ListingArrayAdapter adapter = new ListingArrayAdapter(view.getContext(), R.layout.list,
                                        carListings, this, this );
        listView.setAdapter(adapter);

        return view;
    }
}
