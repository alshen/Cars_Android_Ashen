package com.example.ashen.carfinder;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * An ArrayAdapter to handle Car Listings
 */
public class ListingArrayAdapter extends ArrayAdapter {
    private LayoutInflater mInflater;
    private List<CarListing> mCarListings;
    private OnClickListener mOnClickCallback;
    private OnCheckedChangedListener mOnCheckedChangedCallback;

    public ListingArrayAdapter(Context context, int resource, List<CarListing> carListings,
                               OnClickListener onClickedCallback,
                               OnCheckedChangedListener onCheckedChangedCallback) {
        super(context, resource, carListings);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mCarListings              = carListings;
        this.mOnClickCallback          = onClickedCallback;
        this.mOnCheckedChangedCallback = onCheckedChangedCallback;
    }

    @Override
    public View getView(int position, final View convertView, final ViewGroup parent) {
        View view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listing_item, null);
        }

        final CarListing carListing = mCarListings.get(position);

        if (carListing != null) {

            // get and set the TextView for the list item title
            TextView titleView = (TextView) view.findViewById(R.id.list_item_title);
            String title = carListing.getYear() + " " + carListing.getModel() + " " +
                    carListing.getMake();
            titleView.setText(title);

            // get and set the TextView for the list item description
            TextView descriptionView =  (TextView) view.findViewById(R.id.list_item_description);
            descriptionView.setText(carListing.getDescription());

            // get and set the TextView for the list item price
            TextView priceView = (TextView) view.findViewById(R.id.list_item_price);

            // we format this value from an int to a currency value (i.e., $1)
            // this currency value uses the US locale but can probably be localized further
            // we also strip any floating point values to get a whole dollar amount (i.e. 6.00 -> $6)
            // TODO: Further localization and considering rounding is the API should provide non
            // whole numbers
            int price = carListing.getAskingPrice();
            NumberFormat enUS = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedPrice = String.format("$%s", enUS.format(price));
            if (formattedPrice.endsWith(".00")) {
                int centsIndex = formattedPrice.lastIndexOf(".00");
                if (centsIndex != -1) {
                    formattedPrice = formattedPrice.substring(1, centsIndex);
                }
            }
            priceView.setText(formattedPrice);

            // get and set the checkbox that represents a starred (favorite) item
            final CheckBox starred = (CheckBox) view.findViewById(R.id.list_item_starred_checkbox);
            starred.setChecked(carListing.isStarred());
            starred.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnCheckedChangedCallback.onCheckedChanged(carListing.getUuid(),
                            starred.isChecked());
                }
            });
            /*starred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // if the value of this checkbox changes, we perform a call back so the
                    // parent can update the database
                    mOnCheckedChangedCallback.onCheckedChanged(carListing.getUuid(), isChecked);
                }
            });*/

            // increase the touch area of the checkbox, we want to give the user as
            // much space as possible to prevent miss clicks
            final View starredParent = (View) starred.getParent();
            starredParent.post( new Runnable() {
                // Post in the parent's message queue to make sure the parent
                // lays out its children before we call getHitRect()
                public void run() {
                    // TODO: figure out the best touch area, ideally find a way to view this Rect
                    final Rect r = new Rect();
                    starred.getHitRect(r);
                    r.top -= 10;
                    r.bottom += 10;
                    r.left -= 10;
                    r.right += 10;
                    starredParent.setTouchDelegate( new TouchDelegate( r , starred));
                }
            });

            //your image url
            String url = carListing.getImage();
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageForEmptyUri(R.drawable.default_image)
                    .showImageOnFail(R.drawable.default_image)
                    .showImageOnLoading(R.drawable.default_image).build();

            //initialize image view
            ImageView imageView = (ImageView) view.findViewById(R.id.list_image);

            //download and display image from url
            imageLoader.displayImage(url, imageView, options);

            // assigns a listener to handle on click events for this list item
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickCallback.onClicked(carListing.getUuid());
                }
            });
        }

        return view;
    }

    /**
     * Interface for onClicked events
     */
    public interface OnClickListener {
        public void onClicked(String uuid);
    }

    /**
     * Interface for onCheckChanged events
     */
    public interface OnCheckedChangedListener {
        public void onCheckedChanged(String uuid, boolean isChecked);
    }
}
