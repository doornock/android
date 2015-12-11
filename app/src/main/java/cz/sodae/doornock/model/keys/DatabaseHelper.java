package cz.sodae.doornock.model.keys;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_KEYS = "keys";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_KEY_PRIVATE = "private_key";
    public static final String COLUMN_KEY_PUBLIC = "public_key";

    private static final String DATABASE_NAME = "db.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE =
            "create table " + TABLE_KEYS + "("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_TITLE + " varchar(255) not null, "
                + COLUMN_KEY_PRIVATE + " BLOB not null, "
                + COLUMN_KEY_PUBLIC + " BLOB not null "
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(
                DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEYS);
        onCreate(db);
    }

}
