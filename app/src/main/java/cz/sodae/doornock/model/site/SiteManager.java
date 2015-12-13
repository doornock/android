package cz.sodae.doornock.model.site;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import cz.sodae.doornock.model.DatabaseHelper;
import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.model.keys.KeyRing;
import cz.sodae.doornock.utils.GuidPattern;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class SiteManager
{

    private DatabaseHelper db;

    private KeyRing keyRing;

    private SiteApi api;

    public SiteManager(Context context) {
        this.db = new DatabaseHelper(context);
        this.api = new SiteApi();
        this.keyRing = new KeyRing(context);
    }

    public SiteKnockKnock create(String url) throws SiteApi.ApiException, InvalidGUIDException {
        return api.knockKnock(url);
    }

    public Site register(Site site) throws SiteApi.RegistrationFailedException {
        api.register(site);
        this.persist(site);
        return site;
    }

    // key is nullable, if key is null, will be generated!
    public Site addDevice(Site site, String deviceDescription, Key key) throws SiteApi.AddDeviceFailedException {
        if (key == null) {
            key = Key.generateKey(site.getTitle());
        }
        api.addDevice(site, key, deviceDescription);
        this.persist(site);
        return site;
    }


    public Site updateDevice(Site site, Key key) {
        api.updateDevice(site, key);
        site.setKey(key);
        this.persist(site);
        return site;
    }


    public Site getByGuid(String guid) throws InvalidGUIDException
    {
        GuidPattern.validOrThrow(guid);
        guid = guid.toUpperCase();
        List<Site> result = select(db.COLUMN_SITES_GUID + " = ?", new String[]{guid}, null);
        if (result.size() == 0) return null;
        return result.get(0);
    }


    public List<Site> findAll()
    {
        return select(null, null, null);
    }

    private List<Site> select(String selection, String[] selectionArgs, String orderBy) {
        List<Site> list = new LinkedList<>();
        try (SQLiteDatabase connection = db.getReadableDatabase()) {
            Cursor c = connection.query(db.TABLE_SITES, new String[]{
                    db.COLUMN_SITES_ID,
                    db.COLUMN_SITES_API_URL,
                    db.COLUMN_SITES_API_KEY,
                    db.COLUMN_SITES_TITLE,
                    db.COLUMN_SITES_KEY,
                    db.COLUMN_SITES_USERNAME,
                    db.COLUMN_SITES_PASSWORD
            }, selection, selectionArgs, null, null, orderBy);
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

                list.add(site);
                c.moveToNext();
            }
        }
        return list;
    }

    public Site persist(Site site)
    {
        try (SQLiteDatabase connection = db.getWritableDatabase()) {

            if (site.getKey() != null && site.getKey().getId() == null) {
                keyRing.persist(site.getKey());
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(db.COLUMN_SITES_GUID, site.getGuid());
            contentValues.put(db.COLUMN_SITES_TITLE, site.getTitle());
            contentValues.put(db.COLUMN_SITES_API_URL, site.getUrl());
            contentValues.put(db.COLUMN_SITES_API_KEY, site.getApiKey());
            contentValues.put(db.COLUMN_SITES_KEY, site.getKey() != null ? site.getKey().getId() : null);
            contentValues.put(db.COLUMN_SITES_USERNAME, site.getUsername());
            contentValues.put(db.COLUMN_SITES_PASSWORD, site.getPassword());

            if (site.getId() != null) {
                connection.update(db.TABLE_SITES, contentValues, db.COLUMN_SITES_ID + " = ?", new String[]{site.getId().toString()});
            } else {
                long id = connection.insert(db.TABLE_SITES, null, contentValues);
                site.setId(id);
            }
        }

        return site;
    }


}
