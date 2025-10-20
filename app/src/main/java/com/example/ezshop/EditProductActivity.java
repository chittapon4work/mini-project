package com.example.ezshop;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditProductActivity extends AppCompatActivity {
    private EditText etName, etDesc, etImage;
    private EditText etQty;
    private DBhelper db;
    private int productId;
    private int currentQty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        db = new DBhelper(this);
        productId = getIntent().getIntExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "ไม่พบข้อมูลสินค้า", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadProductData();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etEditName);
        etDesc = findViewById(R.id.etEditDesc);
        etImage = findViewById(R.id.etEditImage);
        etQty = findViewById(R.id.etEditQty);

        Button btnSave = findViewById(R.id.btnSaveProduct);
        btnSave.setOnClickListener(v -> saveChanges());

        Button btnDelete = findViewById(R.id.btnDeleteProduct);
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductData();
    }

    private void loadProductData() { // โหลดข้อมูลสินค้าจากฐานข้อมูลมาใส่
        etName.setText("");
        etDesc.setText("");
        etImage.setText("");
        if (etQty != null) etQty.setText("");

        Cursor c = null;
        try {
            c = db.getProductById(productId);
            if (c != null && c.moveToFirst()) { // ดึงข้อมูลสินค้า
                String name = null;
                String desc = null;
                String image = null;
                int qty = 0;

                int idxName = c.getColumnIndex(DBhelper.product_col_name);
                int idxDesc = c.getColumnIndex(DBhelper.product_col_desc);
                int idxImage = c.getColumnIndex(DBhelper.product_col_image);
                int idxQty = c.getColumnIndex(DBhelper.product_col_qty);

                if (idxName != -1) name = c.getString(idxName);
                else if (c.getColumnCount() > 1) {
                    try { name = c.getString(1); } catch (Exception ignored) {}
                }

                if (idxDesc != -1) desc = c.getString(idxDesc);
                else if (c.getColumnCount() > 3) {
                    try { desc = c.getString(3); } catch (Exception ignored) {}
                }

                if (idxImage != -1) image = c.getString(idxImage);
                else if (c.getColumnCount() > 4) {
                    try { image = c.getString(4); } catch (Exception ignored) {}
                }

                if (idxQty != -1) {
                    try { qty = c.getInt(idxQty); } catch (Exception ignored) { qty = 0; }
                } else if (c.getColumnCount() > 2) {
                    try { qty = c.getInt(2); } catch (Exception ignored) { qty = 0; }
                }
                currentQty = qty; // เก็บค่า qty ปัจจุบันไว้ใช้ทีหลัง

                etName.setText(name != null ? name : "");
                etDesc.setText(desc != null ? desc : "");
                etImage.setText(image != null ? image : "");
                if (etQty != null) etQty.setText(String.valueOf(qty));

            } else {
                Toast.makeText(this, "ไม่พบข้อมูลสินค้า", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "เกิดข้อผิดพลาดในการโหลดข้อมูล", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
    }

    private void showDeleteConfirmation() { // Alert ก่อนลบสินค้า
        new AlertDialog.Builder(this)
            .setTitle("ยืนยันการลบสินค้า")
            .setMessage("คุณต้องการลบสินค้านี้ใช่หรือไม่?")
            .setPositiveButton("ลบ", (dialog, which) -> deleteProduct())
            .setNegativeButton("ยกเลิก", null)
            .show();
    }

    private void deleteProduct() { // ฟเมธอดลบสินค้าออกจากฐานข้อมูล
        boolean success = db.deleteProduct(productId);
        if (success) {
            Toast.makeText(this, "ลบสินค้าเรียบร้อย", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "ไม่สามารถลบสินค้าได้", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() { // เมธอดบันทึกการแก้ไขสินค้า
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String image = etImage.getText().toString().trim();
        String qtyStr = etQty != null ? etQty.getText().toString().trim() : "";
        int qty = currentQty;

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "กรุณากรอกชื่อสินค้า", Toast.LENGTH_SHORT).show();
            return;
        } // ตรวจสอบจำนวนสินค้าให้ถูกต้อง
        if (!qtyStr.isEmpty()) {
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty < 0) {
                    Toast.makeText(this, "กรุณากรอกจำนวนที่เป็นจำนวนเต็มบวก", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "กรุณากรอกจำนวนเป็นตัวเลข", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean success = db.updateProduct(productId, name, qty, desc, image); // อัปเดตสินค้า
        if (success) {
            Toast.makeText(this, "บันทึกการแก้ไขเรียบร้อย", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "ไม่สามารถบันทึกการแก้ไขได้", Toast.LENGTH_SHORT).show();
        }
    }
}
