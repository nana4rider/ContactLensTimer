package net.nana4.contactlenstimer.views.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import net.nana4.contactlenstimer.MainActivity;
import net.nana4.contactlenstimer.R;
import net.nana4.contactlenstimer.utils.ContactLendsTimerUtils;

/**
 * Created by Shunichiro AKI on 2015/08/30.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager myNotification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = prepareNotification(context, intent);

        // 通知
        myNotification.notify(intent.getIntExtra("requestCode", -1), notification);

        if (intent.getBooleanExtra("repeatTimer", false)) {
            ContactLendsTimerUtils.resetTimer(context, intent.getStringExtra("prefKey"));
        }
    }

    private Notification prepareNotification(Context context, Intent intent) {
        Intent bootIntent =
                new Intent(context, MainActivity.class);
        PendingIntent contentIntent =
                PendingIntent.getActivity(context, 0, bootIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setTicker(context.getString(R.string.app_name))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(intent.getStringExtra("message"))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent);

        return builder.build();

    }
}