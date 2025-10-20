package com.example.ezshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CartActivity extends AppCompatActivity {
    RecyclerView rvCart;
    CartAdapter adapter;
    DBhelper db;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        db = new DBhelper(this);
        email = getIntent().getStringExtra("email");
        if (email == null) email = "";

        setupViews();
        setupNavigation();
    }

    private void setupViews() {
        rvCart = findViewById(R.id.rvCart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, email);
        rvCart.setAdapter(adapter);

        Button btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            long receiptId = db.createReceiptFromCart(email);
            if (receiptId == -1) {
                Toast.makeText(CartActivity.this, "ตะกร้าว่างหรือเกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CartActivity.this, "สร้างใบเสร็จหมายเลข: " + receiptId, Toast.LENGTH_LONG).show();
                adapter.refresh();
            }
        });
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        String role = db.getRoleByEmail(email);
        if ("stocker".equalsIgnoreCase(role)) {
            MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
            cartItem.setTitle(R.string.menu_receipts);
        }

        nav.setSelectedItemId(R.id.nav_cart);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, PRD_IRDActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.refresh();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_cart);
        String role = db.getRoleByEmail(email);
        if ("stocker".equalsIgnoreCase(role)) {
            MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
            cartItem.setTitle(R.string.menu_receipts);
        } else {
            MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
            cartItem.setTitle(R.string.menu_cart);
        }
    }
}
