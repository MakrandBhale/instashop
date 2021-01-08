package com.makarand.instashop.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.makarand.instashop.R;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTextView, priceTextView, sellerTextView, timestampTextView;
    public ShapeableImageView productImageView;
    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        nameTextView = itemView.findViewById(R.id.product_name_textview);
        priceTextView = itemView.findViewById(R.id.produc_price_textview);
        sellerTextView = itemView.findViewById(R.id.product_seller_textview);
        timestampTextView = itemView.findViewById(R.id.product_timestamp_textview);
        productImageView = itemView.findViewById(R.id.product_image_view);

        float radius = itemView.getResources().getDimension(R.dimen.default_corner_radius);
        productImageView.setShapeAppearanceModel(productImageView.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED,radius)
                .build());
    }
}
