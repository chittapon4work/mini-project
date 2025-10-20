package com.example.ezshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvProfileTel, tvProfileRole;
    private EditText etNewEmail, etCurrentPassword, etNewPassword, etConfirmNewPassword, etEmailPassword;
    private Button btnChangeEmail, btnChangePassword, btnLogout;
    private DBhelper dbHelper;
    private String currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        currentEmail = getIntent().getStringExtra("email");
        dbHelper = new DBhelper(this);

        if (currentEmail != null) {
            loadProfileData();
        }

        setupListeners();
        setupNavigation();
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_profile);

        // ตรวจสอบบทบาทและเปลี่ยนข้อความในเมนู
        String role = dbHelper.getRoleByEmail(currentEmail);
        if ("stocker".equalsIgnoreCase(role)) {
            MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
            cartItem.setTitle(R.string.menu_receipts);
        }

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, PRD_IRDActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("email", currentEmail);
                startActivity(intent);
                // เลื่อนขวาเมื่อกลับหน้าหลัก
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                String currentRole = dbHelper.getRoleByEmail(currentEmail);
                Intent intent;
                if ("stocker".equalsIgnoreCase(currentRole)) {
                    intent = new Intent(this, ReceipActivity.class);
                } else {
                    intent = new Intent(this, CartActivity.class);
                }
                intent.putExtra("email", currentEmail);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                // เลื่อนขวาเมื่อไปหน้าตะกร้า
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(R.id.nav_profile);

        // อัพเดทข้อความในเมนูเมื่อกลับมาที่หน้านี้
        String role = dbHelper.getRoleByEmail(currentEmail);
        if ("stocker".equalsIgnoreCase(role)) {
            MenuItem cartItem = nav.getMenu().findItem(R.id.nav_cart);
            cartItem.setTitle(R.string.menu_receipts);
        }
    }

    @Override
    public void finish() {
        super.finish();
        // เลื่อนขวาเมื่อกดปุ่มกลับ
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void initializeViews() {
        // Profile info
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileTel = findViewById(R.id.tvProfileTel);
        tvProfileRole = findViewById(R.id.tvProfileRole);

        // Email change
        etNewEmail = findViewById(R.id.etNewEmail);
        etEmailPassword = findViewById(R.id.etEmailPassword); // เพิ่มการอ้างอิงช่องรหัสผ่านสำหรับเปลี่ยนอีเมล
        btnChangeEmail = findViewById(R.id.btnChangeEmail);

        // Password change
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Logout
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadProfileData() {
        Cursor cursor = dbHelper.getEmployeeInfo(currentEmail);
        if (cursor != null && cursor.moveToFirst()) {
            tvProfileName.setText("ชื่อ: " + cursor.getString(0));
            tvProfileEmail.setText("อีเมล: " + cursor.getString(1));
            tvProfileTel.setText("เบอร์โทร: " + cursor.getString(2));
            tvProfileRole.setText("ตำแหน่ง: " + cursor.getString(3));
            cursor.close();
        }
    }

    private void setupListeners() {
        btnChangeEmail.setOnClickListener(v -> handleEmailChange());
        btnChangePassword.setOnClickListener(v -> handlePasswordChange());
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleEmailChange() {
        String newEmail = etNewEmail.getText().toString().trim();
        String password = etEmailPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newEmail)) {
            etNewEmail.setError("กรุณากรอกอีเมลใหม่");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etEmailPassword.setError("กรุณากรอกรหัสผ่าน");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etNewEmail.setError("กรุณากรอกอีเมลให้ถูกต้อง");
            return;
        }

        // ตรวจสอบรหัสผ่านก่อนเปลี่ยนอีเมล
        if (!dbHelper.checkUser(currentEmail, password)) {
            etEmailPassword.setError("รหัสผ่านไม่ถูกต้อง");
            return;
        }

        if (dbHelper.updateEmail(currentEmail, newEmail)) {
            currentEmail = newEmail;
            Toast.makeText(this, "อัพเดตอีเมลสำเร็จ", Toast.LENGTH_SHORT).show();
            loadProfileData();
            etNewEmail.setText("");
            etEmailPassword.setText("");
        } else {
            Toast.makeText(this, "ไม่สามารถอัพเดตอีเมลได้", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePasswordChange() {
        String currentPass = etCurrentPassword.getText().toString();
        String newPass = etNewPassword.getText().toString();
        String confirmPass = etConfirmNewPassword.getText().toString();

        if (TextUtils.isEmpty(currentPass)) {
            etCurrentPassword.setError("กรุณากรอกรหัสผ่านปัจจุบัน");
            return;
        }

        if (TextUtils.isEmpty(newPass)) {
            etNewPassword.setError("กรุณากรอกรหัสผ่านใหม่");
            return;
        }

        if (TextUtils.isEmpty(confirmPass)) {
            etConfirmNewPassword.setError("กรุณายืนยันรหัสผ่านใหม่");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            etConfirmNewPassword.setError("รหัสผ่านไม่ตรงกัน");
            return;
        }

        // Verify current password
        if (!dbHelper.checkUser(currentEmail, currentPass)) {
            etCurrentPassword.setError("รหัสผ่านปัจจุบันไม่ถูกต้อง");
            return;
        }

        if (dbHelper.updatePassword(currentEmail, newPass)) {
            Toast.makeText(this, "เปลี่ยนรหัสผ่านสำเร็จ", Toast.LENGTH_SHORT).show();
            clearPasswordFields();
        } else {
            Toast.makeText(this, "ไม่สามารถเปลี่ยนรหัสผ่านได้", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearPasswordFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmNewPassword.setText("");
    }

    private void handleLogout() {
        // Clear any saved login state if needed
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
