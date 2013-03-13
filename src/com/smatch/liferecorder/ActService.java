package com.smatch.liferecorder;

import static android.hardware.SensorManager.SENSOR_ACCELEROMETER;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.SENSOR_ORIENTATION;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

@SuppressLint("Override")
public class ActService extends Service implements SensorListener {
	private final static String TAG = "Smatch";
	private final int ONGOING_NOTIFICATION = 1;
	private String userid="UID01";
	private String monitorDate = "";
	private SensorManager sensormgr=null;
	private NotificationManager notificationManager = null;
	/*			電源管理		*/
	private PowerManager pm = null;
	private PowerManager.WakeLock wl;
	
	private List<Float> x_list = new ArrayList<Float>();
	private List<Float> y_list = new ArrayList<Float>();
	private List<Float> z_list = new ArrayList<Float>();
	private List<Float> v0_list = new ArrayList<Float>();
	private List<Float> v1_list = new ArrayList<Float>();
	private List<Float> v2_list = new ArrayList<Float>();
	float x,y,z,v0,v1,v2;
	Timer timer=null;
	MyHandler hm=null;
	//傳按下啟動的日期作為存檔的資料夾名稱
	private String datestring;
	private DateFormat dateFormat, DTFormat, dateFormat2;
	// 檔案路徑:/sdcard/DailyRecord/日期/
	// 所有檔案都存在此資料夾下
	private String DailyFolder= "/sdcard/DailyRecord/SensorRecord/";
	private String updateDate = "";
	private int listsize=0;

