package com.example.ezshop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private String email;
    // Adapter สำหรับเชื่อมข้อมูลสินค้ากับ RecyclerView
    public ProductAdapter(Context context, String role, String email) { // สร้าง Adapter
        this.context = context;
        this.db = new DBhelper(context);
        this.role = role == null ? "" : role;
        this.email = email;
        loadProducts(); // โหลดข้อมูลสินค้าทั้งหมดจากฐานข้อมูล
    }

    private void loadProducts() { // โหลดสินค้าทั้งหมดจากฐานข้อมูล SQLite
        products.clear();
        filteredProducts.clear();
        Cursor c = db.getAllProducts(); // ดึงข้อมูลจากตาราง products
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                int qty = c.getInt(2);
                String desc = c.getString(3);
                String image = c.getString(4);
                // สร้าง obj สินค้าและเพิ่มใน list
                Product product = new Product(id, name, qty, desc, image);
                products.add(product);
                filteredProducts.add(product);
            }
            c.close();
        }
    }

    public void filter(String query) { // เมธอดกรองสินค้าจากคำค้นหา SearchView
        filteredProducts.clear();
        if (query == null || query.isEmpty()) {
            filteredProducts.addAll(products);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Product product : products) {
                // ค้นจากรหัสสินค้า หรือชื่อสินค้า
                if (String.valueOf(product.id).contains(lowerQuery) ||
                    product.name.toLowerCase().contains(lowerQuery)) {
                    filteredProducts.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override // ViewHolder ผูกกับ layout item_product.xml
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    // ตัวช่วยตรวจสอบว่า role คือฝ่ายเติมสต๊อกหรือไม่
    private boolean isStockerRole(String r) {
        if (r == null) return false;
        String low = r.toLowerCase();
        return low.contains("stocker") || low.contains("เติม");
    }

    @Override // เมธอดผูกข้อมูลกับแต่ละแถวใน RecyclerView
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = filteredProducts.get(position);
        // Reset ค่าเดิมก่อน กันข้อมูลซ้ำเวลา RecyclerView รีไซเคิล View
        holder.tvName.setText("");
        holder.tvDesc.setText("");
        holder.tvQty.setText("");
        holder.img.setImageResource(R.mipmap.ic_launcher);

        // แสดงข้อมูลสินค้า
        holder.tvName.setText(p.name);
        holder.tvDesc.setText(p.description == null ? "" : p.description);

        // แสดงจำนวนคงเหลือ
        if (p.qty <= 0) {
            holder.tvQty.setText("รอเติมสต็อก");
            holder.tvQty.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.btnAddCart.setEnabled(false);
        } else {
            holder.tvQty.setText("จำนวน: " + p.qty);
            holder.tvQty.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            holder.btnAddCart.setEnabled(true);
        }

        // โหลดรูปภาพจากชื่อไฟล์ในฐานข้อมูล รูปจาก drawable
        if (p.image != null && !p.image.isEmpty()) {
            int resId = context.getResources().getIdentifier(p.image, "drawable", context.getPackageName());
            if (resId != 0) holder.img.setImageResource(resId);
            else holder.img.setImageResource(R.mipmap.ic_launcher);
        } else {
            holder.img.setImageResource(R.mipmap.ic_launcher);
        }

        // แสดงปุ่มต่าง ๆ ตามบทบาทผู้ใช้
        if (isStockerRole(role)) { // ฝ่ายเติมสต็อก IRD
            holder.btnIncrease.setVisibility(View.VISIBLE);
            holder.etStockAmount.setVisibility(View.VISIBLE);
            holder.btnAddCart.setVisibility(View.GONE);
        } else { // ฝ่ายเบิกผลิต PRD
            holder.btnIncrease.setVisibility(View.GONE);
            holder.etStockAmount.setVisibility(View.GONE);
            holder.btnAddCart.setVisibility(View.VISIBLE);
        }

        holder.btnIncrease.setOnClickListener(v -> { // ปุ่มเพิ่มสต็อก
            String amountStr = holder.etStockAmount.getText().toString();
            int amount = 1;
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
            // อัปเดตฐานข้อมูลเพิ่มสต็อก
            boolean ok = db.increaseStock(p.id, amount);
            if (ok) {
                Toast.makeText(context, "เพิ่มสต็อกเรียบร้อย", Toast.LENGTH_SHORT).show();
                holder.etStockAmount.setText("");
                refresh();
            } else {
                Toast.makeText(context, "ไม่สามารถเพิ่มสต็อก", Toast.LENGTH_SHORT).show();
            }
        });
        // ปุ่มเพิ่มสินค้าลงตะกร้าเฉพาะ PRD ฝ่ายผลิต
        holder.btnAddCart.setOnClickListener(v -> {
            if (email == null || email.isEmpty()) {
                Toast.makeText(context, "ไม่มีอีเมลผู้ใช้งาน", Toast.LENGTH_SHORT).show();
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
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder { // ViewHolder item ใน RecyclerView
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
