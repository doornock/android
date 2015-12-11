package cz.sodae.doornock.utils.commands;

public class SelectFileCommand extends BasicCommand
{
    private byte[] appId;

    public SelectFileCommand(byte[] appId) {
        super(
                (byte)0x00, // CLA	- Class - Class of instruction
                (byte)0xA4, // INS	- Instruction - Instruction code
                (byte)0x04, // P1	- Parameter 1 - Instruction parameter 1
                (byte)0x00, // P2	- Parameter 2 - Instruction parameter 2
                0, appId
        );
        this.appId = appId;
    }

    public static boolean isThisCommnad(byte[] apdu)
    {
        return apdu[0] == 0x00 && apdu[1] == 0xA4;
    }
}
