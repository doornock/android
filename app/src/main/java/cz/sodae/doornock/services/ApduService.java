package cz.sodae.doornock.services;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.InputStream;

import cz.sodae.doornock.R;
import cz.sodae.doornock.utils.Bytes;
import cz.sodae.doornock.utils.commands.BasicCommand;
import cz.sodae.doornock.utils.FileLoader;

public class ApduService extends HostApduService
{

    private static final String TAG = "Doornock/APDU";

    private static final byte[] A_OKAY = {
            (byte)0x90,  // SW1	Status byte 1 - Command processing status
            (byte)0x00   // SW2	Status byte 2 - Command processing qualifier
    };

    private static final byte[] A_ERROR_INVALID_AUTH = { // @todo fix code
            (byte)0x66,  // SW1	Status byte 1 - Command processing status
            (byte)0x02   // SW2	Status byte 2 - Command processing qualifier
    };

    private static final byte[] A_ERROR_INVALID_LC = { // @todo fix code
            (byte)0x66,  // SW1	Status byte 1 - Command processing status
            (byte)0x01   // SW2	Status byte 2 - Command processing qualifier
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, String.format("Start flag %d, id %d", flags, startId));
        return 0;
    }


    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.i(TAG, String.format("Request: %s", Bytes.bytesToHex(commandApdu)));

        BasicCommand bc = null;

        try {
            bc = new BasicCommand(commandApdu);

        } catch (BasicCommand.ApduInvalidLengthException e) {
            return A_ERROR_INVALID_LC;
        }

        if (Bytes.isEqual(ApduSelectByteCommand(), commandApdu)) {
            Log.i(TAG, "Hello!");
            return A_OKAY;
        }

        return sign(bc);


        //return A_OKAY;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, String.format("Deactive %d", reason));
    }


    private byte[] ApduSelectByteCommand()
    {
        byte[] header = {
            (byte)0x00, // CLA	- Class - Class of instruction
            (byte)0xA4, // INS	- Instruction - Instruction code
            (byte)0x04, // P1	- Parameter 1 - Instruction parameter 1
            (byte)0x00, // P2	- Parameter 2 - Instruction parameter 2
            (byte)0x07, // Lc field	- Number of bytes present in the data field of the command
        };
        byte[] tail = {
            (byte)0x00  // Le field	- Maximum number of bytes expected in the data field of the response to the command
        };
        byte[] aid = Bytes.hexToBytes(getApplicationContext().getString(R.string.service_apdu_aid));

        return Bytes.concatenate(header, aid, tail);
    }


    private byte[] sign(BasicCommand commandApdu)
    {
        /*
        byte[] header = {
            (byte) 0x00, // CLA - http://www.cardwerk.com/smartcards/smartcard_standard_ISO7816-4_5_basic_organizations.aspx#table8
            (byte) 0x02,
            (byte) 0x00,
            (byte) 0x00,
        };

        byte[] tail = {
            (byte) 0x08
        };
        */

        try {
            AssetFileDescriptor f = getAssets().openFd("private_key.der");
            InputStream fis = f.createInputStream();
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) f.getLength()];
            dis.readFully(keyBytes);
            dis.close();

            /*
            byte[] signed = FileLoader.sign(
                    commandApdu.getData(),
                    FileLoader.getPrivateKey(keyBytes)
            );

            Toast.makeText(ApduService.this, "YEAH!", Toast.LENGTH_SHORT).show();

            return Bytes.concatenate(signed, A_OKAY);
            */
            return new byte[0]; // todo

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return A_ERROR_INVALID_AUTH;
        }
    }

}
