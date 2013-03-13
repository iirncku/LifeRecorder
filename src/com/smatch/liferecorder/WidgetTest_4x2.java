package com.smatch.liferecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetTest_4x2 extends AppWidgetProvider {
	private String TAG = "Smatch Widget";
	private String userid="UID01";
	private DateFormat DTFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
	private String DailyFolder= "/sdcard/DailyRecord/";
	private String state = "unknown";
	private boolean isServiceStarted = false;
	
    @Override
    public void onUpdate(Context context,AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d(TAG, "onUpdate");
    	
        RemoteViews updateViews = new RemoteViews( context.getPackageName(), R.layout.widgetlayout_4x2);
//        updateViews.setTextViewText(R.id.state,  state );
        recordState("update", new Date());
        isServiceStarted = isMyServiceRunning(context);
        if(isServiceStarted){
        	updateViews.setTextViewText(R.id.service_state,  "Recording..." );
        }else{
        	updateViews.setTextViewText(R.id.service_state,  "unRecord" );
        }
        
        Intent updateState_rest=new Intent("updateState_rest");
        PendingIntent Pprevintent_rest= PendingIntent.getBroadcast(context, 0, updateState_rest, 0);
        updateViews.setOnClickPendingIntent(R.id.rest_button, Pprevintent_rest); 
        
        Intent updateState_walk=new Intent("updateState_walk");
        PendingIntent Pprevintent_walk= PendingIntent.getBroadcast(context, 0, updateState_walk, 0);
        updateViews.setOnClickPendingIntent(R.id.walk_button, Pprevintent_walk); 
        
        Intent updateState_move=new Intent("updateState_move");
        PendingIntent Pprevintent_move= PendingIntent.getBroadcast(context, 0, updateState_move, 0);
        updateViews.setOnClickPendingIntent(R.id.move_button, Pprevintent_move); 
        
        Intent updateState_run=new Intent("updateState_run");
        PendingIntent Pprevintent_run= PendingIntent.getBroadcast(context, 0, updateState_run, 0);
        updateViews.setOnClickPendingIntent(R.id.run_button, Pprevintent_run); 
        
        Intent updateState_other=new Intent("updateState_other");
        PendingIntent Pprevintent_other= PendingIntent.getBroadcast(context, 0, updateState_other, 0);
        updateViews.setOnClickPendingIntent(R.id.other_button, Pprevintent_other); 
        
        Intent start=new Intent("StartRecord");
        PendingIntent Pprevintent_start= PendingIntent.getBroadcast(context, 0, start, 0);
        updateViews.setOnClickPendingIntent(R.id.start_button, Pprevintent_start); 
        
        Intent stop=new Intent("StopRecord");
        PendingIntent Pprevintent_stop= PendingIntent.getBroadcast(context, 0, stop, 0);
        updateViews.setOnClickPendingIntent(R.id.stop_button, Pprevintent_stop); 
        
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds){
    	Log.d(TAG, "onDeleted");
    	recordState(state, new Date());
    	super.onDeleted(context, appWidgetIds);
    }
    
    @Override
    public void onDisabled(Context context){
    	Log.d(TAG, "onDisabled");
    	super.onDisabled(context);
    }
    
    @Override
    public void onEnabled(Context context){
    	Log.d(TAG, "onEnabled");
    	super.onEnabled(context);
    }
    
    @Override
    public void onReceive(Context context, Intent intent){
    	Log.d(TAG, "onReceive  "+intent.getAction());
    	RemoteViews updateViews = new RemoteViews( context.getPackageName(), R.layout.widgetlayout_4x2);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName appWidgetId = new ComponentName(context, WidgetTest_4x2.class);
        
        if(intent.getAction().startsWith("updateState")){
        	if(intent.getAction().equals("updateState_rest")){
        		Log.d(TAG, "updateState_rest");
        		state = "Rest";
        	}else if(intent.getAction().equals("updateState_walk")){
        		state = "Walk";
        		Log.d(TAG, "updateState_walk");
        	}else if(intent.getAction().equals("updateState_move")){
        		state = "Move";
        		Log.d(TAG, "updateState_move");
        	}else if(intent.getAction().equals("updateState_run")){
        		state = "Run";
        		Log.d(TAG, "updateState_run");
        	}else if(intent.getAction().equals("updateState_other")){
        		state = "Other";
        		Log.d(TAG, "updateState_other");
        	}
        	updateViews.setTextViewText(R.id.state, state );
        	appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        	recordState(state, new Date());
        }else if(intent.getAction().startsWith("StartRecord")){
        	Log.d(TAG, "StartRecord");
        	Intent myServIntent = new Intent(context, ActService.class);
        	context.startService(myServIntent);
        	updateViews.setTextViewText(R.id.service_state,  "Recording..." );
        	appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        	
        }else if(intent.getAction().startsWith("StopRecord")){
        	Log.d(TAG, "StopRecord");
        	Intent myServIntent = new Intent(context, ActService.class);
        	context.stopService(myServIntent);
        	updateViews.setTextViewText(R.id.service_state,  "unRecord" );
        	appWidgetManager.updateAppWidget(appWidgetId, updateViews);
        }
    	
    	super.onReceive(context, intent);
    }
    
    public void recordState(String state, Date date){		
		try {
			File fileDir2 = new File(DailyFolder);
			if (!fileDir2.exists()) {
				fileDir2.mkdirs();
			}
			String file_path = fileDir2.getPath() + "/" + userid + "_label.csv";
			File file = new File(file_path);
			BufferedWriter bufOut = new BufferedWriter(new FileWriter(file,
					true));
//			Log.d(TAG,DTFormat.format(date)+"||FeatureSave");
			bufOut.append(DTFormat.format(date) + "," + state + "\n");
			// }
			bufOut.close();	
			
		} catch (Exception e) {
			Log.d(TAG, "msg: " + e.toString());
		}
    }
    
    private boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        	//Log.d("Service",service.service.getClassName());
            if ("com.smatch.liferecorder.ActService".equals(service.service.getClassName())) {
            	//Toast.makeText(this, "ABC", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }
}