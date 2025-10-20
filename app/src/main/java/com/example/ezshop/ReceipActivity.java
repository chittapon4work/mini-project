package com.example.ezshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReceipActivity extends AppCompatActivity {
    DBhelper db;
    LinearLayout container;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipts);

        db = new DBhelper(this);
        container = findViewById(R.id.receiptsContainer);
        email = getIntent().getStringExtra("email");

        setupUI();
        loadReceipts();
        setupNavigation();
    }

    private void setupUI() {
        // เปลี่ยนข้อความในเมนู nav_cart เป็น "ดูใบเสร็จ"
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav != null) {
            nav.post(() -> {
                MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
                if (cartItem != null) {
                    cartItem.setTitle(R.string.menu_receipts);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReceipts();
        // ตั้งค่า nav bar ให้แสดงที่ cart ทุกครั้งที่กลับมาหน้านี้
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav != null) {
            nav.post(() -> {
                nav.setSelectedItemId(R.id.nav_cart);
                MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
                if (cartItem != null) cartItem.setTitle(R.string.menu_receipts);
            });
        }
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav != null) nav.setSelectedItemId(R.id.nav_cart);
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

    private void loadReceipts() {
        container.removeAllViews();
        Cursor c = db.getReceipts();
        if (c != null) {
            while (c.moveToNext()) {
                final int id = c.getInt(0);
                String email = c.getString(1);
                String total = c.getString(2);
                String created = c.getString(3);

                View row = getLayoutInflater().inflate(R.layout.item_receipt, null);
                TextView tv = row.findViewById(R.id.tvReceiptInfo);
                Button btnDetail = row.findViewById(R.id.btnReceiptDetail);
                tv.setText("ใบเสร็จ: " + id + " | ผู้สั่ง: " + email + " | จำนวนรวม: " + total);
                btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ReceipActivity.this, ReceiptDetailActivity.class);
                        i.putExtra("receiptId", id);
                        startActivity(i);
                    }
                });
                container.addView(row);
            }
            c.close();
        }
    }
}
