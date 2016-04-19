package cz.sodae.doornock.services.commands;

/**
 * Sign command provide singing a input data and response signed data
 */
public class SignCommand {
    public static boolean isThisCommand(byte[] apdu) {
        return (apdu[0] & 0xFF) == 0xD0 && (apdu[1] & 0xFF) == 0x02;
    }
}
