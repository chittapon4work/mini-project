package com.example.ezshop;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.ezshop.R; // ensure resources resolve

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    private Context context;
    private List<CartItem> items = new ArrayList<>();
    private DBhelper db;
    private String email;

    public CartAdapter(Context context, String email) {
        this.context = context;
        this.email = email;
        this.db = new DBhelper(context);
        loadItems();
    }

    private void loadItems() {
        items.clear();
        Cursor c = db.getCartForEmail(email);
        if (c != null) {
            while (c.moveToNext()) {
                int cartId = c.getInt(0);
                int productId = c.getInt(1);
                String name = c.getString(2);
                int stock = c.getInt(3);
                int qty = c.getInt(4);
                items.add(new CartItem(cartId, productId, name, stock, qty));
            }
            c.close();
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem it = items.get(position);
        holder.tvName.setText(it.name);
        holder.tvQty.setText("จำนวน: " + it.qty);

        holder.btnRemove.setOnClickListener(v -> {
            boolean ok = db.removeCartItem(it.cartId);
            if (ok) {
                Toast.makeText(context, "ลบออกจากตะกร้า", Toast.LENGTH_SHORT).show();
                refresh();
            } else {
                Toast.makeText(context, "ไม่สามารถลบได้", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void refresh() {
        loadItems();
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQty;
        Button btnRemove;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvQty = itemView.findViewById(R.id.tvCartProductQty);
            btnRemove = itemView.findViewById(R.id.btnRemoveCart);
        }
    }

    static class CartItem {
        int cartId;
        int productId;
        String name;
        int stock;
        int qty;

        CartItem(int cartId, int productId, String name, int stock, int qty) {
            this.cartId = cartId;
            this.productId = productId;
            this.name = name;
            this.stock = stock;
            this.qty = qty;
        }
    }
}
