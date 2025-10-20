package com.example.ezshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    DBhelper dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        dbhelper = new DBhelper(this);
    }
    public void login(View v) {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showinfo("Message", "กรุณากรอกอีเมลและรหัสผ่าน");
            return;
        }

        boolean isValid = dbhelper.checkLogin(email, password);

        if (isValid) {
            String role = dbhelper.getRoleByEmail(email);
            Intent intent;

            if (role != null && role.trim().equals(DBhelper.ROLE_PRODUCTION)) {
                intent = new Intent(this, PRD_IRDActivity.class);
            } else if (role != null && role.trim().equals(DBhelper.ROLE_STOCKER)) {
                intent = new Intent(this, PRD_IRDActivity.class);
            } else {
                intent = new Intent(this, PRD_IRDActivity.class);
            }

            intent.putExtra("email", email);

            edtEmail.setText("");
            edtPassword.setText("");
            startActivity(intent);

        } else {
            showinfo("Message", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
        }
    }
    public void register(View v){
        Intent gotoRegister = new Intent(this, register.class);
        startActivity(gotoRegister);
    }
    public void clickvivew(View v){
        Intent gotoLogin = new Intent(this, view.class);
        startActivity(gotoLogin);
    }
    public void showinfo(String title,String msg) {
        AlertDialog.Builder show = new AlertDialog.Builder(this);
        show.setCancelable(true);
        show.setTitle(title);
        show.setMessage(msg);
        show.show();
    }
}