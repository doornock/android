package cz.sodae.doornock.model.site;

import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.utils.GuidPattern;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class Site {

    /** Internal id */
    private Long id;

    /** Doornock GUID */
    private String guid;

    /** API URL */
    private String url;

    /** Title of Doornock site */
    private String title;

    /** Username to log in */
    private String username;

    /** Password to log in */
    private String password;

    /** Id of registered device */
    private String deviceId;

    /** Secret api key of registered device */
    private String apiKey;

    /** Key to use NFC authentication */
    private Key key;

    private boolean requireUnlock;

    public Site(String url) {
        this.url = url;
    }


    public Long getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public Key getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isDeviceRegistred() {
        return apiKey != null;
    }

    public boolean isRequiredUnlock() {
        return requireUnlock;
    }

    public Site setId(Long id) {
        this.id = id;
        return this;
    }

    public Site setGuid(String guid) throws InvalidGUIDException {
        GuidPattern.validOrThrow(guid);
        this.guid = guid.toUpperCase();
        return this;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Site setTitle(String title) {
        this.title = title;
        return this;
    }

    public Site setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public Site setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public Site setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public Site setRequiredUnlock(boolean requireUnlock) {
        this.requireUnlock = requireUnlock;
        return this;
    }
}
