package cz.sodae.doornock.services.commands;

public class SignCommand
{
    // http://www.cardwerk.com/smartcards/smartcard_standard_ISO7816-4_5_basic_organizations.aspx#table8
    // @todo change CLS to D0 to FE
    public static boolean isThisCommand(byte[] apdu)
    {
        return apdu[0] == 0x00 && apdu[1] == 0x02;
    }
}
