package cz.sodae.doornock.utils.commands;

import java.util.Arrays;

import cz.sodae.doornock.utils.Bytes;

public class BasicCommand
{

    private static final byte[] A_OKAY = {
            (byte)0x90,  // SW1	Status byte 1 - Command processing status
            (byte)0x00   // SW2	Status byte 2 - Command processing qualifier
    };

    protected byte defClass;

    protected byte defInstruction;

    protected byte defParameter;

    protected byte defParameterSecond;

    protected byte[] defData;

    protected int defExceptedLengthResponse;


    public BasicCommand(byte[] apdu) throws ApduInvalidLengthException{
        this(
                apdu[0], apdu[1], apdu[2], apdu[3],
                (int) apdu[apdu.length - 1],
                Arrays.copyOfRange(apdu, 5, 5 + apdu[4])
        );

        if (apdu.length != apdu[4] + 6) { // Lc + count of headers bytes (6) must be same as apdu bytes
            throw new ApduInvalidLengthException("Excepted");
        }
    }

    public BasicCommand(byte defClass, byte defInstruction, byte defParameter, byte defParameterSecond, int defExceptedLengthResponse, byte[] defData) {
        this.defClass = defClass;
        this.defInstruction = defInstruction;
        this.defParameter = defParameter;
        this.defParameterSecond = defParameterSecond;
        this.defExceptedLengthResponse = defExceptedLengthResponse;
        this.defData = defData;
    }

    public BasicCommand(byte defClass, byte defInstruction, int defExceptedLengthResponse) {
        this(defClass, defInstruction, (byte) 0x00, (byte) 0x00, defExceptedLengthResponse, new byte[0]);
    }

    public BasicCommand(byte defClass, byte defInstruction) {
        this(defClass, defInstruction, 0);
    }

    public byte getDefClass() {
        return defClass;
    }

    public byte getDefInstruction() {
        return defInstruction;
    }

    public byte getDefParameter() {
        return defParameter;
    }

    public byte getDefParameterSecond() {
        return defParameterSecond;
    }

    public byte[] getData()
    {
        return defData;
    }

    public byte[] composeAPDU()
    {
        byte[] header = {
                defClass,
                defInstruction,
                defParameter,
                defParameterSecond,
                (byte) defData.length
        };

        byte[] tail = {
                (byte) defExceptedLengthResponse
        };

        return Bytes.concatenate(header, getData(), tail);
    }


    public class ApduInvalidLengthException extends Exception
    {
        public ApduInvalidLengthException(String detailMessage) {
            super(detailMessage);
        }
    }

}
