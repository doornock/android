package cz.sodae.doornock.services;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import cz.sodae.doornock.R;
import cz.sodae.doornock.model.site.Site;
import cz.sodae.doornock.model.site.SiteManager;
import cz.sodae.doornock.services.commands.Command;
import cz.sodae.doornock.services.commands.HelloCommand;
import cz.sodae.doornock.services.commands.SelectFileCommand;
import cz.sodae.doornock.services.commands.SignCommand;
import cz.sodae.doornock.utils.Bytes;
import cz.sodae.doornock.utils.InvalidGUIDException;

public class ApduService extends HostApduService {

    private static final String TAG = "Doornock/APDU";
    private static final int NOTIFICATION_ID = 0;

    /** After {@link #onDeactivated(int)} notification will be hidden */
    private boolean hideNotification = true;

    /** Common commands variable - have to be released on {@link #onDeactivated(int)} */
    Site site;

    // https://www.eftlab.com.au/index.php/site-map/knowledge-base/118-apdu-response-list

    /**
     * Successful status code
     */
    private static final byte[] A_OKAY = {
            (byte) 0x90,  // SW1	Status byte 1 - Command processing status
            (byte) 0x00   // SW2	Status byte 2 - Command processing qualifier
    };

    /**
     * Error in process status code
     */
    private static final byte[] A_ERROR_INVALID_AUTH = {
            (byte) 0x98,  // SW1	Status byte 1 - Command processing status
            (byte) 0x04   // SW2	Status byte 2 - Command processing qualifier
    };


    /**
     * Status code when APDU is invalid
     */
    private static final byte[] A_ERROR_INVALID_LC = {
            (byte) 0x67,  // SW1	Status byte 1 - Command processing status
            (byte) 0x00   // SW2	Status byte 2 - Command processing qualifier
    };


    /**
     * Status code when APDU command is unknown
     */
    private static final byte[] A_ERROR_UNKNOWN_COMMAND = {
            (byte) 0x69,  // SW1	Status byte 1 - Command processing status
            (byte) 0x00   // SW2	Status byte 2 - Command processing qualifier
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, String.format("Start flag %d, id %d", flags, startId));
        return 0;
    }

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

        if (Bytes.isEqual(
                SelectFileCommand.getAPDU(getApplicationContext().getString(R.string.service_apdu_aid)),
                commandApdu
        )) {
            Log.i(TAG, "Reader's asking select app");
            quickNotification(getString(R.string.service_apdu_notification_node_detected));
            return A_OKAY;
        }

        if (HelloCommand.isThisCommand(commandApdu)) {
            Log.i(TAG, "Terminal sends GUID");

            try {
                String guid = new String(bc.getData());

                Log.i(TAG, "Received guid is " + guid);

                site = s.getByGuid(guid);

                if (site == null) {
                    quickNotification(getString(R.string.service_apdu_notification_site_not_found));
                    return A_ERROR_INVALID_AUTH;
                }

                Log.i(TAG, "Site found, device_id=" + site.getDeviceId());
                quickNotification(String.format(
                        getString(R.string.service_apdu_notification_site_found),
                        site.getTitle()
                ));

                return Bytes.concatenate(site.getDeviceId().getBytes(), A_OKAY);
            } catch (InvalidGUIDException e) {
                return A_ERROR_INVALID_AUTH;
            }
        }


        if (SignCommand.isThisCommand(commandApdu)) {
            Log.i(TAG, "Received sign command");

            try {
                byte[] data = bc.getData();

                if (site != null && site.getKey() != null) {

                    if (!checkUnlockRequirement(site))
                        return A_ERROR_INVALID_AUTH;

                    byte[] signed = site.getKey().sign(data);

                    quickNotification(String.format(
                            getString(R.string.service_apdu_notification_site_authentication),
                            site.getTitle()
                    ));
                    Log.i(TAG, "Signing site " + site.getTitle());

                    return Bytes.concatenate(signed, A_OKAY);

                } else {
                    Log.i(TAG, "Site not found");
                    quickNotification(getString(R.string.service_apdu_notification_site_not_found));

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
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (hideNotification) {
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.cancel(NOTIFICATION_ID);
                }
            }
        }.start();

        site = null;
    }


    /**
     * Checks if device is unlocked by site if it required
     *
     * @param site
     * @return if site does not require unlocked device, or device is already unlocked, return true
     */
    private boolean checkUnlockRequirement(Site site) {
        if (site.isRequiredUnlock()) {
            KeyguardManager km = ((KeyguardManager) getSystemService(KEYGUARD_SERVICE));
            if (km.isKeyguardLocked()) {
                interruptUserNotification(getString(R.string.service_apdu_notification_unlock_required));
                return false;
            }
        }
        return true;
    }


    /**
     * When app need attention from user
     *
     * @param message message to show
     */
    private void interruptUserNotification(String message) {
        Vibrator vb = ((Vibrator) getSystemService(VIBRATOR_SERVICE));
        if (vb.hasVibrator()) {
            vb.vibrate(new long[]{0, 500, 200}, -1);
        }
        showNotification(NOTIFICATION_ID, message);
        hideNotification = false;
    }

    /**
     * Notification which will be removed
     *
     * @param message message to show
     */
    private void quickNotification(String message) {
        showNotification(NOTIFICATION_ID, message);
    }


    /**
     * Show notification
     *
     * @param id id in NotificationManager
     * @param message message to show
     */
    private void showNotification(int id, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(message);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(id, mBuilder.build());
    }

}
