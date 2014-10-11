package com.example.ashen.carfinder;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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

    // boolean array for storing
    // the state of each CheckBox
    private boolean[] mCheckBoxState;

    static class ViewHolder {
        TextView  title;
        TextView  description;
        TextView  price;
        ImageView image;
        CheckBox  starred;
    }

    public ListingArrayAdapter(Context context, int resource, List<CarListing> carListings,
                               OnClickListener onClickedCallback,
                               OnCheckedChangedListener onCheckedChangedCallback) {
        super(context, resource, carListings);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mCarListings              = carListings;
        this.mOnClickCallback          = onClickedCallback;
        this.mOnCheckedChangedCallback = onCheckedChangedCallback;
        this.mCheckBoxState            = new boolean[mCarListings.size()];

        for (int i = 0; i < mCheckBoxState.length; i++) {
            mCheckBoxState[i] = mCarListings.get(i).isStarred();
        }
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup viewGroup) {
        View view = convertView;
        ViewHolder holder = new ViewHolder();
        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listing_item, null);

            holder.title = (TextView) view.findViewById(R.id.list_item_title);
            holder.description = (TextView) view.findViewById(R.id.list_item_description);
            holder.price = (TextView) view.findViewById(R.id.list_item_price);
            holder.starred = (CheckBox) view.findViewById(R.id.list_item_starred_checkbox);
            holder.image = (ImageView) view.findViewById(R.id.list_image);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        final CarListing carListing = mCarListings.get(position);

        if (carListing != null) {
            // get and set the TextView for the list item title
            String title = carListing.getYear() + " " + carListing.getModel() + " " +
                    carListing.getMake();
            holder.title.setText(title);

            // get and set the TextView for the list item description
            holder.description.setText(carListing.getDescription());

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
            holder.price.setText(formattedPrice);

            //VITAL PART!!! Set the state of the CheckBox using the boolean array
            holder.starred.setChecked(mCheckBoxState[position]);
            holder.starred.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckBoxState[position]= ((CheckBox)v).isChecked();
                    mOnCheckedChangedCallback.onCheckedChanged(carListing.getUuid(),
                            mCheckBoxState[position]);
                }
            });

            // increase the touch area for the checked box
            final CheckBox delegate = holder.starred;
            final View parent = (View) delegate.getParent();
            parent.post( new Runnable() {
                // Post in the parent's message queue to make sure the parent
                // lays out its children before we call getHitRect()
                public void run() {
                    final Rect r = new Rect();
                    delegate.getHitRect(r);
                    r.top -= 10;
                    r.bottom += 10;
                    r.left -= 10;
                    r.right += 10;
                    parent.setTouchDelegate( new TouchDelegate( r , delegate));
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

            //download and display image from url
            imageLoader.displayImage(url, holder.image, options);

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