	public ActService() {
		this.dateFormat = new SimpleDateFormat("yyyyMMdd");
		this.DTFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		this.dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		Log.d(TAG,"OnCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"OnStart");
		if(hm==null){
			hm=new MyHandler(Looper.getMainLooper());
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			try{
				pm = (PowerManager) getSystemService(ActService.POWER_SERVICE);
				wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Smatch");
				wl.acquire();
				Log.d(TAG,"wl.acquire");
			}catch(Exception e){
				Log.d(TAG,e.toString());
			}
		}
		if(timer==null)
			start();
		forgroundNoti();
		return START_STICKY;
	}
	
	public void start(){
		timer = new Timer(true);
		sensormgr = (SensorManager)getSystemService(SENSOR_SERVICE);
    	sensormgr.registerListener(ActService.this,SENSOR_ACCELEROMETER|SENSOR_ORIENTATION,SENSOR_DELAY_FASTEST);
    	TimerTask GetSensorData = new TimerTask() {
			public void run() {
				Message msg =hm.obtainMessage(1);
				hm.sendMessage(msg);
			}
		};
		timer.scheduleAtFixedRate(GetSensorData, 50, 50);
	}
	
	@Override
	public void onDestroy() {
		try {
			Log.d(TAG,"Release");
			wl.release();
		}catch(Exception e){
			Log.d(TAG,e.toString());
		}
		Log.d(TAG,"OnDestroy|| "+listsize);
		timer.cancel();
		timer=null;
		sensormgr.unregisterListener(ActService.this);
		sensormgr=null;
		stopForeground(true);
		super.onDestroy();
	}
	
	@Override
	public void onLowMemory(){
		Log.d(TAG,"onLowMemory|| "+listsize);
		super.onLowMemory();
	}
	
	public void onTrimMemory (){
		Log.d(TAG,"onTrimMemory|| "+listsize);
		this.onTrimMemory();
	}
	
	public void forgroundNoti() {
		// 設定當按下這個通知之後要執行的activity
		Intent notifyIntent = new Intent(ActService.this, LifeRecorder.class);
//		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		PendingIntent appIntent = PendingIntent.getActivity(ActService.this, 0,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification();
		// 設定出現在狀態列的圖示
		notification.icon = R.drawable.iir_red;
		// 顯示在狀態列的文字
		notification.tickerText = "Daily Recording...\nYou can click here to open the Record Application.";
		// 會有通知預設的鈴聲、振動、light
		notification.defaults = Notification.DEFAULT_LIGHTS;
		// 設定通知的標題、內容
		notification.setLatestEventInfo(ActService.this, "Daily Recorder", "Daily Recording... Click Me to Stop",
				appIntent);
		// 送出Notification
//		notificationManager.notify("Daily", 0, notification);
		startForeground(ONGOING_NOTIFICATION, notification);
	}

	public void onAccuracyChanged(int sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		// TODO Auto-generated method stub
		synchronized (this)
		{
			if (sensor == SENSOR_ACCELEROMETER)
			{
				x=values[0];
				y=values[1];
				z=values[2];
			}
			else
			{
				if(sensor == SENSOR_ORIENTATION)
				{	
					v0=values[0];
					if((int)(values[1]%10)/10<5)
						v1=(int)values[1];
					else
						v1=(int)(values[1])+1;
					if((int)(values[2]%10)/10<5)
						v2=(int)values[2];
					else
						v2=(int)(values[2])+1;
				}
			}
		}
	}

	class MyHandler extends Handler
	{
		String startTime="";
		String lastTime="";
		Date date1=null;
		
		 public MyHandler(Looper looper) {
             super(looper);
         }
		 
         public void handleMessage(Message msg) {
        	 if(msg.what==1)
        	 {
        		 if(x_list.size() < 140){
        			 listsize+=1;
        			 if(x_list.size()==0){
        				 date1 = new Date();
        				 datestring = dateFormat2.format(date1);
        				 startTime = DTFormat.format(date1);
//        				 Log.d(TAG,startTime+"||Start||"+listsize);
        			 }
        			 x_list.add(x);
        			 y_list.add(y);
        			 z_list.add(z);
        			 v1_list.add(v1);
        			 v2_list.add(v2);
        			 v0_list.add(v0);
        		 }
        		 else if(x_list.size()>=140){
        			 try{
        				 if(!lastTime.equals(startTime)){
        					 lastTime=startTime;
        					 if(date1.getHours()>=6||date1.getHours()<=1){
//        						 Log.d(TAG,date1.getHours()+"||GetHours");
        						 SaveData mysaveThread = new SaveData(x_list, y_list, z_list, v0_list,v1_list,v2_list, startTime, date1);
                				 mysaveThread.setDaemon(true);
                				 mysaveThread.start();
        					 }
        				 }else{
        					 Log.d(TAG,"Repeat");
        				 }
        				 
        			 }catch(Exception e){
        				 Log.d(TAG,"???????");
        			 }finally{
        				 listsize=0;
            			 x_list.clear();
            			 y_list.clear();
            			 z_list.clear();
            			 v1_list.clear();
            			 v2_list.clear();
            			 v0_list.clear();
        			 }
        		 }
        	 }
        	 else if(msg.what==2){
        		 if (updateDate.length() != 0) {
 					File fileDir = new File("/sdcard/DailyRecord/");
 					if (!fileDir.exists()) {
 						fileDir.mkdirs();
 					}
 					File file = new File("/sdcard/DailyRecord/MonitorLog.txt");
 					try {
 						BufferedWriter bufOut = new BufferedWriter(new FileWriter(file,	true));
 						bufOut.append(updateDate + "\n");
 						bufOut.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
        	 }
         }
	}
	
	public class SaveData extends Thread{
		private List<Float> x_list = null;
		private List<Float> y_list = null;
		private List<Float> z_list = null;
		private List<Float> v0_list = null;
		private List<Float> v1_list = null;
		private List<Float> v2_list = null;
		private String startTime = "";
		private Date date1 = null;
		public SaveData(List<Float> x,List<Float> y,List<Float> z,List<Float> v0,List<Float> v1,List<Float> v2,String Time, Date date){
//			Log.d(TAG,x.size()+"||Size");
			x_list = new ArrayList<Float>(x);
			y_list = new ArrayList<Float>(y);
			z_list = new ArrayList<Float>(z);
			v0_list = new ArrayList<Float>(v0);
			v1_list = new ArrayList<Float>(v1);
			v2_list = new ArrayList<Float>(v2);
			startTime = Time;
			date1 = date;
		}
		
		@Override
		public void run(){
//			Log.d(TAG,"SaveThread");
//			Log.d(TAG,startTime+"||RawSave");
			WriteRawDataFile(x_list, y_list, z_list,0,startTime, DailyFolder+datestring);
			WriteRawDataFile(v0_list,v1_list,v2_list,1,startTime, DailyFolder+datestring);
//			Log.d(TAG,"Before feature");
			WriteFeatureFile( x_list, y_list, z_list, v0_list,v1_list,v2_list, date1);
		}
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.d(TAG, "msg: " + "Provider Disabled");
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.d(TAG, "msg: " + "Provider enabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.d(TAG, "msg: " + "Status");
	}


	public void WriteRawDataFile(List<Float> x, List<Float> y, List<Float> z, int id, String startTime, String DailyFolder)
    {
		try
		{
			// 當id為0時，寫入加速度資料;當id為1時，寫入方位角資料
			if(id == 0)
			{
				File fileDir = new File(DailyFolder + "/" + userid + "-Acc/");
				if (!fileDir.exists()) {
					fileDir.mkdirs();
				}
				String file_path = fileDir.getPath()+"/"+startTime+"_acc.txt";
				File file = new File(file_path);
				BufferedWriter bufOut = new BufferedWriter(new FileWriter(file));
				for (int i = 0; i < 140; i++) {
					bufOut.append(x.get(i) + "\t" + y.get(i) + "\t" + z.get(i)
							+ "\n");
				}
				bufOut.close();
			}
			else if(id == 1)
			{
				File fileDir = new File(DailyFolder + "/" + userid + "-Ori/");
				if (!fileDir.exists()) {
					fileDir.mkdirs();
				}
				String file_path = fileDir.getPath()+"/"+startTime+"_ori.txt";
				File file = new File(file_path);
				BufferedWriter bufOut = new BufferedWriter(new FileWriter(file));
				for (int i = 0; i < 140; i++) {
					if(!x.get(i).isNaN())
						bufOut.append(x.get(i) + "\t" + y.get(i) + "\t" + z.get(i)
							+ "\n");
					else{
						for(int j=i;j<x.size();j++){
							if(!x.get(j).isNaN()){
								x.set(i, x.get(j));
								break;
							}
						}
						if(x.get(i).isNaN())
							x.set(i, (float) 0);
//						Log.d(TAG, "Nan: " + "NaN?????");
						bufOut.append(x.get(i) + "\t" + y.get(i) + "\t" + z.get(i)
								+ "\n");
					}
				}
				bufOut.close();
			}
		}
		catch(Exception e)
		{
			Log.d(TAG, "msg: " + "raw error...");
			e.printStackTrace();
		}
	}

	/*
	 * 寫入特徵txt檔 檔案路徑:/sdcard/DailyRecord/SensorRecord/日期/Features
	 * 檔名:yyyy-MM-dd
	 */	
	public void WriteFeatureFile(List<Float> x_list, List<Float> y_list, List<Float> z_list,List<Float> v0_list,
			List<Float> v1_list, List<Float> v2_list,Date date) {
		
		Features features = new Features(x_list, y_list, z_list);
		double[] featureSet = features.getFeatureSet();
		double[] OrifeatureSet = GetOriFeatures(v0_list,v1_list,v2_list);
		
		try {
			File fileDir2 = new File(DailyFolder+dateFormat2.format(date));
			if (!fileDir2.exists()) {
				fileDir2.mkdirs();
			}
			String file_path = fileDir2.getPath() + "/" + userid + "-"
					+ dateFormat2.format(date) + ".txt";
			File file = new File(file_path);
			BufferedWriter bufOut = new BufferedWriter(new FileWriter(file,
					true));
//			Log.d(TAG,DTFormat.format(date)+"||FeatureSave");
			bufOut.append(DTFormat.format(date) + "\t" + featureSet[0] + "\t" + featureSet[1]
					+ "\t" + featureSet[2] + "\t" + featureSet[3] + "\t"
					+ featureSet[4] + "\t" + featureSet[5] + "\t"
					+ featureSet[6] + "\t" + featureSet[7] + "\t"
					+ featureSet[8] + "\t" + featureSet[9] + "\t"
					+ featureSet[10] + "\t" + featureSet[11] + "\t"
					+ featureSet[12] + "\t" + featureSet[13] + "\t"
					+ featureSet[14] + "\t" + featureSet[15] + "\t"
					+ featureSet[16] + "\t" + featureSet[17] + "\t"
					+ featureSet[18] + "\t" + featureSet[19] + "\t"
					+ featureSet[20] + "\t" + featureSet[21] + "\t"
					+ featureSet[22] + "\t" + featureSet[23] + "\t"
					+ featureSet[24] + "\t" + featureSet[25] + "\t" 
					+ OrifeatureSet[0] + "\t" + OrifeatureSet[1] + "\t" + OrifeatureSet[2]
					+ "\n");
			// }
			bufOut.close();	
			updateDate = userid + "-" + DTFormat.format(date);
			if(!monitorDate.equals(dateFormat2.format(date))){
				monitorDate = dateFormat2.format(date);
				Message msg = hm.obtainMessage(2);
				hm.sendMessage(msg);
			}
			
		} catch (Exception e) {
			Log.d(TAG, "msg: " + "feature save error...");
		}
//		Log.d(TAG, "msg: " + "feature saved");
	}
	/*				Construct Ori Features			*/
	public List<Float> middleList(List<Float> list) {
  		List<Float> newlist = new ArrayList<Float>();
  		if(list.size()<=128){
  			return list;
  		}else{
  			for(int i=6;i<134;i++){
  	  			newlist.add(list.get(i));
  	  		}
  	  		return newlist;
  		}
  	}
	public double[] GetOriFeatures(List<Float> x_list,
			List<Float> y_list, List<Float> z_list){
		double[] var = new double[3];
		List<Float> dataX = middleList(x_list);
		List<Float> dataY = middleList(y_list);
		List<Float> dataZ = middleList(z_list);
		
		double vx = 0.0,vy = 0.0,vz = 0.0;
		for (int i = 1; i < dataX.size(); i++ )
		{
			vx += (Math.abs(dataX.get(i)-dataX.get(i-1)) < 180) ? Math.abs(dataX.get(i)-dataX.get(i-1)) : (360-Math.abs(dataX.get(i)-dataX.get(i-1)));
			vy += (Math.abs(dataY.get(i)-dataY.get(i-1)) < 180) ? Math.abs(dataY.get(i)-dataY.get(i-1)) : (360-Math.abs(dataY.get(i)-dataY.get(i-1)));
			vz += (Math.abs(dataZ.get(i)-dataZ.get(i-1)) < 90) ? Math.abs(dataZ.get(i)-dataZ.get(i-1)) : (180-Math.abs(dataZ.get(i)-dataZ.get(i-1)));
		}	
		var[0]=vx;
		var[1]=vy;
		var[2]=vz;
		return var;
		
	}

}
