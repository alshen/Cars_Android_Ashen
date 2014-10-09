package com.example.ashen.carfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Fragment containing CarListings that have been starred
 */
public class FavoritesFragment extends ListingsFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list,
                container, false);

        ListingDbHelper helper = new ListingDbHelper(view.getContext());
        ListView listView =(ListView)view.findViewById(android.R.id.list);
        ListingArrayAdapter adapter = new ListingArrayAdapter(view.getContext(), R.layout.list,
                helper.getFavoriteCarListings(), this, this);
        listView.setAdapter(adapter);
        return view;
    }
}
