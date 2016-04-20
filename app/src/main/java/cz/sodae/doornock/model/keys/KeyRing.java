package cz.sodae.doornock.model.keys;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;

import cz.sodae.doornock.model.DatabaseHelper;

public class KeyRing {
    private DatabaseHelper db;

    public KeyRing(Context context) {
        this.db = new DatabaseHelper(context);
    }

    /**
     * Return Key in database
     *
     * @param id key id
     * @return Key, or null if not found
     */
    public Key getById(Long id) {
        List<Key> result = select(DatabaseHelper.COLUMN_KEYS_ID + " = ?", new String[]{id.toString()}, null);
        if (result.size() == 0) return null;
        return result.get(0);
    }

    /**
     * Remove key from db
     *
     * @param key
     * @return is really removed from database. If key is not saved, it cannot be deleted
     */
    public boolean remove(Key key) {
        try (SQLiteDatabase connection = db.getWritableDatabase()) {
            if (key.getId() != 0) {
                return connection.delete(
                        DatabaseHelper.TABLE_SITES,
                        DatabaseHelper.COLUMN_KEYS_ID + " = ?",
                        new String[]{key.getId().toString()}
                ) > 0;
            }
        }
        return false;
    }

    /**
     * @return non filtered list of keys
     */
    public List<Key> findAll() {
        return select(null, null, null);
    }

    private List<Key> select(String selection, String[] selectionArgs, String orderBy) {
        List<Key> list = new LinkedList<>();
        try (SQLiteDatabase connection = db.getReadableDatabase()) {
            try (Cursor c = connection.query(DatabaseHelper.TABLE_KEYS, new String[]{
                    DatabaseHelper.COLUMN_KEYS_ID,
                    DatabaseHelper.COLUMN_KEYS_TITLE,
                    DatabaseHelper.COLUMN_KEYS_KEY_PRIVATE,
                    DatabaseHelper.COLUMN_KEYS_KEY_PUBLIC
            }, selection, selectionArgs, null, null, orderBy)) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    try {
                        list.add(
                                new Key(
                                        Base64.decode(c.getString(2), Base64.DEFAULT),
                                        Base64.decode(c.getString(3), Base64.DEFAULT),
                                        c.getString(1)
                                ).setId(c.getLong(0))
                        );
                        c.moveToNext();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return list;
    }


    /**
     * Save data about key. If is not already saved, it fill id
     */
    public Key save(Key key) {
        try (SQLiteDatabase connection = db.getWritableDatabase()) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.COLUMN_KEYS_TITLE, key.getTitle());
            contentValues.put(DatabaseHelper.COLUMN_KEYS_KEY_PRIVATE, Base64.encodeToString(key.getPrivateKey().getEncoded(), Base64.DEFAULT));
            contentValues.put(DatabaseHelper.COLUMN_KEYS_KEY_PUBLIC, Base64.encodeToString(key.getPublicKey().getEncoded(), Base64.DEFAULT));

            if (key.getId() != null) {
                connection.update(DatabaseHelper.TABLE_KEYS, contentValues, DatabaseHelper.COLUMN_KEYS_ID + " = ?", new String[]{key.getId().toString()});
            } else {
                long id = connection.insert(DatabaseHelper.TABLE_KEYS, null, contentValues);
                key.setId(id);
            }
        }

        return key;
    }

}
