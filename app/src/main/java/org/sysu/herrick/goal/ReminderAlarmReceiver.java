package org.sysu.herrick.goal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;

import java.util.UUID;

/**
 * Created by Herrick on 2017/1/8.
 */

public class ReminderAlarmReceiver extends BroadcastReceiver {
    private static String REMINDER_ACTION = "org.sysu.herrick.goal.ReminderAlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(REMINDER_ACTION)) {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_goal_icon);
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentTitle("GOAL-Reminder")
                    .setContentText("Click to check out your goal.")
                    .setTicker("GOAL-Reminder")
                    .setLargeIcon(bm)
                    .setSmallIcon(R.mipmap.ic_goal_icon)
                    .setAutoCancel(true);
            Intent in = new Intent(context, NoteActivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent result = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), in, 0);
            builder.setContentIntent(result);
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }
    }
}
