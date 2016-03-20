package cz.sodae.doornock.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.InputStream;

import cz.sodae.doornock.R;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteManager;
import cz.sodae.doornock.utils.Bytes;
import cz.sodae.doornock.utils.InvalidGUIDException;
import cz.sodae.doornock.utils.commands.BasicCommand;
import cz.sodae.doornock.utils.FileLoader;
import cz.sodae.doornock.utils.commands.HelloCommand;
import cz.sodae.doornock.utils.commands.SignCommand;
import cz.sodae.doornock.utils.security.keys.SignerAndVerifier;

public class ApduService extends HostApduService
{

    private static final String TAG = "Doornock/APDU";
    private static final int NOTIFICATION_ID = 0;
    private NotificationManager nm;

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

    Site site;

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.i(TAG, String.format("Request: %s", Bytes.bytesToHex(commandApdu)));

        BasicCommand bc = null;

        SiteManager s = new SiteManager(this);

        try {
            bc = new BasicCommand(commandApdu);

        } catch (BasicCommand.ApduInvalidLengthException e) {
            return A_ERROR_INVALID_LC;
        }

        if (Bytes.isEqual(ApduSelectByteCommand(), commandApdu)) {
            Log.i(TAG, "Hello!");

            return A_OKAY;
        }

        if (HelloCommand.isThisCommnad(commandApdu)) {
            Log.i(TAG, "HELLO!");

            nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Doorlock")
                    .setContentText("Trying unlocking...");

            nm.notify(NOTIFICATION_ID, mBuilder.build());

            try {
                String guid = new String(bc.getData());
                Log.i(TAG, "GUID!" + guid);
                site = s.getByGuid(guid);
                if (site == null) return A_ERROR_INVALID_AUTH;
                Log.i(TAG, "FOUND!" + site.getDeviceId());
                return Bytes.concatenate(site.getDeviceId().getBytes(), A_OKAY);
            } catch (InvalidGUIDException e) {
                return A_ERROR_INVALID_AUTH;
            }
        }


        if (SignCommand.isThisCommnad(commandApdu)) {
            Log.i(TAG, "YOU WANT SIGN?");
            try {
                byte[] data = bc.getData();

                if (site != null && site.getKey() != null) {
                    byte[] signed = SignerAndVerifier.sign(data, site.getKey().getPrivateKey());
                    Log.i(TAG, "SIGNING!" + site.getDeviceId());


                    nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Doorlock")
                            .setContentText("Unlocking in site " + site.getTitle());

                    nm.notify(NOTIFICATION_ID, mBuilder.build());


                    return Bytes.concatenate(signed, A_OKAY);

                } else {

                    nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Doorlock")
                            .setContentText("Site not found");

                    nm.notify(NOTIFICATION_ID, mBuilder.build());


                    throw new Exception("SITE IS NOT WELL");
                }

            } catch (InvalidGUIDException e) {
                e.printStackTrace();
                return A_ERROR_INVALID_AUTH;
            } catch (Exception e) {
                e.printStackTrace();
                return A_ERROR_INVALID_AUTH;
            }
        }


        return A_ERROR_UNKNOWN_COMMAND;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, String.format("Deactive %d", reason));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
        site = null;
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


}
