package cz.sodae.doornock.services.commands;

public class SignCommand
{
    // http://www.cardwerk.com/smartcards/smartcard_standard_ISO7816-4_5_basic_organizations.aspx#table8
    public static boolean isThisCommand(byte[] apdu) {
        return apdu[0] == 0xD0 && apdu[1] == 0x02;
    }
}
