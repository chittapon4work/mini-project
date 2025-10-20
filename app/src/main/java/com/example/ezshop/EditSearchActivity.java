package com.example.ezshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class EditSearchActivity extends AppCompatActivity {
    private SearchView searchView;
    private ListView listView;
    private DBhelper db;
    private ArrayAdapter<String> adapter;
    private List<Integer> resultIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_search);

        db = new DBhelper(this);
        searchView = findViewById(R.id.searchEditProduct);
        listView = findViewById(R.id.listSearchResults);
        // สร้าง adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        // พิมพ์ข้อความค้นหาใน SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
        // เมื่อคลิกที่สินค้าเปิดหน้าแก้ไข
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= resultIds.size()) return;
                int productId = resultIds.get(position);
                // เปิดหน้า EditProductActivity พร้อมส่ง id ไป
                Intent i = new Intent(EditSearchActivity.this, EditProductActivity.class);
                i.putExtra("productId", productId);
                startActivityForResult(i, 200); // รอค่ากลับมา
            }
        });
    }

    private void performSearch(String q) {
        adapter.clear();
        resultIds.clear();
        if (TextUtils.isEmpty(q)) return; // ไม่พิมพ์จะไม่ค้นหา

        // ตรวจว่าผู้ตัวเลขหรือชื่อสินค้า
        boolean isNum = q.matches("\\\\d+");
        String query;
        String[] args;
        // ค้นทั้ง id และชื่อสินค้า
        if (isNum) {
            query = "SELECT id, " + DBhelper.product_col_name + ", " + DBhelper.product_col_qty + " FROM " + DBhelper.product_table + " WHERE id = ? OR " + DBhelper.product_col_name + " LIKE ?";
            args = new String[]{q, "%" + q + "%"};
        }
        // ค้นจากชื่อสินค้าเท่านั้น
        else {
            query = "SELECT id, " + DBhelper.product_col_name + ", " + DBhelper.product_col_qty + " FROM " + DBhelper.product_table + " WHERE " + DBhelper.product_col_name + " LIKE ?";
            args = new String[]{"%" + q + "%"};
        }

        Cursor c = null;
        try {
            c = db.getReadableDatabase().rawQuery(query, args);
            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(0);
                    String name = c.getString(1);
                    int qty = 0;
                    try { qty = c.getInt(2); } catch (Exception ignored) {}
                    adapter.add(id + " - " + name + " (" + qty + ")");
                    resultIds.add(id);
                }
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "เกิดข้อผิดพลาดในการค้นหา", Toast.LENGTH_SHORT).show();
            if (c != null) c.close();
        }
    }

    @Override // เมื่อกลับมาจากหน้าแก้ไขสินค้า
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}

