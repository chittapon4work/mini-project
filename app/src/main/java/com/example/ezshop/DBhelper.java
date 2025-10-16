package com.example.ezshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBhelper extends SQLiteOpenHelper {
    public static final String DB_name =  "EZShop.db";
    public static final String employee_table =  "emp_table";
        public static final String col2_name = "first_name";
        public static final String col3_last_name = "last_name";
        public static final String col4_password = "password";
        public static final String col5_tel = "tel";
        public static final String col6_email = "email";
        public static final String col7_role = "role";
    public DBhelper(Context context){
        super(context,DB_name,null,11);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CUSTOMER_TABLE = "CREATE TABLE " + employee_table +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "first_name TEXT,"+
                "last_name TEXT,"+
                "password TEXT,"+
                "tel TEXT,"+
                "email TEXT UNIQUE,"+
                "role TEXT)";
        db.execSQL(CREATE_CUSTOMER_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_EMPLOYEE_TABLE = "DROP TABLE IF EXISTS "+employee_table;
        db.execSQL(DROP_EMPLOYEE_TABLE);
        onCreate(db);
    }
    public Cursor getEmployee(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+employee_table,null);
        return cursor;
    }
    public boolean insertData(String name,String last_name,String password,String tel,String email, String role){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col2_name,name);
        contentValues.put(col3_last_name,last_name);
        contentValues.put(col4_password,password);
        contentValues.put(col5_tel,tel);
        contentValues.put(col6_email,email);
        contentValues.put(col7_role, role);
        long result = db.insert(employee_table,null,contentValues);
        if(result == -1){
            return false;
        } else{
            return true;
        }

    }
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + employee_table + " WHERE " + col6_email + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + employee_table + " WHERE " + col6_email + " = ? AND " + col4_password + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return result;
    }
    public Cursor getEmployeeInfo(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT first_name, email, tel , role FROM " + employee_table + " WHERE " + col6_email + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        return cursor;
    }
    public String getRoleByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String role = null;
        Cursor cursor = db.rawQuery("SELECT role FROM " + employee_table + " WHERE " + col6_email + " = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return role;
    }
}
