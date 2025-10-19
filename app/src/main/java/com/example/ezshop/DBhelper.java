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

    // New constants for products, cart and receipts
    public static final String product_table = "products";
    public static final String product_col_name = "name";
    public static final String product_col_qty = "qty";
    public static final String product_col_desc = "description";
    public static final String product_col_image = "image";

    public static final String cart_table = "cart"; // stores items added to cart before checkout
    public static final String cart_col_email = "email";
    public static final String cart_col_product_id = "product_id";
    public static final String cart_col_qty = "qty";

    public static final String receipt_table = "receipts";
    public static final String receipt_col_email = "email";
    public static final String receipt_col_total = "total";
    public static final String receipt_col_created = "created_at";

    public static final String receipt_items_table = "receipt_items";
    public static final String receipt_items_col_receipt_id = "receipt_id";
    public static final String receipt_items_col_product_id = "product_id";
    public static final String receipt_items_col_qty = "qty";

    // bump DB version so onUpgrade recreates updated tables
    public DBhelper(Context context){
        super(context,DB_name,null,13);  // Increased from 12 to 13 to trigger database recreation
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

        // seed demo users with Thai role names
        ContentValues user1 = new ContentValues();
        user1.put(col2_name, "StockerUser");
        user1.put(col3_last_name, "Demo");
        user1.put(col4_password, "password");
        user1.put(col5_tel, "0000000000");
        user1.put(col6_email, "stocker@example.com");
        user1.put(col7_role, "ฝ่ายเติมสต๊อก");
        db.insert(employee_table, null, user1);

        ContentValues user2 = new ContentValues();
        user2.put(col2_name, "EmployeeUser");
        user2.put(col3_last_name, "Demo");
        user2.put(col4_password, "password");
        user2.put(col5_tel, "1111111111");
        user2.put(col6_email, "employee@example.com");
        user2.put(col7_role, "ฝ่ายเบิกผลิต");
        db.insert(employee_table, null, user2);

        // create products table (with description and image)
        String CREATE_PRODUCT_TABLE = "CREATE TABLE IF NOT EXISTS " + product_table +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                product_col_name + " TEXT,"+
                product_col_qty + " INTEGER,"+
                product_col_desc + " TEXT,"+
                product_col_image + " TEXT)";
        db.execSQL(CREATE_PRODUCT_TABLE);

        // seed some sample products for testing
        ContentValues p1 = new ContentValues();
        p1.put(product_col_name, "สินค้า A");
        p1.put(product_col_qty, 10);
        p1.put(product_col_desc, "คำอธิบายสำหรับสินค้า A");
        p1.put(product_col_image, "");
        db.insert(product_table, null, p1);
        ContentValues p2 = new ContentValues();
        p2.put(product_col_name, "สินค้า B");
        p2.put(product_col_qty, 5);
        p2.put(product_col_desc, "คำอธิบายสำหรับสินค้า B");
        p2.put(product_col_image, "");
        db.insert(product_table, null, p2);

        // create cart table
        String CREATE_CART_TABLE = "CREATE TABLE IF NOT EXISTS " + cart_table +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                cart_col_email + " TEXT,"+
                cart_col_product_id + " INTEGER,"+
                cart_col_qty + " INTEGER)";
        db.execSQL(CREATE_CART_TABLE);

        // create receipts table
        String CREATE_RECEIPT_TABLE = "CREATE TABLE IF NOT EXISTS " + receipt_table +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                receipt_col_email + " TEXT,"+
                receipt_col_total + " REAL,"+
                receipt_col_created + " TEXT)";
        db.execSQL(CREATE_RECEIPT_TABLE);

        // create receipt items table
        String CREATE_RECEIPT_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS " + receipt_items_table +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                receipt_items_col_receipt_id + " INTEGER,"+
                receipt_items_col_product_id + " INTEGER,"+
                receipt_items_col_qty + " INTEGER)";
        db.execSQL(CREATE_RECEIPT_ITEMS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_EMPLOYEE_TABLE = "DROP TABLE IF EXISTS "+employee_table;
        db.execSQL(DROP_EMPLOYEE_TABLE);
        // drop newly added tables on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + product_table);
        db.execSQL("DROP TABLE IF EXISTS " + cart_table);
        db.execSQL("DROP TABLE IF EXISTS " + receipt_table);
        db.execSQL("DROP TABLE IF EXISTS " + receipt_items_table);
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

    // ------------- Product / Stock methods --------------
    public boolean addProduct(String name, int qty, String desc, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(product_col_name, name);
        cv.put(product_col_qty, qty);
        cv.put(product_col_desc, desc);
        cv.put(product_col_image, image);
        long id = db.insert(product_table, null, cv);
        db.close();
        return id != -1;
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, " + product_col_name + ", " + product_col_qty + ", " + product_col_desc + ", " + product_col_image + " FROM " + product_table, null);
    }

    public boolean increaseStock(int productId, int amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + product_table + " SET " + product_col_qty + " = " + product_col_qty + " + ? WHERE id = ?", new Object[]{amount, productId});
        db.close();
        return true;
    }

    public boolean decreaseStock(int productId, int amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + product_col_qty + " FROM " + product_table + " WHERE id = ?", new String[]{String.valueOf(productId)});
        if (c.moveToFirst()) {
            int current = c.getInt(0);
            if (current < amount) {
                c.close();
                db.close();
                return false;
            }
            int updated = current - amount;
            ContentValues cv = new ContentValues();
            cv.put(product_col_qty, updated);
            db.update(product_table, cv, "id = ?", new String[]{String.valueOf(productId)});
        }
        c.close();
        db.close();
        return true;
    }

    // ------------- Cart methods --------------
    public boolean addToCart(String email, int productId, int qty) {
        // Reserve stock and add to cart inside a transaction
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT " + product_col_qty + " FROM " + product_table + " WHERE id = ?", new String[]{String.valueOf(productId)});
            int stock = 0;
            if (c.moveToFirst()) {
                stock = c.getInt(0);
            }
            c.close();
            if (stock < qty) {
                db.endTransaction();
                db.close();
                return false;
            }

            // if product already in cart for this email, increase qty
            Cursor existing = db.rawQuery("SELECT id, qty FROM " + cart_table + " WHERE " + cart_col_email + " = ? AND " + cart_col_product_id + " = ?", new String[]{email, String.valueOf(productId)});
            if (existing.moveToFirst()) {
                int id = existing.getInt(0);
                int oldQty = existing.getInt(1);
                ContentValues cv = new ContentValues();
                cv.put(cart_col_qty, oldQty + qty);
                db.update(cart_table, cv, "id = ?", new String[]{String.valueOf(id)});
                existing.close();
            } else {
                existing.close();
                ContentValues cv = new ContentValues();
                cv.put(cart_col_email, email);
                cv.put(cart_col_product_id, productId);
                cv.put(cart_col_qty, qty);
                db.insert(cart_table, null, cv);
            }

            // decrease product stock to reserve
            ContentValues pv = new ContentValues();
            pv.put(product_col_qty, stock - qty);
            db.update(product_table, pv, "id = ?", new String[]{String.valueOf(productId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return true;
    }

    public Cursor getCartForEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT c.id, p.id as product_id, p." + product_col_name + ", p." + product_col_qty + ", c." + cart_col_qty + " FROM " + cart_table + " c JOIN " + product_table + " p ON c." + cart_col_product_id + " = p.id WHERE c." + cart_col_email + " = ?";
        return db.rawQuery(q, new String[]{email});
    }

    public void clearCartForEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(cart_table, cart_col_email + " = ?", new String[]{email});
        db.close();
    }

    public boolean removeCartItem(int cartId) {
        // restore stock for that cart item and delete the cart row
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT " + cart_col_product_id + ", " + cart_col_qty + " FROM " + cart_table + " WHERE id = ?", new String[]{String.valueOf(cartId)});
            if (!c.moveToFirst()) {
                c.close();
                db.endTransaction();
                db.close();
                return false;
            }
            int productId = c.getInt(0);
            int qty = c.getInt(1);
            c.close();

            // get current stock
            Cursor p = db.rawQuery("SELECT " + product_col_qty + " FROM " + product_table + " WHERE id = ?", new String[]{String.valueOf(productId)});
            int stock = 0;
            if (p.moveToFirst()) stock = p.getInt(0);
            p.close();

            ContentValues pv = new ContentValues();
            pv.put(product_col_qty, stock + qty);
            db.update(product_table, pv, "id = ?", new String[]{String.valueOf(productId)});

            db.delete(cart_table, "id = ?", new String[]{String.valueOf(cartId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return true;
    }

    // ------------- Receipt methods --------------
    public long createReceiptFromCart(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cart = db.rawQuery("SELECT c." + cart_col_product_id + ", c." + cart_col_qty + ", p." + product_col_name + " FROM " + cart_table + " c JOIN " + product_table + " p ON c." + cart_col_product_id + " = p.id WHERE c." + cart_col_email + " = ?", new String[]{email});
        if (cart.getCount() == 0) {
            cart.close();
            db.close();
            return -1;
        }

        // For simplicity total = sum(qty)
        int totalQty = 0;
        while (cart.moveToNext()) {
            totalQty += cart.getInt(1);
        }
        cart.moveToPosition(-1);

        ContentValues rv = new ContentValues();
        rv.put(receipt_col_email, email);
        rv.put(receipt_col_total, totalQty);
        rv.put(receipt_col_created, String.valueOf(System.currentTimeMillis()));
        long receiptId = db.insert(receipt_table, null, rv);
        if (receiptId == -1) {
            cart.close();
            db.close();
            return -1;
        }

        // insert receipt items (stock was already reserved when adding to cart)
        while (cart.moveToNext()) {
            int productId = cart.getInt(0);
            int qty = cart.getInt(1);
            ContentValues iv = new ContentValues();
            iv.put(receipt_items_col_receipt_id, receiptId);
            iv.put(receipt_items_col_product_id, productId);
            iv.put(receipt_items_col_qty, qty);
            db.insert(receipt_items_table, null, iv);
        }

        cart.close();
        // clear cart
        db.delete(cart_table, cart_col_email + " = ?", new String[]{email});
        db.close();
        return receiptId;
    }

    public Cursor getReceipts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, " + receipt_col_email + ", " + receipt_col_total + ", " + receipt_col_created + " FROM " + receipt_table + " ORDER BY id DESC", null);
    }

    public Cursor getReceiptItems(long receiptId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT ri." + receipt_items_col_qty + ", p." + product_col_name + " FROM " + receipt_items_table + " ri JOIN " + product_table + " p ON ri." + receipt_items_col_product_id + " = p.id WHERE ri." + receipt_items_col_receipt_id + " = ?";
        return db.rawQuery(q, new String[]{String.valueOf(receiptId)});
    }

    public boolean updateEmail(String currentEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col6_email, newEmail);

        // อัพเดตอีเมลในตาราง employee
        int result = db.update(employee_table, values, col6_email + " = ?", new String[]{currentEmail});

        // อัพเดตอีเมลในตาราง cart
        ContentValues cartValues = new ContentValues();
        cartValues.put(cart_col_email, newEmail);
        db.update(cart_table, cartValues, cart_col_email + " = ?", new String[]{currentEmail});

        // อัพเดตอีเมลในตาราง receipts
        ContentValues receiptValues = new ContentValues();
        receiptValues.put(receipt_col_email, newEmail);
        db.update(receipt_table, receiptValues, receipt_col_email + " = ?", new String[]{currentEmail});

        return result > 0;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col4_password, newPassword);

        int result = db.update(employee_table, values, col6_email + " = ?", new String[]{email});
        return result > 0;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + employee_table +
                      " WHERE " + col6_email + " = ? AND " +
                      col4_password + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
