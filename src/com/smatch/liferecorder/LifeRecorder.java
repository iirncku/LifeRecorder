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
	private TextView text;                                         //��ܪ��A��TextView
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
    		//���U���s�����}�o�@��
    		public void onClick(View arg0){
    			new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    			.setMessage("�T�w���}�ͬ��Ҧ������t�ΡH").setPositiveButton("�O",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								LifeRecorder.this.finish();
    							}
    						}).setNegativeButton("�_",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								// Do nothing, Back to system
    							}
    						}).show();
    		}
    	});
        
        Monitor.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
    			//�s�W�@��Intent����
				if(!IsMonitoring){
					Intent myServIntent = new Intent(LifeRecorder.this, SensorService.class);
	    			startService(myServIntent);
	    			IsMonitoring=true;
	    			text.setText(R.string.Recording);
				}
			}});
        Stop.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
    			//�s�W�@��Intent����
				if(!IsMonitoring)
            		return;
    			new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    				.setMessage("�O�_�Ȱ��ͬ��Ҧ������H").setPositiveButton("�O",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int which) {
    								Intent i2 = new Intent("ACTSTOP");
    							    sendBroadcast(i2);
    								IsMonitoring=false;
    								text.setText(R.string.nonRecording);
    							}
    						}).setNegativeButton("�_",
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
			.setMessage("�T�w���}�ͬ��Ҧ������t�ΡH").setPositiveButton("�O",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int which) {
								LifeRecorder.this.finish();
							}
						}).setNegativeButton("�_",
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
        //�Ѽ�1:�s��id, �Ѽ�2:itemId, �Ѽ�3:item����, �Ѽ�4:item�W��
        menu.add(0, 0, 0, "�}�l����");
        menu.add(0, 1, 0, "�Ȱ�����");
        menu.add(0, 2, 0, "���}�t��");
        menu.add(0, 3, 0, "�W���ɮ�");
        return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        //�̾�itemId�ӧP�_�ϥΪ��I����@��item
        switch(item.getItemId()) {
            case 0:
            	if(IsMonitoring)
            		break;
    			//�s�W�@��Intent����
    			Intent myServIntent = new Intent(LifeRecorder.this, SensorService.class);
    			startService(myServIntent);
    			IsMonitoring=true;
    			text.setText(R.string.Recording);
                break;
            case 1:
            	if(!IsMonitoring)
            		break;
    			new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    				.setMessage("�O�_�Ȱ��ͬ��Ҧ������H").setPositiveButton("�O",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog, int which) {
    								Intent i2 = new Intent("ACTSTOP");
    							    sendBroadcast(i2);
    								IsMonitoring=false;
    								text.setText(R.string.nonRecording);
    							}
    						}).setNegativeButton("�_",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								// Do nothing, Back to system
    							}
    						}).show();
    			break;
            case 2:
            	new AlertDialog.Builder(LifeRecorder.this).setTitle("Alert")
    			.setMessage("�T�w���}�ͬ��Ҧ������t�ΡH").setPositiveButton("�O",
    						new DialogInterface.OnClickListener() {
    							public void onClick(DialogInterface dialog,	int which) {
    								LifeRecorder.this.finish();
    							}
    						}).setNegativeButton("�_",
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
