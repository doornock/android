package cz.sodae.doornock.model.keys;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;

import cz.sodae.doornock.model.DatabaseHelper;
import cz.sodae.doornock.utils.security.keys.RSAEncryptUtil;

public class KeyRing
{
    private DatabaseHelper db;

    public KeyRing(Context context) {
        this.db = new DatabaseHelper(context);
    }

    /*
    public Key getPrimary()
    {
        List<Key> result = select("primary", new String[]{"1"}, null);
        if (result.size() == 0) return null;
        return result.get(0);
    }
    */

    public Key getById(Long id)
    {
        List<Key> result = select(db.COLUMN_KEYS_ID + " = ?", new String[]{id.toString()}, null);
        if (result.size() == 0) return null;
        return result.get(0);
    }

    public List<Key> findAll()
    {
        return select(null, null, null);
    }

    private List<Key> select(String selection, String[] selectionArgs, String orderBy)
    {
        List<Key> list = new LinkedList<>();
        try (SQLiteDatabase connection = db.getReadableDatabase()) {
            Cursor c = connection.query(db.TABLE_KEYS, new String[]{
                    db.COLUMN_KEYS_ID,
                    db.COLUMN_KEYS_TITLE,
                    db.COLUMN_KEYS_KEY_PRIVATE,
                    db.COLUMN_KEYS_KEY_PUBLIC
            }, selection, selectionArgs, null, null, orderBy);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                try {
                    list.add(
                        new Key(
                            RSAEncryptUtil.decodeBASE64(c.getString(2)),
                            RSAEncryptUtil.decodeBASE64(c.getString(3)),
                            c.getString(1)
                        ).setId(c.getLong(0))
                    );
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public Key persist(Key key)
    {
        try (SQLiteDatabase connection = db.getWritableDatabase()) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(db.COLUMN_KEYS_TITLE, key.getTitle());
            contentValues.put(db.COLUMN_KEYS_KEY_PRIVATE, RSAEncryptUtil.encodeBASE64(key.getPrivateKey().getEncoded()));
            contentValues.put(db.COLUMN_KEYS_KEY_PUBLIC, RSAEncryptUtil.encodeBASE64(key.getPublicKey().getEncoded()));

            if (key.getId() != null) {
                connection.update(db.TABLE_KEYS, contentValues, db.COLUMN_KEYS_ID + " = ?", new String[]{key.getId().toString()});
            } else {
                long id = connection.insert(db.TABLE_KEYS, null, contentValues);
                key.setId(id);
            }
        }

        return key;
    }

}
