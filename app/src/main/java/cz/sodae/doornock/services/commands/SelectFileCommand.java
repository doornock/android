package cz.sodae.doornock.services.commands;

import cz.sodae.doornock.utils.Bytes;

/**
 * Select file
 * http://www.cardwerk.com/smartcards/smartcard_standard_ISO7816-4_5_basic_organizations.aspx#table8
 */
public class SelectFileCommand {
    public static boolean isThisCommand(byte[] apdu) {
        return (apdu[0] & 0xFF) == 0x00 && (apdu[1] & 0xFF) == 0xA4;
    }

    public static byte[] getAPDU(String aid) {
        try {
            return new Command(
                    (byte) 0x00, (byte) 0xA4, // CLS, INS
                    (byte) 0x04, (byte) 0x00, // P1, P2
                    (byte) 0x00, // Le
                    Bytes.hexToBytes(aid)
            ).composeApdu();
        } catch (Command.ApduInvalidLengthException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}
