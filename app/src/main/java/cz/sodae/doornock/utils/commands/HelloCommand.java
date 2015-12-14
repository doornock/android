package cz.sodae.doornock.utils.commands;

public class HelloCommand extends BasicCommand
{
    private byte[] appId;

    public HelloCommand() {
        super(
                (byte)0x00, // CLA	- Class - Class of instruction
                (byte)0x03, // INS	- Instruction - Instruction code
                (byte)0x00, // P1	- Parameter 1 - Instruction parameter 1
                (byte)0x00, // P2	- Parameter 2 - Instruction parameter 2
                0, null
        );
    }

    public static boolean isThisCommnad(byte[] apdu)
    {
        return apdu[0] == 0x00 && apdu[1] == 0x03;
    }
}
