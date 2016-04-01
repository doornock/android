package cz.sodae.doornock.services.commands;

public class SelectFileCommand
{

    public static boolean isThisCommand(byte[] apdu) {
        return (apdu[0] & 0xFF) == 0x00 && (apdu[1] & 0xFF) == 0xA4;
    }
}
