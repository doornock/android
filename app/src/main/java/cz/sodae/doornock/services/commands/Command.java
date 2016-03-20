package cz.sodae.doornock.services.commands;

import java.util.Arrays;

import cz.sodae.doornock.utils.Bytes;

public class Command
{

    protected byte defClass;

    protected byte defInstruction;

    protected byte defParameter;

    protected byte defParameterSecond;

    protected byte[] defData;

    protected int defMaximumLengthResponse;

    public Command(byte[] apdu) throws ApduInvalidLengthException {
        this.defClass = apdu[0];
        this.defInstruction = apdu[1];
        this.defParameter = apdu[2];
        this.defParameterSecond = apdu[3];
        this.defData = new byte[0]; // default

        if (apdu.length == 4) {
            this.defMaximumLengthResponse = 0;
            return;
        }

        // Algorithm to parse APDU Extended

        int LePosition = 0;
        int LcPosition = 0;
        int LcSize = 0;
        int dataSize = 0;
        int dataPosition = 0;

        if (apdu.length == 4 + 1) { // when there is only Le
            LePosition = 4;
        } else if (apdu[4] == 0x00) { // extended APDU, Lc/Le is/are 3 bytes
            if (apdu.length == 4 + 3) { // when only Le
                LePosition = 5;
            } else { // always when Lc = 3 bytes
                LcPosition = 5;
                LcSize = 2;
                dataSize = twoByteToInt(apdu[LcPosition], apdu[LcPosition + 1]);
                dataPosition = LcPosition + LcSize;
                LePosition = dataPosition + dataSize;
            }
        } else { // when Lc is 1 byte
            dataPosition = 5;
            dataSize = apdu[4];
            LePosition = dataPosition + dataSize;
        }

        if (LePosition != 0) {
            int max = 0;
            if (apdu.length == LePosition + 1 + 2) {
                max = twoByteToInt(apdu[LePosition], apdu[LePosition + 1]);
                this.defMaximumLengthResponse = max == 0 ? 65536 : max;
            } else if (apdu.length == LePosition + 1 + 1) {
                max = apdu[LePosition];
                this.defMaximumLengthResponse = max == 0 ? 256 : max;
            }
        }

        if (dataPosition != 0) {
            this.defData = Arrays.copyOfRange(apdu, dataPosition, dataPosition + dataSize);
        }

    }

    public Command(byte defClass, byte defInstruction, byte defParameter, byte defParameterSecond, int defMaximumLengthResponse, byte[] defData)
        throws ApduInvalidLengthException {
        this.defClass = defClass;
        this.defInstruction = defInstruction;
        this.defParameter = defParameter;
        this.defParameterSecond = defParameterSecond;
        this.defMaximumLengthResponse = defMaximumLengthResponse;
        this.defData = defData;

        if (defMaximumLengthResponse > 65535 || defMaximumLengthResponse < 0) {
            throw new ApduInvalidLengthException("Maximum of length is ");
        }
    }

    public Command(byte defClass, byte defInstruction, int defMaximumLengthResponse)
            throws ApduInvalidLengthException {
        this(defClass, defInstruction, (byte) 0x00, (byte) 0x00, defMaximumLengthResponse, new byte[0]);
    }

    public Command(byte defClass, byte defInstruction)
            throws ApduInvalidLengthException {
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


    public byte[] composeApdu() throws ApduInvalidLengthException
    {
        if (defMaximumLengthResponse > 256) {
            throw new ApduInvalidLengthException("Classic APDU has maximum size of response 256");
        }
        if (defData.length > 255) {
            throw new ApduInvalidLengthException("Classic APDU has maximum size of data 255");
        }

        byte[] header = {
                defClass,
                defInstruction,
                defParameter,
                defParameterSecond,
        };

        return Bytes.concatenate(
                header,
                new byte[] {(byte) (0xFF & (defData.length))},
                defData,
                new byte[] {(byte) (0xFF & (defMaximumLengthResponse == 256 ? 0 : defMaximumLengthResponse))}
        );
    }

    public byte[] composeApduExtended() throws ApduInvalidLengthException
    {
        byte[] header = {
                defClass,
                defInstruction,
                defParameter,
                defParameterSecond,
        };


        byte[] Lc;
        byte[] Le;

        int dataSize = getData().length;

        if (dataSize > 0) {
            Lc = Bytes.concatenate(new byte[] {0x00}, intToTwoByte(dataSize), defData);
        } else {
            Lc = new byte[0];
        }

        byte[] LePart = intToTwoByte(defMaximumLengthResponse == 65536 ? 0 : defMaximumLengthResponse);
        if (defMaximumLengthResponse == 0) {
            Le = new byte[0];
        } else if (dataSize > 0) {
            Le = LePart;
        } else {
            Le = Bytes.concatenate(new byte[] {0x00}, LePart);
        }

        return Bytes.concatenate(header, Lc, Le);
    }

    private int twoByteToInt(byte upper, byte lower) {
        return ((upper & 0xff) << 8) | (lower & 0xff);
    }


    private byte[] intToTwoByte(int number)
    {
        if (number < 0) {
            return new byte[0];
        }

        return new byte[] {
                (byte) ((number >> 8) & 0xFF),
                (byte) (number & 0xFF)
        };
    }


    public class ApduInvalidLengthException extends Exception
    {
        public ApduInvalidLengthException(String detailMessage) {
            super(detailMessage);
        }
    }

}
