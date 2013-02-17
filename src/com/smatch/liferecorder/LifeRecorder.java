package com.smatch.liferecorder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LifeRecorder extends Activity{
	/** Called when the activity is first created. */
	//private NotificationManager myNotiManager;
	private TextView text;                                         //顯示狀態的TextView
	private Button Monitor, BackBT, Stop;  //login button
	private SharedPreferences save;
	private boolean IsMonitoring=false;
//	private NotificationManager notificationManager=null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        BackBT=(Button)findViewById(R.id.Leave);
        Monitor=(Button)findViewById(R.id.Monitor);
        Stop=(Button)findViewById(R.id.Stop);
        text=(TextView)findViewById(R.id.Text);
        
        save = getSharedPreferences("save", MODE_PRIVATE);
        if(save.getString("UpdateDate", "").length()==0){
        	Editor editor = save.edit();
			editor.putString("UpdateDate", "2011-10-10-00-00-00");
			editor.commit();
        }
        
        if(isMyServiceRunning()){
        	IsMonitoring=true;
        	text.setText(R.string.Recording);
        }
        BackBT.setOnClickListener(new Button.OnClickListener(){
    		//按下按鈕後離開這一頁
    		public void onClick(View arg0){
    			new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    			.setMessage("確定離開生活模式紀錄系統？").setPositiveButton("是",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								LifeRecorder.this.finish();
    							}
    						}).setNegativeButton("否",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								// Do nothing, Back to system
    							}
    						}).show();
    		}
    	});
        
        Monitor.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
    			//新增一個Intent物件
				if(!IsMonitoring){
					Intent myServIntent = new Intent(LifeRecorder.this, SensorService.class);
	    			startService(myServIntent);
	    			IsMonitoring=true;
	    			text.setText(R.string.Recording);
				}
			}});
        Stop.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
    			//新增一個Intent物件
				if(!IsMonitoring)
            		return;
    			new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    				.setMessage("是否暫停生活模式紀錄？").setPositiveButton("是",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int which) {
    								Intent i2 = new Intent("ACTSTOP");
    							    sendBroadcast(i2);
    								IsMonitoring=false;
    								text.setText(R.string.nonRecording);
    							}
    						}).setNegativeButton("否",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								// Do nothing, Back to system
    							}
    						}).show();
			}});
    }

    @Override
    public void onDestroy()
	{
//    	Intent myServIntent = new Intent(LifeRecorder.this, ActService.class);
//		stopService(myServIntent);
    	
		super.onDestroy();
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
			.setMessage("確定離開生活模式紀錄系統？").setPositiveButton("是",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int which) {
								LifeRecorder.this.finish();
							}
						}).setNegativeButton("否",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int which) {
								// Do nothing, Back to system
							}
						}).show();
           return true;
        }else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
           return super.onKeyDown(KeyEvent.KEYCODE_MENU, event);
        }
        return super.onKeyDown(keyCode, event);
    }
    
    //MENU
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        menu.add(0, 0, 0, "開始紀錄");
        menu.add(0, 1, 0, "暫停紀錄");
        menu.add(0, 2, 0, "離開系統");
        menu.add(0, 3, 0, "上傳檔案");
        return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch(item.getItemId()) {
            case 0:
            	if(IsMonitoring)
            		break;
    			//新增一個Intent物件
    			Intent myServIntent = new Intent(LifeRecorder.this, SensorService.class);
    			startService(myServIntent);
    			IsMonitoring=true;
    			text.setText(R.string.Recording);
                break;
            case 1:
            	if(!IsMonitoring)
            		break;
    			new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    				.setMessage("是否暫停生活模式紀錄？").setPositiveButton("是",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int which) {
    								Intent i2 = new Intent("ACTSTOP");
    							    sendBroadcast(i2);
    								IsMonitoring=false;
    								text.setText(R.string.nonRecording);
    							}
    						}).setNegativeButton("否",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								// Do nothing, Back to system
    							}
    						}).show();
    			break;
            case 2:
            	new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    			.setMessage("確定離開生活模式紀錄系統？").setPositiveButton("是",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								LifeRecorder.this.finish();
    							}
    						}).setNegativeButton("否",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								// Do nothing, Back to system
    							}
    						}).show();
    			break;
            case 3:
            	if(IsMonitoring)
            		break;
				Intent intent = new Intent();
				intent.setClass(LifeRecorder.this, Login.class);
				startActivity(intent);
    			break;
            default:
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        	//Log.d("Service",service.service.getClassName());
            if ("com.smatch.liferecorder.SensorService".equals(service.service.getClassName())) {
            	//Toast.makeText(this, "ABC", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }
}
