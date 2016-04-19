package cz.sodae.doornock.utils;

import java.util.regex.Pattern;

public class GuidPattern {

    final static Pattern GUID_PATTERN = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");

    public static boolean validate(String guid) {
        return guid == null || GUID_PATTERN.matcher(guid).matches();
    }

    public static void validOrThrow(String guid) throws InvalidGUIDException {
        if (!validate(guid))
            throw new InvalidGUIDException(guid);
    }

}