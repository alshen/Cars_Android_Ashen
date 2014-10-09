package com.example.ashen.carfinder;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

        List<String> makes = helper.getAllMakes();
        makes.add(0, "ALL"); // insert the ALL option

        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter adapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, makes);
        spinner.setAdapter(adapter);

        final Spinner spinner2 = (Spinner) view.findViewById(R.id.spinner2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                String selection = spinner.getSelectedItem().toString();
                List<String> models = new ArrayList<String>();
                if (!selection.equals("ALL")) {
                    models = helper.getAllModels(selection);
                }
                models.add(0, "ALL");
                ArrayAdapter a = new ArrayAdapter(view.getContext(), R.layout.spinner_item, models);
                spinner2.setAdapter(a);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final EditText editText1 = (EditText) view.findViewById(R.id.ed1);
        final EditText editText2 = (EditText) view.findViewById(R.id.ed2);

        Button search = (Button) view.findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set the min/max price to either their value or 0 if they are empty
                String minPriceString = editText1.getText().toString();
                String maxPriceString = editText2.getText().toString();

                int minPrice = minPriceString.isEmpty()?  0 : Integer.parseInt(minPriceString);
                int maxPrice = maxPriceString.isEmpty()?  0: Integer.parseInt(maxPriceString);

                String make  = spinner.getSelectedItem().toString();
                String model = spinner2.getSelectedItem().toString();

                if (minPrice > maxPrice) {
                    Toast.makeText(view.getContext(), "Minimum Price must be lower than Maximum Price",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    FragmentManager fragMgr = getFragmentManager();
                    FragmentTransaction transaction = fragMgr.beginTransaction();

                    SearchResultsFragment newFragment = new SearchResultsFragment();

                    Bundle args = new Bundle();
                    args.putString("make", make);
                    args.putString("model", model);
                    args.putInt("minPrice", minPrice);
                    args.putInt("maxPrice", maxPrice);
                    newFragment.setArguments(args);

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.container, newFragment, "search_results_frag");
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                }
            }
        });
        return view;
    }
}
