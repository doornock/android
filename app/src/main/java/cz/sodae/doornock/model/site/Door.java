package cz.sodae.doornock.model.site;

/**
 * Entity representing Door from API
 */
public class Door {

    private String id;

    private String title;

    private boolean access;


    public Door(String id, String title, boolean access) {
        this.id = id;
        this.title = title;
        this.access = access;
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasAccess() {
        return access;
    }
}
