package com.example.ashen.carfinder;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ListingsFragment extends ListFragment implements ListingArrayAdapter.OnClickListener,
    ListingArrayAdapter.OnCheckedChangedListener {
    private static final String KEY_UUID = "uuid";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list,
                container, false);

        ListingDbHelper helper = new ListingDbHelper(view.getContext());
        ListView listView =(ListView)view.findViewById(android.R.id.list);
        ListingArrayAdapter adapter = new ListingArrayAdapter(view.getContext(), R.layout.list,
                                        helper.getAllCarListings(), this, this );
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCheckedChanged(String uuid, boolean isChecked) {
        ListingDbHelper helper = new ListingDbHelper(getActivity());
        helper.updateCarListingStarred(uuid, isChecked);
    }

    @Override
    public void onClicked(String uuid) {
        // when an item in out list is clicked we display a detailed view
        // of the CarListing
        FragmentManager fragMgr = getFragmentManager();
        FragmentTransaction transaction = fragMgr.beginTransaction();

        ListingDetailsFragment newFragment = new ListingDetailsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_UUID, uuid);
        newFragment.setArguments(args);

        // Replace whatever is in the container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, newFragment, "details_frag");
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
