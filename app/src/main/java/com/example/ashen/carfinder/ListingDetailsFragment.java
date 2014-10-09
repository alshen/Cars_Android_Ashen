package com.example.ashen.carfinder;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class representing the detailed view of a CarListing
 */
public class ListingDetailsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.details_fragment,
                container, false);
        ListingDbHelper helper = new ListingDbHelper(view.getContext());
        String uuid = getArguments().getString("uuid");

        CarListing carListing = helper.getCarListing(uuid);

        TextView title = (TextView) view.findViewById(R.id.dftitle);
        title.setText(carListing.getYear() + " " + carListing.getMake() + " " + carListing.getModel());

        int price = carListing.getAskingPrice();
        NumberFormat enUS = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedPrice = String.format("$%s", enUS.format(price));
        if (formattedPrice.endsWith(".00")) {
            int centsIndex = formattedPrice.lastIndexOf(".00");
            if (centsIndex != -1) {
                formattedPrice = formattedPrice.substring(1, centsIndex);
            }
        }
        TextView priceView = (TextView) view.findViewById(R.id.dfprice);
        priceView.setText(formattedPrice);

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.default_image)
                .showImageOnFail(R.drawable.default_image)
                .showImageOnLoading(R.drawable.default_image).build();

        //initialize image view
        ImageView imageView = (ImageView) view.findViewById(R.id.dfimage);

        //download and display image from url
        imageLoader.displayImage(carListing.getImage(), imageView, options);

        // Make textView
        TextView make = (TextView) view.findViewById(R.id.dft1);
        make.setText(carListing.getMake());

        // Model textView
        TextView model = (TextView) view.findViewById(R.id.dft3);
        model.setText(carListing.getModel());

        // Description textView
        TextView description = (TextView) view.findViewById(R.id.dfdescription);
        description.setText(carListing.getDescription());

        return view;
    }
}
