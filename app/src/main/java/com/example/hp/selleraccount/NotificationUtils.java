package com.example.hp.selleraccount;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.NotificationCompat;


/**
 * Created by hp on 17-03-2018.
 */

public class NotificationUtils extends Activity {

    private static final int LIST_PENDING_ID = 100;
    private static final String LIST_NOTIFICATION_ID = "reminder_notification_channel";
    private static final int LIST_NOTIFICATION_CHANNEL_ID = 101;
    private static final int LIST_REMINDER_NOTIFICATION_ID = 102;

    private static PendingIntent contentIntent(Context context) {

        Intent startActivityIntent = new Intent(context, MainActivity.class);

        return PendingIntent.getActivity(context, LIST_PENDING_ID, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_bell);
        return largeIcon;
    }

    @TargetApi(26)

    public static void remindUser(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        {

            NotificationChannel mChannel = new NotificationChannel(LIST_NOTIFICATION_ID, context
                    .getString(R.string.main_notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, LIST_NOTIFICATION_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_bell)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.list_notification))
                .setContentText(context.getString(R.string.please_check_list))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.waiting_for_your_response)))
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        }

        notificationManager.notify(LIST_REMINDER_NOTIFICATION_ID, notificationBuilder.build());

    }

}
