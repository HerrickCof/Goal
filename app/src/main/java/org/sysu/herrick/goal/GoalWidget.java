package org.sysu.herrick.goal;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.util.UUID;

/**
 * Implementation of App Widget functionality.
 */
public class GoalWidget extends AppWidgetProvider {

    private static String UPDATE_FROM_SERVICE = "org.sysu.herrick.goal.WidgetRefreshService";
    private Bundle bundle;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.goal_widget);
        if (bundle != null) {
            if (bundle.getInt("achieved_0") == 1)
                rv.setTextViewText(R.id.widget_goal_1_achieved, "[ ACHIEVED ]");
            else rv.setTextViewText(R.id.widget_goal_1_achieved, " ");
            if (bundle.getInt("achieved_1") == 1)
                rv.setTextViewText(R.id.widget_goal_2_achieved, "[ ACHIEVED ]");
            else rv.setTextViewText(R.id.widget_goal_2_achieved, " ");
            if (bundle.getInt("achieved_2") == 1)
                rv.setTextViewText(R.id.widget_goal_3_achieved, "[ ACHIEVED ]");
            else rv.setTextViewText(R.id.widget_goal_3_achieved, " ");
            rv.setTextViewText(R.id.widget_goal_1_des, bundle.getString("des_0"));
            rv.setTextViewText(R.id.widget_goal_2_des, bundle.getString("des_1"));
            rv.setTextViewText(R.id.widget_goal_3_des, bundle.getString("des_2"));
            if (bundle.getLong("days_0") == -1)
                rv.setTextViewText(R.id.widget_goal_1_days, "");
            else
                rv.setTextViewText(R.id.widget_goal_1_days, bundle.getLong("days_0") + "");
            if (bundle.getLong("days_1") == -1)
                rv.setTextViewText(R.id.widget_goal_2_days, "");
            else
                rv.setTextViewText(R.id.widget_goal_2_days, bundle.getLong("days_1") + "");
            if (bundle.getLong("days_2") == -1)
                rv.setTextViewText(R.id.widget_goal_3_days, "");
            else
                rv.setTextViewText(R.id.widget_goal_3_days, bundle.getLong("days_2") + "");
            rv.setTextViewText(R.id.widget_next_ddl, "NEXT DEADLINE: " + bundle.getString("next_ddl"));
        }




        Intent refresh = new Intent(context, WidgetRefreshService.class);
        Bundle b = new Bundle();
        b.putString("widget_request", "require_update");
        refresh.putExtras(b);
        refresh.setData(Uri.parse(refresh.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pi = PendingIntent.getService(context, UUID.randomUUID().hashCode(), refresh, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widget_refresh, pi);

       /* Intent goal_1 = new Intent(context, WidgetRefreshService.class);
        Bundle b_1 = new Bundle();
        b_1.putString("widget_request", "open_detail");
        b_1.putInt("which_goal", 1);
        goal_1.putExtras(b_1);
        goal_1.setData(Uri.parse(goal_1.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent p_1 = PendingIntent.getService(context, UUID.randomUUID().hashCode(), goal_1, PendingIntent.FLAG_ONE_SHOT);
        rv.setOnClickPendingIntent(R.id.widget_goal_1, p_1);

        Intent goal_2 = new Intent(context, WidgetRefreshService.class);
        Bundle b_2 = new Bundle();
        b_2.putString("widget_request", "open_detail");
        b_2.putInt("which_goal", 2);
        goal_1.putExtras(b_2);
        goal_2.setData(Uri.parse(goal_2.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent p_2 = PendingIntent.getService(context, UUID.randomUUID().hashCode(), goal_2, PendingIntent.FLAG_ONE_SHOT);
        rv.setOnClickPendingIntent(R.id.widget_goal_2, p_2);

        Intent goal_3 = new Intent(context, WidgetRefreshService.class);
        Bundle b_3 = new Bundle();
        b_3.putString("widget_request", "open_detail");
        b_3.putInt("which_goal", 3);
        goal_3.putExtras(b_3);
        goal_3.setData(Uri.parse(goal_3.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent p_3 = PendingIntent.getService(context, UUID.randomUUID().hashCode(), goal_3, PendingIntent.FLAG_ONE_SHOT);
        rv.setOnClickPendingIntent(R.id.widget_goal_3, p_3);*/

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, GoalWidget.class));
        if (intent.getAction().equals(UPDATE_FROM_SERVICE)) {
            bundle = intent.getExtras();
            for (int appWidgetId : appIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

