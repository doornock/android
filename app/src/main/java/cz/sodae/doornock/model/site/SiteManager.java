package cz.sodae.doornock.model.site;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

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

    public SiteKnockKnock create(String url) throws SiteApi.SiteApiException, InvalidGUIDException {
        return api.knockKnock(url);
    }



    public Site register(Site site) throws RegistrationFailedException {
        try {
            api.register(site);
            this.save(site);
            return site;
        } catch (SiteApi.SiteApiException e) {
            throw new RegistrationFailedException(e);
        }
    }

    // key is nullable, if key is null, will be generated!
    public Site registerDevice(Site site, String deviceDescription) throws AddDeviceFailedException {
        try {
            Key key = Key.generateKey(site.getTitle());
            api.addDevice(site, key, deviceDescription);
            site.setKey(key);
            this.save(site);
            return site;
        } catch (SiteApi.SiteApiException e) {
            throw new AddDeviceFailedException(e);
        }
    }


    public Site updateDevice(Site site, Key key) throws SiteApi.SiteApiException {
        api.updateDevice(site, key);
        site.setKey(key);
        this.save(site);
        return site;
    }



    public boolean remove(Site site)
    {
        if (site.getKey() != null) {
            keyRing.remove(site.getKey());
        }
        try (SQLiteDatabase connection = db.getWritableDatabase()) {
            if (site.getId() != 0) {
                connection.delete(db.TABLE_SITES, db.COLUMN_KEYS_ID + " = ?", new String[]{site.getId().toString()});
                return true;
            }
        }
        return false;
    }


    public List<Door> findDoor(Site site) throws FindDoorsException
    {
        try {
            return this.api.findDoors(site);
        } catch (SiteApi.SiteApiException e) {
            throw new FindDoorsException(e);
        }
    }


    public void openDoor(Site site, Door door) throws OpenDoorException
    {
        try {
            this.api.openDoor(site, door);
        } catch (SiteApi.SiteApiException e) {
            throw new OpenDoorException(e);
        }
    }



    public List<Site> findAll()
    {
        return select(null, null, null);
    }


    public Site getByGuid(String guid) throws InvalidGUIDException
    {
        GuidPattern.validOrThrow(guid);
        guid = guid.toUpperCase();
        List<Site> result = select(db.COLUMN_SITES_GUID + " = ?", new String[]{guid}, null);
        if (result.size() == 0) return null;
        return result.get(0);
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
                    db.COLUMN_SITES_PASSWORD,
                    db.COLUMN_SITES_DEVICE_ID,
                    db.COLUMN_SITES_GUID
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
        return list;
    }

    public Site save(Site site)
    {
        try (SQLiteDatabase connection = db.getWritableDatabase()) {

            if (site.getKey() != null && site.getKey().getId() == null) {
                keyRing.save(site.getKey());
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(db.COLUMN_SITES_GUID, site.getGuid());
            contentValues.put(db.COLUMN_SITES_TITLE, site.getTitle());
            contentValues.put(db.COLUMN_SITES_API_URL, site.getUrl());
            contentValues.put(db.COLUMN_SITES_API_KEY, site.getApiKey());
            contentValues.put(db.COLUMN_SITES_DEVICE_ID, site.getDeviceId());
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




    public class RegistrationFailedException extends Exception
    {
        public RegistrationFailedException() {
        }

        public RegistrationFailedException(String detailMessage) {
            super(detailMessage);
        }

        public RegistrationFailedException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public RegistrationFailedException(Throwable throwable) {
            super(throwable);
        }
    }

    public class AddDeviceFailedException extends Exception
    {
        public AddDeviceFailedException() {
        }

        public AddDeviceFailedException(String detailMessage) {
            super(detailMessage);
        }

        public AddDeviceFailedException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public AddDeviceFailedException(Throwable throwable) {
            super(throwable);
        }
    }

    public class FindDoorsException extends Exception
    {
        public FindDoorsException() {
        }

        public FindDoorsException(String detailMessage) {
            super(detailMessage);
        }

        public FindDoorsException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public FindDoorsException(Throwable throwable) {
            super(throwable);
        }
    }

    public class OpenDoorException extends Exception
    {
        public OpenDoorException() {
        }

        public OpenDoorException(String detailMessage) {
            super(detailMessage);
        }

        public OpenDoorException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public OpenDoorException(Throwable throwable) {
            super(throwable);
        }
    }

}
