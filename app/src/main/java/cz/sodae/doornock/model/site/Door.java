package cz.sodae.doornock.model.site;

public class Door
{

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

    public boolean isAccess() {
        return access;
    }
}
