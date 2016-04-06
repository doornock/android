package cz.sodae.doornock.services;

import android.app.NotificationManager;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import cz.sodae.doornock.R;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteManager;
import cz.sodae.doornock.utils.Bytes;
import cz.sodae.doornock.utils.InvalidGUIDException;
import cz.sodae.doornock.services.commands.Command;
import cz.sodae.doornock.services.commands.HelloCommand;
import cz.sodae.doornock.services.commands.SignCommand;

public class ApduService extends HostApduService
{

    private static final String TAG = "Doornock/APDU";
    private static final int NOTIFICATION_ID = 0;
    private NotificationManager nm;


    // https://www.eftlab.com.au/index.php/site-map/knowledge-base/118-apdu-response-list

    private static final byte[] A_OKAY = {
            (byte)0x90,  // SW1	Status byte 1 - Command processing status
            (byte)0x00   // SW2	Status byte 2 - Command processing qualifier
    };


    private static final byte[] A_ERROR_INVALID_AUTH = {
            (byte)0x98,  // SW1	Status byte 1 - Command processing status
            (byte)0x04   // SW2	Status byte 2 - Command processing qualifier
    };

    private static final byte[] A_ERROR_INVALID_LC = {
            (byte)0x67,  // SW1	Status byte 1 - Command processing status
            (byte)0x00   // SW2	Status byte 2 - Command processing qualifier
    };


    private static final byte[] A_ERROR_UNKNOWN_COMMAND = {
            (byte)0x69,  // SW1	Status byte 1 - Command processing status
            (byte)0x00   // SW2	Status byte 2 - Command processing qualifier
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

        Command bc;
        SiteManager s = new SiteManager(this);

        try {
            bc = new Command(commandApdu);
        } catch (Command.ApduInvalidLengthException e) {
            return A_ERROR_INVALID_LC;
        }

        if (Bytes.isEqual(ApduSelectByteCommand(), commandApdu)) {
            Log.i(TAG, "Reader's asking select app");
            showNotification("Terminal detected");
            return A_OKAY;
        }

        if (HelloCommand.isThisCommand(commandApdu)) {
            Log.i(TAG, "Terminal sends GUID");
            showNotification("Terminal detected");

            try {
                String guid = new String(bc.getData());

                Log.i(TAG, "Received guid is " + guid);

                site = s.getByGuid(guid);

                if (site == null) {
                    return A_ERROR_INVALID_AUTH;
                }

                Log.i(TAG, "Site found, device_id=" + site.getDeviceId());
                showNotification("Terminal found: " + site.getTitle());

                return Bytes.concatenate(site.getDeviceId().getBytes(), A_OKAY);
            } catch (InvalidGUIDException e) {
                return A_ERROR_INVALID_AUTH;
            }
        }


        if (SignCommand.isThisCommand(commandApdu)) {
            Log.i(TAG, "Received sign command");
            showNotification("Terminal detected");

            try {
                byte[] data = bc.getData();

                if (site != null && site.getKey() != null) {

                    long startTime = System.currentTimeMillis();
                    byte[] signed = site.getKey().sign(data);
                    long estimatedTime = System.currentTimeMillis() - startTime;

                    Log.i(TAG, "Singing in " + estimatedTime);


                    Log.i(TAG, "Signing");
                    showNotification("Unlocking in site " + site.getTitle());

                    return Bytes.concatenate(signed, A_OKAY);

                } else {
                    Log.i(TAG, "Site not found");
                    showNotification("Site not found");

                    return A_ERROR_INVALID_AUTH;
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


    private void showNotification(String message)
    {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(message);

        nm.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
