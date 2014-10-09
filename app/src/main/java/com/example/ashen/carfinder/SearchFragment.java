package com.example.ashen.carfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: this class needs to be refactored
 */
public class SearchFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.search,
                container, false);
        final ListingDbHelper helper = new ListingDbHelper(this.getActivity());

        final List<String> makes = helper.getAllMakes();
        makes.add(0, "Any"); // insert the ALL option

        final List<String> models = new ArrayList<String>();

        final EditText search_make = (EditText) view.findViewById(R.id.search_make);
        search_make.setInputType(InputType.TYPE_NULL);

        final EditText search_model = (EditText) view.findViewById(R.id.search_model);
        search_model.setInputType(InputType.TYPE_NULL);

        search_make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                dialogBuilder.setCancelable(true);
                dialogBuilder.setTitle("Select Make");
                final CharSequence[] items = makes.toArray(new CharSequence[makes.size()]);
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        search_make.setText(items[which]);
                        // TODO: seems a bit hacky but the inner class requires the list to be final
                        // but the final keyword prevents reassignment
                        models.clear();
                        models.add("Any");
                        models.addAll(helper.getAllModels(items[which].toString()));
                        search_model.setText("Any");
                    }
                });
                dialogBuilder.show();
            }
        });

        search_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                dialogBuilder.setCancelable(true);
                dialogBuilder.setTitle("Select Model");
                final CharSequence[] items = models.toArray(new CharSequence[models.size()]);
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        search_model.setText(items[which]);
                    }
                });
                dialogBuilder.show();
            }
        });

        Button search = (Button) view.findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText search_min_price = (EditText) view.findViewById(R.id.search_price_min);
                EditText search_max_price = (EditText) view.findViewById(R.id.search_price_max);

                //set the min/max price to either their value or 0 if they are empty
                String minPriceString = search_min_price.getText().toString();
                String maxPriceString = search_max_price.getText().toString();

                int minPrice = minPriceString.isEmpty()?  0 : Integer.parseInt(minPriceString);
                int maxPrice = maxPriceString.isEmpty()?  0: Integer.parseInt(maxPriceString);

                if (minPrice > maxPrice) {
                    Toast.makeText(view.getContext(), "Minimum Price must be lower than Maximum Price",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                EditText search_min_year = (EditText) view.findViewById(R.id.search_year_min);
                EditText search_max_year = (EditText) view.findViewById(R.id.search_year_max);

                //set the min/max year to either their value or 0 if they are empty
                String minYearString = search_min_year.getText().toString();
                String maxYearString = search_max_year.getText().toString();

                int minYear = minYearString.isEmpty()?  0 : Integer.parseInt(minYearString);
                int maxYear = maxYearString.isEmpty()?  0: Integer.parseInt(maxYearString);

                if (minYear > maxYear) {
                    Toast.makeText(view.getContext(), "Minimum Year must be lower than Maximum Year",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String make  = search_make.getText().toString();
                String model = search_model.getText().toString();

                FragmentManager fragMgr = getFragmentManager();
                FragmentTransaction transaction = fragMgr.beginTransaction();

                SearchResultsFragment newFragment = new SearchResultsFragment();

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
