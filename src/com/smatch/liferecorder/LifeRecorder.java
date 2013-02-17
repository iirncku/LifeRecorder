/*=====================================================================================*/
/*Project : 		LifeRecorder App
/*執行功能：	程式主畫面，控制記錄、暫停、上傳檔案到server
/*關聯檔案：	Main.xml, SensorService.java, Login.java
/*=====================================================================================*/

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
	private TextView text;                                         //顯示狀態的TextView
	private Button Monitor, BackBT, Stop;
	private SharedPreferences save;
	private boolean IsMonitoring=false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        BackBT=(Button)findViewById(R.id.Leave);
        Monitor=(Button)findViewById(R.id.Monitor);
        Stop=(Button)findViewById(R.id.Stop);
        text=(TextView)findViewById(R.id.Text);
		/*		查詢本機最近上傳時間		*/
        save = getSharedPreferences("save", MODE_PRIVATE);
        if(save.getString("UpdateDate", "").length()==0){
        	Editor editor = save.edit();
			editor.putString("UpdateDate", "2011-10-10-00-00-00");
			editor.commit();
        }
        /*		檢查記錄的Service是否正在執行		*/
        if(isMyServiceRunning()){
        	IsMonitoring=true;
        	text.setText(R.string.Recording);
        }
		/*		設置離開按鈕的事件		*/
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
        /*		設置開始記錄按鈕的事件		*/
        Monitor.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
    			//新增一個Intent物件
				if(!IsMonitoring){
					/*		開啟Service並跟新程式狀態		*/
					Intent myServIntent = new Intent(LifeRecorder.this, SensorService.class);
	    			startService(myServIntent);
	    			IsMonitoring=true;
	    			text.setText(R.string.Recording);
				}
			}});
		/*		設置暫停按鈕的事件		*/
        Stop.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
    			//新增一個Intent物件
				if(!IsMonitoring)
            		return;
    			new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    				.setMessage("是否暫停生活模式紀錄？").setPositiveButton("是",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int which) {
    								/*		透過BroadCast與Receiver的機制與Service溝通			*/
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
		super.onDestroy();
	}
    
	/*			改寫按下Back鍵的功能			*/
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
    
    //設置MENU
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        menu.add(0, 0, 0, "開始紀錄");
        menu.add(0, 1, 0, "暫停紀錄");
        menu.add(0, 2, 0, "離開系統");
        menu.add(0, 3, 0, "上傳檔案");
        return super.onCreateOptionsMenu(menu);
    }
    
	/*		設置MENU的事件			*/
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch(item.getItemId()) {
			/*		開始記錄		*/
            case 0:
            	if(IsMonitoring)
            		break;
    			//新增一個Intent物件
    			Intent myServIntent = new Intent(LifeRecorder.this, SensorService.class);
    			startService(myServIntent);
    			IsMonitoring=true;
    			text.setText(R.string.Recording);
                break;
			/*		暫停記錄		*/
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
			/*		離開程式		*/
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
			/*		上傳檔案		*/
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
    
	/*		確認Service是否正在執行			*/
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
