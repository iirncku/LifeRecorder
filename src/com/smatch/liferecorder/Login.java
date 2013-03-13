package com.smatch.liferecorder;

/**
 * �W���ɮ�
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
        /* �I�sHomepage����checkInternetConnection���դ���O�_�㦳�s�userver���s�u��O */ 
        if(checkInternetConnection("http://140.116.247.50:8080/dudu/activitypattern","utf-8"))   
        { 
          bInternetConnectivity=true; 
        }
        save = getSharedPreferences("save", MODE_PRIVATE);
        UserID = save.getString("UserID", "");
		Date.setText(UserID);
		///////////�o����////////////////
		
		time=formatter.format(new java.util.Date());
        StartUploadDate = save.getString("UpdateDate", time);
        //Date.setText(StartUploadDate);
        BackBT.setOnClickListener(new Button.OnClickListener(){
    		//���U���s�����}�o�@��
    		public void onClick(View arg0){
				Login.this.finish();
    		}
		
    	});
        
        
        //�p�G�������`..
        if(bInternetConnectivity==true){
        	UploadBT1.setOnClickListener(new Button.OnClickListener(){
        		public void onClick(View arg0){
        			actionUrl="http://140.116.247.50:8080/dudu/phoneupload1.php";        			
        			String ServerURL="http://140.116.247.50:8080/dudu/phoneupload1.php";
            		
        			//�إ�HTTP Post�s�u
        			HttpPost httpRequest=new HttpPost(ServerURL);
        		
        			//Post�B�@�ǰe�ܼƥ�����NameValuePair[]�}�C�x�s
        			List <NameValuePair> params=new ArrayList <NameValuePair>();
        		
        			try{
        				//�o�XHTTP request
        				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			
        				//���oHTTP response
        				HttpResponse httpResponse=new DefaultHttpClient().execute(httpRequest);
        			
        				//�p�G���A�X��200  ok
        				if(httpResponse.getStatusLine().getStatusCode()==200){
        					String[] updateList= UpdateList();
        					
        					//filepath="/sdcard/MySensor/Features";
        					//File f=new File(filepath);
        					//File[] files=f.listFiles();
        					
        					//if(files.length!=0){
        					if(updateList.length!=0){
        						//�N�Ҧ��ɮץ[�JArrayList
        						for(int i=0;i<updateList.length;i++){    
        							
        							File uploadfile=new File("/sdcard/MySensor/Features/"+updateList[i]);
        							newName=uploadfile.getName();
        							//�I�supload��k���ɮ׶Ǩ���A��
        							upload(uploadfile.getPath());
        							if(check==0){
        								count+=1;
        							}
        						}
                    	
        						if(count!=0){
        							time=formatter.format(new java.util.Date());
        							showDialog("�S�x�ɮפW�ǧ����I");
        							Editor editor = save.edit();
        							editor.putString("UpdateDate",time );
        							editor.commit();
        							StartUploadDate=time;
        						}else{
        							showDialog("�W�Ǥ������A�еy�ԦA�աC");
        						}
        					}else{
        						showDialog("�S���ɮץi�H�W�ǡI");
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
        	//�����L�k�s�u��ĵ��
        	UploadBT1.setOnClickListener(new Button.OnClickListener() {
    			public void onClick(View arg0) {
    				showDialog("�����L�k�s�u�I���ˬd�����C");
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
    
    /*��ܮ�*/
    private void showDialog(String message){
    	new AlertDialog.Builder(Login.this)
    	.setTitle("�T��")
    	.setMessage(message)
    	.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}})
		.show();
    }
    
    /* �W���ɮצ�Server��method */
    private int upload(String uploadFile)
    {
      String end = "\r\n";
      String twoHyphens = "--";
      String boundary = "*****";
      try
      {
        URL url =new URL(actionUrl);
        HttpURLConnection con=(HttpURLConnection)url.openConnection();
        /* ���\Input�BOutput�A���ϥ�Cache */
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        /* �]�w�ǰe��method=POST */
        con.setRequestMethod("POST");
        /* setRequestProperty */
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type",
                           "multipart/form-data;boundary="+boundary);
        /* �]�wDataOutputStream */
        DataOutputStream ds = 
          new DataOutputStream(con.getOutputStream());
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; " +
                      "name=\"thefile\";filename=\"" +
                      newName +"\"" + end);
        ds.writeBytes(end);   

        /* ���o�ɮת�FileInputStream */
        FileInputStream fStream = new FileInputStream(uploadFile);
        /* �]�w�C���g�J1024bytes */
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length = -1;
        /* �q�ɮ�Ū����Ʀܽw�İ� */
        while((length = fStream.read(buffer)) != -1)
        {
          /* �N��Ƽg�JDataOutputStream�� */
          ds.write(buffer, 0, length);
        }
        ds.writeBytes(end);
        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

        /* close streams */
        fStream.close();
        ds.flush();
        
        /* ���oResponse���e */
        InputStream is = con.getInputStream();
        int ch;
        StringBuffer b =new StringBuffer();
        while( ( ch = is.read() ) != -1 )
        {
          b.append( (char)ch );
        }
        
        check=Integer.parseInt(b.toString().trim());
        
        /* ����DataOutputStream */
        ds.close();
      }
      catch(Exception e)
      {
        showDialog(""+e);
      }
      return check;
    }
    
    /* �ˬd�����s�u�O�_���` */ 
    public static boolean checkInternetConnection 
    (String strURL, String strEncoding) 
    { 
      /* �̦h����n��Y�L�^���h��ܵL�k�s�u */ 
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