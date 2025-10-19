package com.example.ezshop;

import android.database.Cursor;
import android.os.Bundle;
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
        if (receiptId != -1) {
            loadDetails(receiptId);
        }
    }

    private void loadDetails(int receiptId) {
        StringBuilder sb = new StringBuilder();
        Cursor c = db.getReceiptItems(receiptId);
        while (c.moveToNext()) {
            int qty = c.getInt(0);
            String name = c.getString(1);
            sb.append(name).append(" x ").append(qty).append('\n');
        }
        c.close();
        tvDetails.setText(sb.toString());
    }
}

