package cz.sodae.doornock.model.site;

import cz.sodae.doornock.model.keys.Key;

public class Site
{
    private Long id;

    private String url;
    private String description;

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

    public Key getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
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

    public Site setId(Long id) {
        this.id = id;
        return this;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Site setDescription(String description) {
        this.description = description;
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
