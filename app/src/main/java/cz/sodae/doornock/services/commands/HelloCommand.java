package cz.sodae.doornock.services.commands;

public class HelloCommand
{
    public static boolean isThisCommand(byte[] apdu) {
        return apdu[0] == 0xD0 && apdu[1] == 0x03;
    }
}
