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

    private int twoByteToInt(byte upper, byte lower) {
        return ((upper & 0xff) << 8) | (lower & 0xff);
    }


    public Command(byte[] apdu) throws ApduInvalidLengthException {
        this(
                apdu[0], apdu[1], apdu[2], apdu[3],
                (int) apdu[apdu.length - 1],
                Arrays.copyOfRange(apdu, 5, 5 + apdu[4])
        );

        this.defClass = apdu[0];
        this.defInstruction = apdu[1];
        this.defParameter = apdu[2];
        this.defParameterSecond = apdu[3];
        this.defData = new byte[0]; // default

        if (apdu.length == 4) {
            this.defMaximumLengthResponse = 0;
            return;
        }

        if (apdu[4] == 0x00) { // 5th byte is zero - APDU extended
            if (apdu.length == 4 + 3) { // header + 3 bytes Le
                if (apdu[5] == 0x00 && apdu[6] == 0x00) {
                    this.defMaximumLengthResponse = 65536;
                } else {
                    this.defMaximumLengthResponse = twoByteToInt(apdu[5], apdu[6]);
                }
            } else if (apdu.length == 4 + 1) { // header + 1 bytes Le
                if (apdu[5] == 0x00) {
                    this.defMaximumLengthResponse = 256;
                } else {
                    this.defMaximumLengthResponse = apdu[5];
                }

            } else { // header + Lc + Nc + Le
                int contentSize = twoByteToInt(apdu[5], apdu[6]);
                this.defData = Arrays.copyOfRange(apdu, 7, 7 + contentSize);

                int LePosition = 4 + 3 + contentSize - 1;
                if (apdu.length == (LePosition + 1)) { // header + Lc + content + (1 byte) Le
                    if (apdu[LePosition] == 0x00) {
                        this.defMaximumLengthResponse = 256;
                    } else {
                        this.defMaximumLengthResponse = apdu[LePosition];
                    }
                } else if (apdu.length == (LePosition + 1 + 2)) { // 2 byte Le
                    if (apdu[LePosition] == 0x00 && apdu[LePosition+1] == 0x00) {
                        this.defMaximumLengthResponse = 65536;
                    } else {
                        this.defMaximumLengthResponse = twoByteToInt(apdu[LePosition], apdu[LePosition+1]);
                    }
                }
            }
        } else {
            this.defData = Arrays.copyOfRange(apdu, 4, 4 + apdu[4]);
            int LePosition = 4 + 1 + apdu[4] - 1;
            if (apdu.length == LePosition + 1 + 2) {
                if (apdu[LePosition] == 0x00 && apdu[LePosition+1] == 0x00) {
                    this.defMaximumLengthResponse = 65536;
                } else {
                    this.defMaximumLengthResponse = twoByteToInt(apdu[LePosition], apdu[LePosition+1]);
                }
            } else if (apdu.length == LePosition + 1 + 1) {
                if (apdu[LePosition] == 0x00) {
                    this.defMaximumLengthResponse = 256;
                } else {
                    this.defMaximumLengthResponse = apdu[LePosition];
                }
            }
        }

    }

    public Command(byte defClass, byte defInstruction, byte defParameter, byte defParameterSecond, int defMaximumLengthResponse, byte[] defData) {
        this.defClass = defClass;
        this.defInstruction = defInstruction;
        this.defParameter = defParameter;
        this.defParameterSecond = defParameterSecond;
        this.defMaximumLengthResponse = defMaximumLengthResponse;
        this.defData = defData;
    }

    public Command(byte defClass, byte defInstruction, int defMaximumLengthResponse) {
        this(defClass, defInstruction, (byte) 0x00, (byte) 0x00, defMaximumLengthResponse, new byte[0]);
    }

    public Command(byte defClass, byte defInstruction) {
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
                (byte) defMaximumLengthResponse
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
