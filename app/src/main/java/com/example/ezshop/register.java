package com.example.ezshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
public class register extends AppCompatActivity {
    EditText edtName,edtLname,edtpass,edtconpass,edtTel,edtEmail;
    Spinner spRole;
    DBhelper dbhelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        dbhelper = new DBhelper(this);
        edtName = findViewById(R.id.edtName);
        edtLname = findViewById(R.id.edtLname);
        edtpass = findViewById(R.id.etdPassword);
        edtconpass = findViewById(R.id.edtCon_password);
        edtTel = findViewById(R.id.edtTel);
        edtEmail = findViewById(R.id.edtEmail);
        spRole = findViewById(R.id.spRole);
    }
    public void register(View view) {
        String name = edtName.getText().toString().trim();
        String lname = edtLname.getText().toString().trim();
        String pass = edtpass.getText().toString().trim();
        String conpass = edtconpass.getText().toString().trim();
        String tel = edtTel.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        if (name.isEmpty() || lname.isEmpty() || pass.isEmpty() || conpass.isEmpty() ||
                tel.isEmpty() || email.isEmpty() || role.isEmpty()) {
            showinfo("Message", "กรุณากรอกข้อมูลให้ครบทุกช่อง");
            return;
        }

        if (!pass.equals(conpass)) {
            showinfo("Message", "รหัสผ่านต้องตรงกัน");
            return;
        }

        if (dbhelper.isEmailExists(email)) {
            showinfo("Message", "อีเมลนี้ถูกใช้สมัครแล้ว กรุณาใช้อีเมลอื่น");
            return;
        }

        boolean inserted = dbhelper.insertData(name, lname, pass, tel, email , role);

        if (inserted) {
            ClearText();
            showinfo("Message", "สมัครสมาชิกสำเร็จ");
        } else {
            showinfo("Message", "ไม่สามารถเพิ่มข้อมูลได้");
        }
    }
    public void ClearText(){
        edtName.setText("");
        edtLname.setText("");
        edtpass.setText("");
        edtconpass.setText("");
        edtTel.setText("");
        edtEmail.setText("");
    }
    public void showinfo(String title,String msg) {
        AlertDialog.Builder show = new AlertDialog.Builder(this);
        show.setCancelable(true);
        show.setTitle(title);
        show.setMessage(msg);
        show.show();
    }
    public void clickHome(View view){
        Intent gotohome = new Intent(this, MainActivity.class);
        startActivity(gotohome);
    }
}