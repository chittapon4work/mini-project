package com.example.ezshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.View;
import android.widget.TextView;

public class EmployeeHomeActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvTel;
    DBhelper dbhelper;
    RecyclerView recyclerProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_after_login);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvTel = findViewById(R.id.tvTel);
        recyclerProducts = findViewById(R.id.recyclerProducts);
        dbhelper = new DBhelper(this);


        String email = getIntent().getStringExtra("email");
        if (email != null) {
            showEmployeeInfo(email);
        }


        setupProductList();


        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_cart) {
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });
    }

    private void showEmployeeInfo(String email) {
        Cursor cursor = dbhelper.getEmployeeInfo(email);
        if (cursor != null && cursor.moveToFirst()) {
            tvName.setText("สวัสดีคุณ : " + cursor.getString(0)+"  ตำแหน่ง : " + cursor.getString(3));
            tvEmail.setText("อีเมล: " + cursor.getString(1));
            tvTel.setText("เบอร์โทร: " + cursor.getString(2));
        }
        if (cursor != null) cursor.close();
    }

    private void setupProductList() {
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
    }
    public void clickHome(View view){
        Intent gotohome = new Intent(this, MainActivity.class);
        startActivity(gotohome);
    }
}
