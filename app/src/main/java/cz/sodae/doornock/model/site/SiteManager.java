package cz.sodae.doornock.model.site;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

import cz.sodae.doornock.model.DatabaseHelper;
import cz.sodae.doornock.model.keys.KeyRing;
import cz.sodae.doornock.utils.GuidPattern;
import cz.sodae.doornock.utils.InvalidGUIDException;


public class SiteManager {

    private DatabaseHelper db;

    private KeyRing keyRing;

    public SiteManager(Context context) {
        this.db = new DatabaseHelper(context);
        this.keyRing = new KeyRing(context);
    }


    /**
     * Remove site and their key
     *
     * @param site
     * @return is really removed from database. If site is not saved, it cannot be deleted
     */
    public boolean remove(Site site) {
        if (site.getKey() != null) {
            keyRing.remove(site.getKey());
        }
        try (SQLiteDatabase connection = db.getWritableDatabase()) {
            if (site.getId() != 0) {
                return connection.delete(
                        DatabaseHelper.TABLE_SITES,
                        DatabaseHelper.COLUMN_KEYS_ID + " = ?",
                        new String[]{site.getId().toString()}
                ) > 0;
            }
        }
        return false;
    }

    /**
     * @return non filtered list registered sites
     */
    public List<Site> findAll() {
        return select(null, null, null);
    }


    /**
     * Return Site by GUID
     *
     * @param guid certain guid of Doornock site
     * @return Doornock site, or null if not found
     */
    public Site getByGuid(String guid) throws InvalidGUIDException {
        GuidPattern.validOrThrow(guid);
        guid = guid.toUpperCase();
        List<Site> result = select(DatabaseHelper.COLUMN_SITES_GUID + " = ?", new String[]{guid}, null);
        if (result.size() == 0) return null;
        return result.get(0);
    }

    private List<Site> select(String selection, String[] selectionArgs, String orderBy) {
        List<Site> list = new LinkedList<>();
        try (SQLiteDatabase connection = db.getReadableDatabase()) {
            try (Cursor c = connection.query(DatabaseHelper.TABLE_SITES, new String[]{
                    DatabaseHelper.COLUMN_SITES_ID,
                    DatabaseHelper.COLUMN_SITES_API_URL,
                    DatabaseHelper.COLUMN_SITES_API_KEY,
                    DatabaseHelper.COLUMN_SITES_TITLE,
                    DatabaseHelper.COLUMN_SITES_KEY,
                    DatabaseHelper.COLUMN_SITES_USERNAME,
                    DatabaseHelper.COLUMN_SITES_PASSWORD,
                    DatabaseHelper.COLUMN_SITES_DEVICE_ID,
                    DatabaseHelper.COLUMN_SITES_GUID
            }, selection, selectionArgs, null, null, orderBy)) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    Site site = new Site(c.getString(1))
                            .setId(c.getLong(0));
                    if (!c.isNull(3))
                        site.setTitle(c.getString(3));

                    if (!c.isNull(2))
                        site.setApiKey(c.getString(2));

                    if (!c.isNull(4))
                        site.setKey(keyRing.getById(c.getLong(4)));

                    if (!(c.isNull(5) || c.isNull(6)))
                        site.setCredentials(c.getString(5), c.getString(6));

                    if (!(c.isNull(7)))
                        site.setDeviceId(c.getString(7));

                    if (!c.isNull(8))
                        try {
                            site.setGuid(c.getString(8));
                        } catch (InvalidGUIDException e) {
                            e.printStackTrace();
                        }

                    list.add(site);
                    c.moveToNext();
                }
            }
        }
        return list;
    }

    /**
     * Save data about site. If is not already saved, it fill id
     */
    public Site save(Site site) {
        try (SQLiteDatabase connection = db.getWritableDatabase()) {

            if (site.getKey() != null && site.getKey().getId() == null) {
                keyRing.save(site.getKey());
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.COLUMN_SITES_GUID, site.getGuid());
            contentValues.put(DatabaseHelper.COLUMN_SITES_TITLE, site.getTitle());
            contentValues.put(DatabaseHelper.COLUMN_SITES_API_URL, site.getUrl());
            contentValues.put(DatabaseHelper.COLUMN_SITES_API_KEY, site.getApiKey());
            contentValues.put(DatabaseHelper.COLUMN_SITES_DEVICE_ID, site.getDeviceId());
            contentValues.put(DatabaseHelper.COLUMN_SITES_KEY, site.getKey() != null ? site.getKey().getId() : null);
            contentValues.put(DatabaseHelper.COLUMN_SITES_USERNAME, site.getUsername());
            contentValues.put(DatabaseHelper.COLUMN_SITES_PASSWORD, site.getPassword());

            if (site.getId() != null) {
                connection.update(DatabaseHelper.TABLE_SITES, contentValues, DatabaseHelper.COLUMN_SITES_ID + " = ?", new String[]{site.getId().toString()});
            } else {
                long id = connection.insert(DatabaseHelper.TABLE_SITES, null, contentValues);
                site.setId(id);
            }
        }

        return site;
    }


}
