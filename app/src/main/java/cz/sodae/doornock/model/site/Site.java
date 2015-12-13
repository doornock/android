package cz.sodae.doornock.model.site;

import cz.sodae.doornock.model.keys.Key;
import cz.sodae.doornock.utils.GuidPattern;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class Site
{
    private Long id;
    private String guid;

    private String url;
    private String title;

    private String username;
    private String password;

    private String apiKey;

    private Key key;

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


    public boolean isDeviceRegistred()
    {
        return apiKey != null;
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

    public Site setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }


}
