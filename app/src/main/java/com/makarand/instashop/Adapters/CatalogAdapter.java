package com.makarand.instashop.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.makarand.instashop.Helpers.TimeHelper;
import com.makarand.instashop.Models.Product;
import com.makarand.instashop.R;
import com.makarand.instashop.ViewHolder.CatalogViewHolder;
import com.squareup.picasso.Picasso;

public class CatalogAdapter extends FirebaseRecyclerAdapter<Product, CatalogViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public CatalogAdapter(@NonNull FirebaseRecyclerOptions<Product> options) {
        super(options);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull CatalogViewHolder holder, int position, @NonNull Product model) {
        holder.nameTextView.setText(model.getName());
        holder.priceTextView.setText("₹ " + model.getPrice());
        holder.timestampTextView.setText(TimeHelper.getTimeStringFromTimestamp(model.getTimeStamp()));
        Picasso.get()
                .load(model.getLink())
                .into(holder.productImageView);
    }

    @NonNull
    @Override
    public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_catalog_list, parent, false);

        return new CatalogViewHolder(view);
    }
}
