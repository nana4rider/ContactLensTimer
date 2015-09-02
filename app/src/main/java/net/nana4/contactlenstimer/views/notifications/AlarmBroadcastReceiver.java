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

/**
 * Created by Shunichiro AKI on 2015/08/30.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager myNotification = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = prepareNotification(context, intent);
        myNotification.notify(R.string.app_name, notification);
    }

    private Notification prepareNotification(Context context, Intent intent) {
        Intent bootIntent =
                new Intent(context, MainActivity.class);
        PendingIntent contentIntent =
                PendingIntent.getActivity(context, 0, bootIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setTicker(intent.getStringExtra("message"))
                .setContentTitle(intent.getStringExtra("message"))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent);

        return builder.build();

    }
}