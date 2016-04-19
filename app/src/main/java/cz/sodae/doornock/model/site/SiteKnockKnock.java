package cz.sodae.doornock.model.site;

import cz.sodae.doornock.utils.GuidPattern;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class SiteKnockKnock {

    private String title;
    private String guid;

    public SiteKnockKnock(String guid, String title) throws InvalidGUIDException {
        this.guid = guid;
        this.title = title;
        GuidPattern.validOrThrow(guid);
    }

    public String getTitle() {
        return title;
    }

    public String getGuid() {
        return guid;
    }
}
