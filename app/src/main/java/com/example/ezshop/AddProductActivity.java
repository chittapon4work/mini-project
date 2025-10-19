package com.example.ezshop;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezshop.R;

public class AddProductActivity extends AppCompatActivity {
    EditText etName, etQty, etDesc, etImage;
    Button btnAdd;
    DBhelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        etDesc = findViewById(R.id.etDesc);
        etImage = findViewById(R.id.etImage);
        btnAdd = findViewById(R.id.btnAddProductDone);
        db = new DBhelper(this);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String qtys = etQty.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                String image = etImage.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(qtys)) {
                    Toast.makeText(AddProductActivity.this, "กรุณากรอกชื่อและจำนวน", Toast.LENGTH_SHORT).show();
                    return;
                }
                int qty;
                try { qty = Integer.parseInt(qtys); } catch (NumberFormatException e) { qty = 0; }
                if (qty < 0) qty = 0;
                boolean ok = db.addProduct(name, qty, desc, image);
                if (ok) {
                    Toast.makeText(AddProductActivity.this, "เพิ่มสินค้าเรียบร้อย", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddProductActivity.this, "ไม่สามารถเพิ่มสินค้าได้", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
