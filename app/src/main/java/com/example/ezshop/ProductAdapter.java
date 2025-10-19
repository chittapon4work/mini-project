package com.example.ezshop;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.ezshop.R; // ensure resources are resolved

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {
    private Context context;
    private List<Product> products = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>(); // สำหรับเก็บรายการที่กรองแล้ว
    private DBhelper db;
    private String role;
    private String email; // for cart operations

    public ProductAdapter(Context context, String role, String email) {
        this.context = context;
        this.db = new DBhelper(context);
        this.role = role == null ? "" : role;
        this.email = email;
        loadProducts();
    }

    private void loadProducts() {
        products.clear();
        filteredProducts.clear();
        Cursor c = db.getAllProducts();
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                int qty = c.getInt(2);
                String desc = c.getString(3);
                String image = c.getString(4);
                Product product = new Product(id, name, qty, desc, image);
                products.add(product);
                filteredProducts.add(product);
            }
            c.close();
        }
    }

    public void filter(String query) {
        filteredProducts.clear();
        if (query == null || query.isEmpty()) {
            filteredProducts.addAll(products);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Product product : products) {
                // ค้นหาจากรหัส (ID) หรือชื่อสินค้า
                if (String.valueOf(product.id).contains(lowerQuery) ||
                    product.name.toLowerCase().contains(lowerQuery)) {
                    filteredProducts.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    // helper to recognize stocker role (accepts English code or Thai display text)
    private boolean isStockerRole(String r) {
        if (r == null) return false;
        String low = r.toLowerCase();
        return low.contains("stocker") || low.contains("เติม");
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = filteredProducts.get(position);
        holder.tvName.setText(p.name);
        holder.tvDesc.setText(p.description == null ? "" : p.description);

        if (p.qty <= 0) {
            holder.tvQty.setText("รอเติมสต็อก");
            holder.tvQty.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.btnAddCart.setEnabled(false);
        } else {
            holder.tvQty.setText("จำนวน: " + p.qty);
            holder.tvQty.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            holder.btnAddCart.setEnabled(true);
        }

        // load image if provided
        if (p.image != null && !p.image.isEmpty()) {
            int resId = context.getResources().getIdentifier(p.image, "drawable", context.getPackageName());
            if (resId != 0) holder.img.setImageResource(resId);
            else holder.img.setImageResource(R.mipmap.ic_launcher);
        } else {
            holder.img.setImageResource(R.mipmap.ic_launcher);
        }

        // show/hide buttons based on role
        if (isStockerRole(role)) {
            holder.btnIncrease.setVisibility(View.VISIBLE);
            holder.etStockAmount.setVisibility(View.VISIBLE);
            holder.btnAddCart.setVisibility(View.GONE);
        } else {
            holder.btnIncrease.setVisibility(View.GONE);
            holder.etStockAmount.setVisibility(View.GONE);
            holder.btnAddCart.setVisibility(View.VISIBLE);
        }

        holder.btnIncrease.setOnClickListener(v -> {
            String amountStr = holder.etStockAmount.getText().toString();
            int amount = 1; // ค่าเริ่มต้น
            if (!TextUtils.isEmpty(amountStr)) {
                try {
                    amount = Integer.parseInt(amountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "กรุณาใส่จำนวนที่ถูกต้อง", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (amount <= 0) {
                Toast.makeText(context, "กรุณาใส่จำนวนมากกว่า 0", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = db.increaseStock(p.id, amount);
            if (ok) {
                Toast.makeText(context, "เพิ่มสต็อกเรียบร้อย", Toast.LENGTH_SHORT).show();
                holder.etStockAmount.setText(""); // ล้างช่องกรอกหลังจากเพิ่มสำเร็จ
                refresh();
            } else {
                Toast.makeText(context, "ไม่สามารถเพิ่มสต็อก", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnAddCart.setOnClickListener(v -> {
            if (email == null || email.isEmpty()) {
                Toast.makeText(context, "ไม่มีอีเมลผู้ใช้งาน", Toast.LENGTH_SHORT).show();
                return;
            }
            if (p.qty <= 0) {
                Toast.makeText(context, "สินค้าหมด กรุณารอเติมสต็อก", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean ok = db.addToCart(email, p.id, 1);
            if (ok) {
                refresh();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    public void refresh() {
        loadProducts();
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvDesc;
        Button btnAddCart, btnIncrease;
        EditText etStockAmount;
        ImageView img;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvDesc = itemView.findViewById(R.id.tvProductDesc);
            tvQty = itemView.findViewById(R.id.tvProductQty);
            btnAddCart = itemView.findViewById(R.id.btnAddCart);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseStock);
            etStockAmount = itemView.findViewById(R.id.etStockAmount);
            img = itemView.findViewById(R.id.imgProduct);
        }
    }
}
