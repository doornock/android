package cz.sodae.doornock.services.commands;

/**
 * Hello command receive GUID of Doornock network in data and return deviceId
 */
public class HelloCommand {
    public static boolean isThisCommand(byte[] apdu) {
        return (apdu[0] & 0xFF) == 0xD0 && (apdu[1] & 0xFF) == 0x03;
    }
}
