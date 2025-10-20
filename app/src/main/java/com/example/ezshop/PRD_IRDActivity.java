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

    private void setupUserAndRole() {
        email = getIntent().getStringExtra("email");
        role = null;
        if (email != null) {
            showEmployeeInfo(email);
            role = dbhelper.getRoleByEmail(email);
        }
    }

    private void setupUI() {
        if (role != null && role.trim().equals(DBhelper.ROLE_STOCKER)) {
            setupStockerUI();
        } else if (role != null && role.trim().equals(DBhelper.ROLE_PRODUCTION)) {
            setupEmployeeUI();
        } else {
            setupDefaultUI();
        }
    }

    private void setupStockerUI() {
        btnViewReceipts.setVisibility(View.GONE);
        btnAddProduct.setVisibility(View.VISIBLE);
        fabOpenCart.setVisibility(View.GONE);
        if (fabEditProduct != null) {
            fabEditProduct.setVisibility(View.VISIBLE);
            fabEditProduct.setOnClickListener(v -> {
                Intent i = new Intent(PRD_IRDActivity.this, EditSearchActivity.class);
                startActivityForResult(i, REQUEST_EDIT_PRODUCT);
            });
        }

        btnViewReceipts.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReceipActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnAddProduct.setOnClickListener(v ->
            startActivity(new Intent(this, AddProductActivity.class)));
    }

    private void setupEmployeeUI() {
        btnViewReceipts.setVisibility(View.GONE);
        btnAddProduct.setVisibility(View.GONE);  // Changed to GONE to hide add product button for ฝ่ายเบิกผลิต
        fabOpenCart.setVisibility(View.GONE);
        if (fabEditProduct != null) fabEditProduct.setVisibility(View.GONE);

        fabOpenCart.setOnClickListener(v -> {
            Intent i = new Intent(PRD_IRDActivity.this, CartActivity.class);
            i.putExtra("email", email);
            startActivity(i);
        });
    }

    private void setupDefaultUI() {
        btnViewReceipts.setVisibility(View.GONE);
        btnAddProduct.setVisibility(View.GONE);
        fabOpenCart.setVisibility(View.GONE);
        if (fabEditProduct != null) fabEditProduct.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PRODUCT && resultCode == RESULT_OK) {
            if (adapter != null) {
                adapter.refresh();
            }
        }
    }

    // Update role check helper to use constant
    private boolean isStockerRole(String r) {
        if (r == null) return false;
        return r.trim().equals(DBhelper.ROLE_STOCKER);
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);

        if (isStockerRole(role)) {
            if (nav != null) {
                nav.post(() -> {
                    MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
                    if (cartItem != null) cartItem.setTitle(R.string.menu_receipts);
                });
            }
        }

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_cart) {
                Intent intent;
                if (isStockerRole(role)) {
                    intent = new Intent(this, ReceipActivity.class);
                } else {
                    intent = new Intent(this, CartActivity.class);
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
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.refresh();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_home);

        // เปลี่ยนข้อความในเมนูตามบทบาทเมื่อกลับมาที่หน้านี้ (ทำหลัง inflation)
        if (isStockerRole(role) && nav != null) {
            nav.post(() -> {
                MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
                if (cartItem != null) cartItem.setTitle(R.string.menu_receipts);
            });
        }
    }

    private void showEmployeeInfo(String email) {
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

    private void setupProductList(String role, String email) {
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, role, email);
         recyclerProducts.setAdapter(adapter);
    }

    private void setupSearchView() {
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
