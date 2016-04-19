package cz.sodae.doornock.services.commands;

/**
 * Select file
 * http://www.cardwerk.com/smartcards/smartcard_standard_ISO7816-4_5_basic_organizations.aspx#table8
 */
public class SelectFileCommand {
    public static boolean isThisCommand(byte[] apdu) {
        return (apdu[0] & 0xFF) == 0x00 && (apdu[1] & 0xFF) == 0xA4;
    }
}
