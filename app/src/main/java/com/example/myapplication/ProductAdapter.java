package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<ProductItem> productList;

    public ProductAdapter(Context context, List<ProductItem> productList) {
        this.context = context;
        this.productList = productList;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName  = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        ProductItem item = productList.get(position);

        holder.productImage.setImageResource(item.getImageResId());
        holder.productName.setText(item.getName());
        holder.productPrice.setText(item.getPrice() + "₩");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productName", item.getName());
            intent.putExtra("productPrice", item.getPrice());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}