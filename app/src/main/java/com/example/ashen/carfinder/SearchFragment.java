package com.example.ashen.carfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment contains a search view, allowing users search for cars based on
 * inputted search criteria
 */
public class SearchFragment extends Fragment {

    ArrayList<String> mModels = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.search,
                container, false);
        final ListingDbHelper helper = new ListingDbHelper(this.getActivity());

        final List<String> makes = helper.getAllMakes();
        makes.add(0, "Any"); // insert the Any option

        // Android reuses this instance when its added to the back stack, so we check that mModels
        // hasn't been initialized already.
        if (mModels == null) mModels = new ArrayList<String>();

        final EditText searchMake = (EditText) view.findViewById(R.id.search_make);
        searchMake.setInputType(InputType.TYPE_NULL);

        final EditText searchModel = (EditText) view.findViewById(R.id.search_model);
        searchModel.setInputType(InputType.TYPE_NULL);

        // creates a selection dialog when the field is clicked
        searchMake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                dialogBuilder.setCancelable(true);
                dialogBuilder.setTitle("Select Make");
                final CharSequence[] items = makes.toArray(new CharSequence[makes.size()]);
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        searchMake.setText(items[which]);
                        // TODO: seems a bit hacky but the inner class requires the list to be final
                        // but the final keyword prevents reassignment
                        mModels.clear();
                        mModels.add("Any");
                        mModels.addAll(helper.getAllModels(items[which].toString()));
                        searchModel.setText("Any");
                    }
                });
                dialogBuilder.show();
            }
        });

        // creates a selection dialog when the field is clicked
        searchModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                dialogBuilder.setCancelable(true);
                dialogBuilder.setTitle("Select Model");
                final CharSequence[] items = mModels.toArray(new CharSequence[mModels.size()]);
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        searchModel.setText(items[which]);
                    }
                });
                dialogBuilder.show();
            }
        });

        Button search = (Button) view.findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText searchMinPrice = (EditText) view.findViewById(R.id.search_price_min);
                EditText searchMaxPrice = (EditText) view.findViewById(R.id.search_price_max);

                //set the min/max price to either their value or 0 if they are empty
                String minPriceString = searchMinPrice.getText().toString();
                String maxPriceString = searchMaxPrice.getText().toString();

                int minPrice = minPriceString.isEmpty()?  0 : Integer.parseInt(minPriceString);
                int maxPrice = maxPriceString.isEmpty()?  999999999 : Integer.parseInt(maxPriceString);

                if (minPrice > maxPrice) {
                    Toast.makeText(view.getContext(), "Minimum Price must be lower than Maximum Price",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                EditText searchMinYear = (EditText) view.findViewById(R.id.search_year_min);
                EditText searchMaxYear = (EditText) view.findViewById(R.id.search_year_max);

                //set the min/max year to either their value or 0 if they are empty
                String minYearString = searchMinYear.getText().toString();
                String maxYearString = searchMaxYear.getText().toString();

                int minYear = minYearString.isEmpty()?  0 : Integer.parseInt(minYearString);
                int maxYear = maxYearString.isEmpty()?  9999 : Integer.parseInt(maxYearString);

                if (minYear > maxYear) {
                    Toast.makeText(view.getContext(), "Minimum Year must be lower than Maximum Year",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String make  = searchMake.getText().toString();
                String model = searchModel.getText().toString();

                FragmentManager fragMgr = getFragmentManager();
                FragmentTransaction transaction = fragMgr.beginTransaction();

                SearchResultsFragment newFragment = new SearchResultsFragment();

                // stores all the arguments that will be passed on for processing
                Bundle args = new Bundle();
                args.putString("make", make);
                args.putString("model", model);
                args.putInt("minPrice", minPrice);
                args.putInt("maxPrice", maxPrice);
                args.putInt("minYear", minYear);
                args.putInt("maxYear", maxYear);
                newFragment.setArguments(args);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.container, newFragment, "search_results_frag");
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        });
        return view;
    }
}
