package cz.sodae.doornock.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_KEYS = "keys";
    public static final String COLUMN_KEYS_ID = "_id";
    public static final String COLUMN_KEYS_TITLE = "title";
    public static final String COLUMN_KEYS_KEY_PRIVATE = "private_key";
    public static final String COLUMN_KEYS_KEY_PUBLIC = "public_key";

    public static final String TABLE_SITES = "sites";
    public static final String COLUMN_SITES_ID = "_id";
    public static final String COLUMN_SITES_GUID = "guid";
    public static final String COLUMN_SITES_USERNAME = "username";
    public static final String COLUMN_SITES_PASSWORD = "password";
    public static final String COLUMN_SITES_TITLE = "title";
    public static final String COLUMN_SITES_DEVICE_ID = "device_id";
    public static final String COLUMN_SITES_API_URL = "api_url";
    public static final String COLUMN_SITES_API_KEY = "api_key";
    public static final String COLUMN_SITES_KEY = "key_id";

    private static final String DATABASE_NAME = "db.db";

    /**
     * Create a migration {@link #onUpgrade} when you create new version instead of destroy DB
     */
    private static final int DATABASE_VERSION = 4;

    // Database creation sql statement
    private static final String DATABASE_CREATE_KEYS =
            "create table " + TABLE_KEYS + "("
                + COLUMN_KEYS_ID + " integer primary key autoincrement, "
                + COLUMN_KEYS_TITLE + " varchar(255) not null, "
                + COLUMN_KEYS_KEY_PRIVATE + " BLOB not null, "
                + COLUMN_KEYS_KEY_PUBLIC + " BLOB not null "
            + ");";

    // Database creation sql statement
    private static final String DATABASE_CREATE_SITES =
            "create table " + TABLE_SITES + "("
                    + COLUMN_SITES_ID + " integer primary key autoincrement, "
                    + COLUMN_SITES_GUID + " varchar(255) null, "
                    + COLUMN_SITES_USERNAME + " varchar(255) null, "
                    + COLUMN_SITES_PASSWORD + " varchar(255) null, "
                    + COLUMN_SITES_TITLE + " varchar(255) null, "
                    + COLUMN_SITES_API_URL + " text not null, "
                    + COLUMN_SITES_API_KEY + " varchar(255) null, "
                    + COLUMN_SITES_DEVICE_ID + " varchar(255) null, "
                    + COLUMN_SITES_KEY + " int null"
                    + ");";

    private static final String DATABASE_CREATE_INDEX_SITES =
            "CREATE UNIQUE INDEX unique_guid " +
                    "ON " + TABLE_SITES + " (" + COLUMN_SITES_GUID + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_KEYS);
        database.execSQL(DATABASE_CREATE_SITES);
        database.execSQL(DATABASE_CREATE_INDEX_SITES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(
                DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SITES);
        onCreate(db);
    }

}
