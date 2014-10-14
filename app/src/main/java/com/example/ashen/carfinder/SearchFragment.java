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
import android.view.View.OnClickListener;
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
    private static final String KEY_MAKE      = "make";
    private static final String KEY_MODEL     = "model";
    private static final String KEY_MIN_YEAR  = "minYear";
    private static final String KEY_MAX_YEAR  = "maxYear";
    private static final String KEY_MIN_PRICE = "minPrice";
    private static final String KEY_MAX_PRICE = "maxPrice";

    private static final int SEARCH_MIN_PRICE = 0;
    private static final int SEARCH_MAX_PRICE = 999999999;
    private static final int SEARCH_MIN_YEAR  = 0;
    private static final int SEARCH_MAX_YEAR  = 9999;

    private ArrayList<String> mModels;
    private List<String> mMakes;

    private ListingDbHelper mHelper;

    private View mView;
    private EditText mMake;
    private EditText mModel;
    private EditText mMinYear;
    private EditText mMaxYear;
    private EditText mMinPrice;
    private EditText mMaxPrice;
    private Button   mSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.search,
                container, false);
        mHelper = new ListingDbHelper(this.getActivity());

        mMakes = mHelper.getAllMakes();
        mMakes.add(0, "Any"); // insert the Any option

        // Android reuses this instance when its added to the back stack, so we check that mModels
        // hasn't been initialized already.
        if (mModels == null) {
            mModels = new ArrayList<String>();
            mModels.add("Any");
        }

        mMake = (EditText) mView.findViewById(R.id.search_make);
        mMake.setInputType(InputType.TYPE_NULL);

        mModel = (EditText) mView.findViewById(R.id.search_model);
        mModel.setInputType(InputType.TYPE_NULL);

        // creates a selection dialog when the field is clicked
        mMake.setOnClickListener(mMakeOnClickListener);

        // creates a selection dialog when the field is clicked
        mModel.setOnClickListener(mModelOnClickListener);

        mSearch = (Button) mView.findViewById(R.id.search_button);
        mSearch.setOnClickListener(mSearchOnClickListener);

        return mView;
    }

    private final OnClickListener mMakeOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
            dialogBuilder.setCancelable(true);
            dialogBuilder.setTitle("Select Make");
            final CharSequence[] items = mMakes.toArray(new CharSequence[mMakes.size()]);
            dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMake.setText(items[which]);
                    // TODO: seems a bit hacky but the inner class requires the list to be final
                    // but the final keyword prevents reassignment
                    mModels.clear();
                    mModels.add("Any");
                    mModels.addAll(mHelper.getAllModels(items[which].toString()));
                    mModel.setText("Any");
                }
            });
            dialogBuilder.show();
        }
    };

    private final OnClickListener mModelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
            dialogBuilder.setCancelable(true);
            dialogBuilder.setTitle("Select Model");
            final CharSequence[] items = mModels.toArray(new CharSequence[mModels.size()]);
            dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mModel.setText(items[which]);
                }
            });
            dialogBuilder.show();
        }
    };

    private final OnClickListener mSearchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMinPrice = (EditText) mView.findViewById(R.id.search_price_min);
            mMaxPrice = (EditText) mView.findViewById(R.id.search_price_max);

            //set the min/max price to either their value or 0 if they are empty
            String minPriceString = mMinPrice.getText().toString();
            String maxPriceString = mMaxPrice.getText().toString();

            int minPrice = minPriceString.isEmpty()?  SEARCH_MIN_PRICE : Integer.parseInt(minPriceString);
            int maxPrice = maxPriceString.isEmpty()?  SEARCH_MAX_PRICE : Integer.parseInt(maxPriceString);

            if (minPrice > maxPrice) {
                Toast.makeText(mView.getContext(), "Minimum Price must be lower than Maximum Price",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mMinYear = (EditText) mView.findViewById(R.id.search_year_min);
            mMaxYear = (EditText) mView.findViewById(R.id.search_year_max);

            //set the min/max year to either their value or 0 if they are empty
            String minYearString = mMinYear.getText().toString();
            String maxYearString = mMaxYear.getText().toString();

            int minYear = minYearString.isEmpty()?  SEARCH_MIN_YEAR : Integer.parseInt(minYearString);
            int maxYear = maxYearString.isEmpty()?  SEARCH_MAX_YEAR : Integer.parseInt(maxYearString);

            if (minYear > maxYear) {
                Toast.makeText(mView.getContext(), "Minimum Year must be lower than Maximum Year",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String make  = mMake.getText().toString();
            String model = mModel.getText().toString();

            FragmentManager fragMgr = getFragmentManager();
            FragmentTransaction transaction = fragMgr.beginTransaction();

            SearchResultsFragment newFragment = new SearchResultsFragment();

            // stores all the arguments that will be passed on for processing
            Bundle args = new Bundle();
            args.putString(KEY_MAKE, make);
            args.putString(KEY_MODEL, model);
            args.putInt(KEY_MIN_PRICE, minPrice);
            args.putInt(KEY_MAX_PRICE, maxPrice);
            args.putInt(KEY_MIN_YEAR, minYear);
            args.putInt(KEY_MAX_YEAR, maxYear);
            newFragment.setArguments(args);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.container, newFragment, "search_results_frag");
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    };
}
