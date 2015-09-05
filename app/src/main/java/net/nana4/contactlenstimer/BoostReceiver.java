package net.nana4.contactlenstimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.nana4.contactlenstimer.utils.ContactLendsTimerUtils;

/**
 * Created by Shunichiro AKI on 2015/09/05.
 */
public class BoostReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) && !intent.getDataString().equals(
                "package:" + context.getPackageName())) {
            return;
        }

        ContactLendsTimerUtils.updateTimer(context);
    }
}
