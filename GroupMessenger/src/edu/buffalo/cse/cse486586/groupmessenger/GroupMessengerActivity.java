package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

	static final String TAG = "GroupMessenger TAG";
	static final int SERVER_PORT = 10000;
	ArrayList<ProcessSpec> causalArray = new ArrayList<ProcessSpec>();
	String[] group = {"11108","11112","11116","11120","11124"};
	String currentPort;
	String msg;
	ProcessSpec senderSpec;
	Socket socket;
	static int globalSequenceNO ;
	TextView tv;
	ServerSocket serverSocket;
	int[] expectedVector={0,0,0,0,0};
	int[] actualVector={0,0,0,0,0};
	private Uri mUri;
	@SuppressLint("UseSparseArrays")
	Map <Integer, ProcessSpec> orderedQueue = new HashMap<Integer, ProcessSpec>();
	static int localNo = 1;
	ContentResolver mContentResolver;
	int avdNo;
	boolean causalCondition;

	private Uri buildUri(String scheme, String authority) 
	{
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	/*
	 *   Code taken from PA1
	 */
	public String getPort(){
		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		return myPort;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_messenger);
		mUri = buildUri("content","edu.buffalo.cse.cse486586.groupmessenger.provider");
		mContentResolver = getContentResolver();

		/*
		 *  Getting current avd's port and starting globalSequence no.
		 */
		currentPort = getPort();
		globalSequenceNO = 0;
		avdNo = Integer.parseInt(currentPort)%11108;
		avdNo = avdNo/4;
		/*
		 * TODO: Use the TextView to display your messages. Though there is no grading component
		 * on how you display the messages, if you implement it, it'll make your debugging easier.
		 */
		tv = (TextView) findViewById(R.id.textView1);
		tv.setMovementMethod(new ScrollingMovementMethod());
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {
			Log.e(TAG, "Can't create a ServerSocket "+e);
			return;
		}
		/*
		 * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
		 * OnPTestClickListener demonstrates how to access a ContentProvider.
		 */
		findViewById(R.id.button1).setOnClickListener(
				new OnPTestClickListener(tv, getContentResolver()));

		final EditText editText = (EditText) findViewById(R.id.editText1);
		Button client = (Button) findViewById(R.id.button4);
		client.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				msg = editText.getText().toString();
				editText.setText("");
				actualVector[avdNo] = actualVector[avdNo] + 1; 
				senderSpec = new ProcessSpec(msg,0,currentPort,0);
				senderSpec.setAvdNo(avdNo);
				new ClientTask().execute(senderSpec);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
		return true;
	}

	/*
	 *      Server and Client Tasks code are inspired by code from PA1
	 * 
	 */
	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
		ObjectInputStream objectIntputStream = null;
		ProcessSpec senderSpec;
		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			Socket soc = null;
			try{
				while(true){
					soc = serverSocket.accept();
					objectIntputStream = new ObjectInputStream(soc.getInputStream());
					senderSpec = (ProcessSpec) objectIntputStream.readObject();
					Log.e(TAG,"vector is "+Arrays.toString(actualVector));
					causalArray.add(senderSpec);
					Iterator<ProcessSpec> its = causalArray.iterator();
					while(its.hasNext()){
						causalCondition = false;
						senderSpec = its.next();
						//actualVector = senderSpec.getLocalVector();
						avdNo = senderSpec.getAvdNo();
						if(actualVector[avdNo] == expectedVector[avdNo]+1){

							for(String s : group){
								int avdCheck = Integer.parseInt(currentPort)%11108;
								avdCheck = avdCheck/4;
								if(!(avdCheck==avdNo)){
									if(actualVector[avdCheck] <= expectedVector[avdCheck]){
										causalCondition = true;
										expectedVector[avdCheck] = expectedVector[avdCheck] + 1;
										//actualVector[avdCheck] = actualVector[avdCheck] + 1;
										Log.e(TAG,"vector is "+Arrays.toString(actualVector));
										Log.e(TAG,"vector is "+Arrays.toString(actualVector));
										Log.e(TAG,"vector is "+Arrays.toString(expectedVector));
									Log.e(TAG,"vector is "+Arrays.toString(expectedVector));
										
									}
								}
							}
						}
					}
					if(causalCondition){
						
						if(senderSpec.getSequenceNO()==0){
							globalSequenceNO++;
							senderSpec.setSequenceNO(globalSequenceNO);
							new ClientTask().execute(senderSpec);

						}else if(!(senderSpec.getSequenceNO()==0)){
							if (localNo == senderSpec.getSequenceNO()){
								ContentValues contentValues = new ContentValues();
								contentValues.put("key", senderSpec.getSequenceNO()-1);
								contentValues.put("value", senderSpec.getMsg());
								mContentResolver.insert(mUri, contentValues);
								Log.e(TAG,"Sending msg : "+senderSpec.getMsg()+" : "+senderSpec.getSequenceNO());
								publishProgress(senderSpec.getMsg());
								senderSpec.setLocalNo(senderSpec.getLocalNo()+1);
								localNo++;
								if(!orderedQueue.isEmpty()){
									Iterator<?> it = orderedQueue.entrySet().iterator();
									while(it.hasNext()){
										@SuppressWarnings("unchecked")
										Entry<Integer, ProcessSpec> entry = (Entry<Integer, ProcessSpec>) it.next();
										if(entry.getKey()==senderSpec.getSequenceNO()+1){
											contentValues = new ContentValues();
											contentValues.put("key", senderSpec.getSequenceNO()-1);
											contentValues.put("value", senderSpec.getMsg());
											mContentResolver.insert(mUri, contentValues);
											Log.e(TAG,"Sending msg : "+senderSpec.getMsg()+" : "+senderSpec.getSequenceNO());
											publishProgress(senderSpec.getMsg());
											senderSpec.setLocalNo(senderSpec.getLocalNo()+1);
											localNo++;
											orderedQueue.remove(entry.getKey());
										}
									}
								}
							}
							else if (!(localNo == senderSpec.getSequenceNO())){
								orderedQueue.put(senderSpec.getSequenceNO(), senderSpec);
							}
						}
					}
				}
			}catch(Exception e){
				Log.e(TAG, "Inside server class Exception "+e);
			}
			return null;
		}
		protected void onProgressUpdate(String...strings) {

			tv.append(strings[0]+'\n');
			return;
		}
	}
	void multicast(ProcessSpec senderSpec) {
		for(String remotePort : group){
			sendToServer(senderSpec,remotePort);
		}
		/*
		 *  To send processSpec across all AVDS with updated sequence no from sequencer
		 *  Following code snippet is taken from PA1 and i have modified it.
		 *  B multicast has 2 parts sending senderSpec to group and to sequencer
		 *  Following code is sending the senderSpec to group which should be received on server side.
		 */
	}
	void sendToServer(ProcessSpec senderSpec,String remotePort){
		ObjectOutputStream objectOutputStream = null;
		try {
			socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
					Integer.parseInt(remotePort));
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(senderSpec);
		}catch (UnknownHostException e) {
			Log.e(TAG, "ClientTask UnknownHostException  "+e );
		} catch (IOException e) {
			Log.e(TAG, "ClientTask socket IOException  " + e);
		}
		finally{
			try {
				objectOutputStream.close();
				socket.close();		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private class ClientTask extends AsyncTask<ProcessSpec, Void, Void> {
		@Override
		protected Void doInBackground(ProcessSpec... params) {
			// TODO Auto-generated method stub
			if(params[0].getSequenceNO()==0){
				Sequencer(params[0]);
			}else if(!(params[0].getSequenceNO()==0)){
				multicast(params[0]);
			}
			return null;
		}        
	}   
	void Sequencer(ProcessSpec senderSpec){
		sendToServer(senderSpec,"11108");
	}
}
