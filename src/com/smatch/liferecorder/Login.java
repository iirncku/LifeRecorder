package com.smatch.liferecorder;

/**
 * 上傳檔案
 * **/

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class Login extends Activity {
    /** Called when the activity is first created. */
	private Button UploadBT1, BackBT;  //login button
	private boolean bInternetConnectivity=false;
	private EditText Date;
	private String newName="";
	private String filepath="";	
	private int check=1, count=0;
	private String actionUrl="";
	private SharedPreferences save;
	private String UserID="";
	private String StartUploadDate="";
	private String time="";
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login);
        Date = (EditText)findViewById(R.id.userdate);
        UploadBT1 = (Button)findViewById(R.id.uploadbt);
        BackBT=(Button)findViewById(R.id.backbt);
        /* 呼叫Homepage中的checkInternetConnection測試手機是否具有連線server的連線能力 */ 
        if(checkInternetConnection("http://140.116.247.50:8080/dudu/activitypattern","utf-8"))   
        { 
          bInternetConnectivity=true; 
        }
        save = getSharedPreferences("save", MODE_PRIVATE);
        UserID = save.getString("UserID", "");
		Date.setText(UserID);
		///////////得到日期////////////////
		
		time=formatter.format(new java.util.Date());
        StartUploadDate = save.getString("UpdateDate", time);
        //Date.setText(StartUploadDate);
        BackBT.setOnClickListener(new Button.OnClickListener(){
    		//按下按鈕後離開這一頁
    		public void onClick(View arg0){
				Login.this.finish();
    		}
		
    	});
        
        
        //如果網路正常..
        if(bInternetConnectivity==true){
        	UploadBT1.setOnClickListener(new Button.OnClickListener(){
        		public void onClick(View arg0){
        			actionUrl="http://140.116.247.50:8080/dudu/phoneupload1.php";        			
        			String ServerURL="http://140.116.247.50:8080/dudu/phoneupload1.php";
            		
        			//建立HTTP Post連線
        			HttpPost httpRequest=new HttpPost(ServerURL);
        		
        			//Post運作傳送變數必須用NameValuePair[]陣列儲存
        			List <NameValuePair> params=new ArrayList <NameValuePair>();
        		
        			try{
        				//發出HTTP request
        				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			
        				//取得HTTP response
        				HttpResponse httpResponse=new DefaultHttpClient().execute(httpRequest);
        			
        				//如果狀態碼為200  ok
        				if(httpResponse.getStatusLine().getStatusCode()==200){
        					String[] updateList= UpdateList();
        					
        					//filepath="/sdcard/MySensor/Features";
        					//File f=new File(filepath);
        					//File[] files=f.listFiles();
        					
        					//if(files.length!=0){
        					if(updateList.length!=0){
        						//將所有檔案加入ArrayList
        						for(int i=0;i<updateList.length;i++){    
        							
        							File uploadfile=new File("/sdcard/MySensor/Features/"+updateList[i]);
        							newName=uploadfile.getName();
        							//呼叫upload方法把檔案傳到伺服器
        							upload(uploadfile.getPath());
        							if(check==0){
        								count+=1;
        							}
        						}
                    	
        						if(count!=0){
        							time=formatter.format(new java.util.Date());
        							showDialog("特徵檔案上傳完成！");
        							Editor editor = save.edit();
        							editor.putString("UpdateDate",time );
        							editor.commit();
        							StartUploadDate=time;
        						}else{
        							showDialog("上傳不完全，請稍候再試。");
        						}
        					}else{
        						showDialog("沒有檔案可以上傳！");
        					}
        				}else{
        					Log.d("httpResponse",httpResponse.getStatusLine().toString());
        					showDialog(httpResponse.getStatusLine().toString());
        				}
        			}catch (ClientProtocolException e){
        				Log.d("ClientProtocolException",e.getMessage().toString());
        				showDialog(e.getMessage().toString());
        				e.printStackTrace();
        			}catch (IOException e){
        				Log.d("IOException",e.getMessage().toString());
        				showDialog(e.getMessage().toString());
        				e.printStackTrace();
        			}catch (Exception e){
        				Log.d("Exception",e.getMessage().toString());
        				showDialog(e.getMessage().toString());
        				e.printStackTrace();
        			}
        		}
			
        	});
        }else{
        	//網路無法連線的警示
        	UploadBT1.setOnClickListener(new Button.OnClickListener() {
    			public void onClick(View arg0) {
    				showDialog("網路無法連線！請檢查網路。");
    			}
    		});
        }
    }
    private String[] UpdateList() throws IOException{
    	
    	FileReader fs = new FileReader("/sdcard/MySensor/MonitorLog.txt");
    	BufferedReader bf=new BufferedReader(fs);
    	List<String> ul=new ArrayList<String>();
    	String fn=null;
    	String[] todate=StartUploadDate.split("-");
    	int[] StartUploadDateInt=new int[6];
    	for(int i=0;i<todate.length;i++){
    		StartUploadDateInt[i]=Integer.parseInt(todate[i]);
    	}
    	String ud="";
    	boolean NeedUpdate=false;
    	//boolean IsUpdate=false;
    	while((fn=bf.readLine())!=null){
    		if(fn.trim().length()!=0){
    			String[] s=fn.split("-");
    			if(!NeedUpdate){
    				for(int i=1;i<s.length;i++){
        				if(Integer.parseInt(s[i])>StartUploadDateInt[i-1]){
        					NeedUpdate=true;
        					break;
        				}else if(Integer.parseInt(s[i])<StartUploadDateInt[i-1]){
        					NeedUpdate=false;
        					break;
        				}
        			}
    				if(NeedUpdate){
    					if(!ud.equals(s[0]+"-"+s[1]+"-"+s[2]+"-"+s[3])){
    						ud=s[0]+"-"+s[1]+"-"+s[2]+"-"+s[3];
    						ul.add(ud+".txt");
    					}
    				}
    			}else{
    				if(!ud.equals(s[0]+"-"+s[1]+"-"+s[2]+"-"+s[3])){
						ud=s[0]+"-"+s[1]+"-"+s[2]+"-"+s[3];
						ul.add(ud+".txt");
					}
    			}
    		}
    	}

    	String[] result=new String[ul.size()];
    	ul.toArray(result);
    	return result;
    }
    
    /*對話框*/
    private void showDialog(String message){
    	new AlertDialog.Builder(Login.this)
    	.setTitle("訊息")
    	.setMessage(message)
    	.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}})
		.show();
    }
    
    /* 上傳檔案至Server的method */
    private int upload(String uploadFile)
    {
      String end = "\r\n";
      String twoHyphens = "--";
      String boundary = "*****";
      try
      {
        URL url =new URL(actionUrl);
        HttpURLConnection con=(HttpURLConnection)url.openConnection();
        /* 允許Input、Output，不使用Cache */
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        /* 設定傳送的method=POST */
        con.setRequestMethod("POST");
        /* setRequestProperty */
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type",
                           "multipart/form-data;boundary="+boundary);
        /* 設定DataOutputStream */
        DataOutputStream ds = 
          new DataOutputStream(con.getOutputStream());
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"thefile\";filename=\"" +
                      newName +"\"" + end);
        ds.writeBytes(end);   

        /* 取得檔案的FileInputStream */
        FileInputStream fStream = new FileInputStream(uploadFile);
        /* 設定每次寫入1024bytes */
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = -1;
        /* 從檔案讀取資料至緩衝區 */
        while((length = fStream.read(buffer)) != -1)
        {
          /* 將資料寫入DataOutputStream中 */
          ds.write(buffer, 0, length);
        }
        ds.writeBytes(end);
        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

        /* close streams */
        fStream.close();
        ds.flush();
        
        /* 取得Response內容 */
        InputStream is = con.getInputStream();
        int ch;
        StringBuffer b =new StringBuffer();
        while( ( ch = is.read() ) != -1 )
        {
          b.append( (char)ch );
        }
        
        check=Integer.parseInt(b.toString().trim());
        
        /* 關閉DataOutputStream */
        ds.close();
      }
      catch(Exception e)
      {
        showDialog(""+e);
      }
      return check;
    }
    
    /* 檢查網路連線是否正常 */ 
    public static boolean checkInternetConnection 
    (String strURL, String strEncoding) 
    { 
      /* 最多延時n秒若無回應則表示無法連線 */ 
      int intTimeout = 5; 
      try 
      { 
        HttpURLConnection urlConnection= null; 
        URL url = new URL(strURL); 
        urlConnection=(HttpURLConnection)url.openConnection(); 
        urlConnection.setRequestMethod("GET"); 
        urlConnection.setDoOutput(true); 
        urlConnection.setDoInput(true); 
        urlConnection.setRequestProperty 
        ( 
          "User-Agent","Mozilla/4.0"+ 
          " (compatible; MSIE 6.0; Windows 2000)" 
        ); 
         
        urlConnection.setRequestProperty 
        ("Content-type","text/html; charset="+strEncoding);      
        urlConnection.setConnectTimeout(1000*intTimeout); 
        urlConnection.connect(); 
        if (urlConnection.getResponseCode() == 200) 
        { 
          return true; 
        } 
        else 
        { 
          return false; 
        } 
      } 
      catch (Exception e) 
      { 
        e.printStackTrace(); 
        return false; 
      } 
    } 
}