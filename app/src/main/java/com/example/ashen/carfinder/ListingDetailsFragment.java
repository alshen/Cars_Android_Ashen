package com.example.ashen.carfinder;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
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
    private static final String KEY_UUID = "uuid";

    private View mView;
    private TextView mTitle;
    private TextView mPrice;
    private TextView mMake;
    private TextView mModel;
    private TextView mDescription;
    private ImageView mImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.details_fragment,
                container, false);
        ListingDbHelper helper = new ListingDbHelper(mView.getContext());
        String uuid = getArguments().getString(KEY_UUID);

        CarListing carListing = helper.getCarListing(uuid);
        if (carListing == null) {
            // this should be a very rare case, the uuid should have been retrieved from a list
            // item which was associated with an entry in the database
            Log.e("ListingDetailsFragment", "CarListing was NULL");
            return mView;
        }

        mTitle = (TextView) mView.findViewById(R.id.dftitle);
        mTitle.setText(carListing.getYear() + " " + carListing.getMake() + " " + carListing.getModel());

        int price = carListing.getAskingPrice();
        mPrice = (TextView) mView.findViewById(R.id.dfprice);
        mPrice.setText(getFormattedPrice(price));

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.default_image)
                .showImageOnFail(R.drawable.default_image)
                .showImageOnLoading(R.drawable.default_image).build();

        //initialize image view
        mImage = (ImageView) mView.findViewById(R.id.dfimage);

        //download and display image from url
        imageLoader.displayImage(carListing.getImage(), mImage, options);

        // Make textView
        mMake = (TextView) mView.findViewById(R.id.dft1);
        mMake.setText(carListing.getMake());

        // Model textView
        mModel = (TextView) mView.findViewById(R.id.dft3);
        mModel.setText(carListing.getModel());

        // Description textView
        mDescription = (TextView) mView.findViewById(R.id.dfdescription);
        mDescription.setText(carListing.getDescription());

        return mView;
    }

    private static String getFormattedPrice(int price) {
        NumberFormat enUS = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedPrice = String.format("$%s", enUS.format(price));

        if (formattedPrice.endsWith(".00")) {
            int centsIndex = formattedPrice.lastIndexOf(".00");
            if (centsIndex != -1) {
                formattedPrice = formattedPrice.substring(1, centsIndex);
            }
        }

        return formattedPrice;
    }
}
