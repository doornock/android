package cz.sodae.doornock.utils;

public class InvalidGUIDException extends Exception
{
    private String invalidGuid;

    public InvalidGUIDException(String guid) {
        super("Guid is invalid: " + guid);
        this.invalidGuid = guid;
    }

    public String getInvalidGUID() {
        return invalidGuid;
    }
}