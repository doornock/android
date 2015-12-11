package cz.sodae.doornock.model.site;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_SITES = "sites";
    public static final String COLUMN_SITES_ID = "_id";
    public static final String COLUMN_SITES_USERNAME = "username";
    public static final String COLUMN_SITES_PASSWORD = "password";
    public static final String COLUMN_SITES_TITLE = "title";
    public static final String COLUMN_SITES_API_URL = "api_url";
    public static final String COLUMN_SITES_API_KEY = "api_key";
    public static final String COLUMN_SITES_KEY = "key_id";

    private static final String DATABASE_NAME = "db.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE =
            "create table " + TABLE_SITES + "("
                + COLUMN_SITES_ID + " integer primary key autoincrement, "
                + COLUMN_SITES_USERNAME + " varchar(255) not null, "
                + COLUMN_SITES_PASSWORD + " varchar(255) not null, "
                + COLUMN_SITES_TITLE + " varchar(255) not null, "
                + COLUMN_SITES_API_URL + " text not null, "
                + COLUMN_SITES_API_KEY + " varchar(255) not null, "
                + COLUMN_SITES_KEY + " int not null"
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SITES);
        onCreate(db);
    }

}
