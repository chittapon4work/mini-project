package com.example.ezshop;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class view extends AppCompatActivity {
    DBhelper dbh;
    Cursor Employee;
    TextView tvshow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view);

        dbh = new DBhelper(this);
        Employee = dbh.getEmployee();
        tvshow = findViewById(R.id.txtvivew);
        if(Employee.getCount() == 0){
            showinfo("Massage","No Data");
        }else {
            StringBuffer databuff = new StringBuffer();
            while (Employee.moveToNext()){
                databuff.append("รหัส : " +Employee.getString(0)+"\n");
                databuff.append("ชื่อ : " +Employee.getString(1)+"\n");
                databuff.append("นามสกุล : " +Employee.getString(2)+"\n");
                databuff.append("รหัส : " +Employee.getString(3)+"\n");
                databuff.append("เบอร์โทร : " +Employee.getString(4)+"\n");
                databuff.append("อีเมล : " +Employee.getString(5)+"\n");
                databuff.append("ตำแหน่ง : " +Employee.getString(6)+"\n");
                databuff.append("--------------------------------------------"+"\n");

            }
            tvshow.setMovementMethod(new ScrollingMovementMethod());
            tvshow.setTextColor(Color.parseColor("#344F1F"));
            tvshow.setText("รายชื่อพนักงาน\n"+databuff);
        }
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