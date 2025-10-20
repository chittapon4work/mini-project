package com.example.ezshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PRD_IRDActivity extends AppCompatActivity {
    private static final int REQUEST_EDIT_PRODUCT = 100;

    TextView tvName,tvRole, tvEmail, tvTel;
    DBhelper dbhelper;
    RecyclerView recyclerProducts;
    ProductAdapter adapter;
    Button btnViewReceipts;
    Button btnAddProduct;
    FloatingActionButton fabOpenCart;
    FloatingActionButton fabEditProduct;
    private androidx.appcompat.widget.SearchView searchView;

    private String role;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_after_login);

        initializeViews();
        setupUserAndRole();
        setupUI();
        setupProductList(role, email);
        setupSearchView();
        setupNavigation();
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);
        tvEmail = findViewById(R.id.tvEmail);
        tvTel = findViewById(R.id.tvTel);
        recyclerProducts = findViewById(R.id.recyclerProducts);
        btnViewReceipts = findViewById(R.id.btnViewReceipts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        fabOpenCart = findViewById(R.id.fabOpenCart);
        fabEditProduct = findViewById(R.id.fabEditProduct);
        searchView = findViewById(R.id.searchView);
        dbhelper = new DBhelper(this);
    }

    private void setupUserAndRole() { // ดึง email หาบทบาทในฐานข้อมูล
        email = getIntent().getStringExtra("email");
        role = null;
        if (email != null) {
            showEmployeeInfo(email);
            role = dbhelper.getRoleByEmail(email);
        }
    }

    private void setupUI() { // ตรวจสอบบทบาทแล้วตั้งค่า UI ตามบทบาท
        if (role != null && role.trim().equals(DBhelper.ROLE_STOCKER)) {
            setupStockerUI();
        } else if (role != null && role.trim().equals(DBhelper.ROLE_PRODUCTION)) {
            setupEmployeeUI();
        } else {
            setupDefaultUI();
        }
    }

    private void setupStockerUI() { // UI ฝั้งฝ่ายเติมสต๊อก Stocker/IRD
        btnViewReceipts.setVisibility(View.GONE);
        btnAddProduct.setVisibility(View.VISIBLE);
        fabOpenCart.setVisibility(View.GONE);
        if (fabEditProduct != null) {
            fabEditProduct.setVisibility(View.VISIBLE);
            // เปิดหน้า EditSearchActivity เพื่อค้นหาและแก้ไขสินค้า
            fabEditProduct.setOnClickListener(v -> {
                Intent i = new Intent(PRD_IRDActivity.this, EditSearchActivity.class);
                startActivityForResult(i, REQUEST_EDIT_PRODUCT);
            });
        }
        // เปิดหน้าใบเสร็จ
        btnViewReceipts.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReceipActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        // เปิดหน้าเพิ่มสินค้า
        btnAddProduct.setOnClickListener(v ->
            startActivity(new Intent(this, AddProductActivity.class)));
    }

    private void setupEmployeeUI() { // UI ฝั้งฝ่ายเติมสต๊อก Production/PRD
        btnViewReceipts.setVisibility(View.GONE);
        btnAddProduct.setVisibility(View.GONE);  // ฝ่ายนี้ห้ามเพิ่มสินค้า
        fabOpenCart.setVisibility(View.GONE);
        if (fabEditProduct != null) fabEditProduct.setVisibility(View.GONE);

        // เปิดหน้า CartActivity เมื่อต้องการดูตะกร้า
        fabOpenCart.setOnClickListener(v -> {
            Intent i = new Intent(PRD_IRDActivity.this, CartActivity.class);
            i.putExtra("email", email);
            startActivity(i);
        });
    }

    private void setupDefaultUI() { // ดักถ้าไม่รู้ role จะปิดทุกปุ่ม
        btnViewReceipts.setVisibility(View.GONE);
        btnAddProduct.setVisibility(View.GONE);
        fabOpenCart.setVisibility(View.GONE);
        if (fabEditProduct != null) fabEditProduct.setVisibility(View.GONE);
    }

    @Override // เมื่อกลับมาจากหน้าแก้ไขสินค้า EditProductActivity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PRODUCT && resultCode == RESULT_OK) {
            if (adapter != null) {
                adapter.refresh(); // โหลดรายการสินค้าใหม่
            }
        }
    }


    private boolean isStockerRole(String r) { // ตัวช่วยเช็กว่าเป็นบทบาท Stocker หรือไม่
        if (r == null) return false;
        return r.trim().equals(DBhelper.ROLE_STOCKER);
    }

    private void setupNavigation() { // จัดการเมนูด navbar
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);

        // เปลี่ยนชื่อเมนู cart เป็น “ใบเสร็จ” สำหรับฝ่ายเติมสต๊อก
        if (isStockerRole(role)) {
            if (nav != null) {
                nav.post(() -> {
                    MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
                    if (cartItem != null) cartItem.setTitle(R.string.menu_receipts);
                });
            }
        }

        // การกดแต่ละปุ่มใน navigation bar
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {// อยู่หน้าปัจจุบัน
                return true;
            } else if (id == R.id.nav_cart) {
                Intent intent;
                if (isStockerRole(role)) {
                    intent = new Intent(this, ReceipActivity.class); // ฝ่ายเติมสต๊อกดูใบเสร็จ
                } else {
                    intent = new Intent(this, CartActivity.class); // ฝ่ายเบิกผลิตดูตะกร้า
                }
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent i = new Intent(this, ProfileActivity.class);
                i.putExtra("email", email);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() { // เมื่อกลับมาหน้านี้อีกครั้ง เฟรชหน้า
        super.onResume();
        if (adapter != null) adapter.refresh(); // โหลดสินค้าล่าสุดใหม่
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);

        // อัปเดตเมนูตามบทบาท
        if (isStockerRole(role) && nav != null) {
            nav.post(() -> {
                MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
                if (cartItem != null) cartItem.setTitle(R.string.menu_receipts);
            });
        }
    }

    private void showEmployeeInfo(String email) { // แสดงข้อมูลพนักงาน
        Cursor cursor = dbhelper.getEmployeeInfo(email);
        if (cursor != null && cursor.moveToFirst()) {
            tvName.setText(getString(R.string.employee_greeting,
                cursor.getString(0)));
            tvRole.setText(getString(R.string.employee_role,
                cursor.getString(3)));
            tvEmail.setText(getString(R.string.employee_email,
                cursor.getString(1)));
            tvTel.setText(getString(R.string.employee_tel,
                cursor.getString(2)));
        }
        if (cursor != null) cursor.close();
    }

    private void setupProductList(String role, String email) { // ตั้งค่า RecyclerView แสดงรายการสินค้า
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, role, email);
         recyclerProducts.setAdapter(adapter);
    }

    private void setupSearchView() { // ตั้งค่าช่องค้นหาสินค้า (SearchView)
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }
}
