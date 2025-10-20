package com.example.ezshop;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ReceiptDetailActivity extends AppCompatActivity {
    DBhelper db;
    TextView tvDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);

        db = new DBhelper(this);
        tvDetails = findViewById(R.id.tvReceiptDetails);

        int receiptId = getIntent().getIntExtra("receiptId", -1);
        Log.d("ReceiptDetail", "receiptId=" + receiptId);

        if (receiptId != -1) {
            loadDetails(receiptId);
        } else {
            tvDetails.setText("ไม่พบรายการใบเสร็จ");
        }
    }

    private void loadDetails(int receiptId) {
        Cursor c = db.getReceiptItems(receiptId);

        if (c == null || c.getCount() == 0) {
            tvDetails.setText("ยังไม่มีรายการสินค้า");
            if(c != null) c.close();
            return;
        }

        StringBuilder sb = new StringBuilder();
        while (c.moveToNext()) {
            int qty = c.getInt(0);     // จำนวน
            String name = c.getString(1); // ชื่อสินค้า
            sb.append(name).append(" x ").append(qty).append('\n');
        }
        c.close();
        tvDetails.setText(sb.toString());
    }
}
