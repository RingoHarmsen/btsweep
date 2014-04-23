package com.example.btsweep;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	static int maxLines=5;
	static int backTime=-1;
	ListView listView;
	private static final int REQUEST_ENABLE_BT = 1;
	boolean allreadyLogged =false;
	boolean noSound=true; 
	ListView listDevicesFound;
	static  ArrayList <BtEvent> foundBtDevices = new ArrayList <BtEvent>();
	ArrayList <String> logLines = new ArrayList <String>(); 
	static ArrayAdapter<String>fountBtDevicesArrayAdapter;
	Button btnScanDevice;
	Button btnScanclearlist;
	Button btnNoSound;
	Button btnExtrada;
	TextView stateBluetooth;
	BtEvent foundDevice = new BtEvent();
	BluetoothAdapter bluetoothAdapter;
	public static int selectedItem;
	private static Context context;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // listView = (ListView) findViewById(R.id.devicesfound);
		btnScanDevice = (Button)findViewById(R.id.scandevice);
		btnExtrada = (Button)findViewById(R.id.extrada);
		btnScanclearlist = (Button)findViewById(R.id.clearlist);
		btnNoSound = (Button)findViewById(R.id.nosound);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		listDevicesFound = (ListView)findViewById(R.id.devicesfound);   // R.layout.list_element
		fountBtDevicesArrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.list_element);
		listDevicesFound.setAdapter(fountBtDevicesArrayAdapter);
		CheckBlueToothState();
		// *** Bind buttons to the onClickListener ***//
		btnScanDevice.   setOnClickListener(btnScanDeviceOnClickListener);
		btnScanclearlist.setOnClickListener(btnScanclearlistOnClickListener);
		btnExtrada.      setOnClickListener(btnExtradaOnClickListener);
		btnNoSound.      setOnClickListener(btnOnClickListener);
		
					// Load logfile
		MainActivity.context = getApplicationContext();
		parselines(Environment.getExternalStorageDirectory()+ "/bt_log69.txt") ; 
		registerReceiver(ActionFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		class Task implements Runnable {
			@Override
			public void run() {
				for (int i = 0; true ; i++) {
					bluetoothAdapter.startDiscovery(); //
					int counter = 0; 
					for (counter=0;counter<5;counter++) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					bluetoothAdapter.cancelDiscovery(); // qq
				}
			}
		}
		try {
			logTextLine("started\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		new Thread(new Task()).start();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
        case R.id.action_settings:
            // Single menu item is selected do something
            // Ex: launching new activity/screen or show alert message
            // Toast.makeText(MainActivity.this, "Selected settings...", Toast.LENGTH_SHORT).show();
            MaxDevicesFragment maxDev = new MaxDevicesFragment() ;
            maxDev.show(getFragmentManager(), "lol");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }    
    
    
    public static Context getAppContext() {
        return MainActivity.context;
    }
    
    static public void displayIndex(int number){
    	// Toast.makeText(getAppContext(), "The chosen item index is: "+ number, Toast.LENGTH_SHORT).show();
    	// int maxLines=5; 
    	switch (number) {
		case 0:
			maxLines = 5 ;
		break;
		case 1:
			maxLines = 10;
		break;
		case 2:
			maxLines = 15;
			break;  // last hour and today 
		case 3: 
			backTime = 3600;
			break;
		case 4:
			backTime = 3600 * 24 ; 

		default:
			maxLines = 25;
			break;
		}
    	sortdalist();
    }
   
    public void readlines(String filename) {
    	try {
    		// open the file for reading
    		InputStream instream = new FileInputStream(filename);

    		// if file the available for reading
    		if (instream != null) {
    		  // prepare the file for reading
    		  InputStreamReader inputreader = new InputStreamReader(instream);
    		  BufferedReader buffreader = new BufferedReader(inputreader);

    		  String line;

    		  // read every line of the file into the line-variable, on line at the time
    		  do {
    		     line = buffreader.readLine();
    		    // do something with the line
    		 	logLines.add(line);
    		  } while (line != null);
    		  buffreader.close();
    		}
    	} catch (Exception ex) {
    	    // print stack trace.
    	} finally {
    		// close the file.
    	}
    }
    
    private void CheckBlueToothState(){
		if (bluetoothAdapter == null){
			Toast.makeText(getApplicationContext(), "Geen bluetooth!", Toast.LENGTH_SHORT).show();
		} else {
			if (bluetoothAdapter.isEnabled()){
				if(bluetoothAdapter.isDiscovering()){
					Toast.makeText(getApplicationContext(), "Bluetooth is in discovery.", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), "Bluetooth is ok.", Toast.LENGTH_SHORT).show();
					btnScanDevice.setEnabled(true);
				}
			}else{
				Toast.makeText(getApplicationContext(), "Bluetooth staat uit!", Toast.LENGTH_SHORT).show();
 				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}
    
    					//*** OnClickListeners ***//
	private Button.OnClickListener btnOnClickListener
	= new Button.OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (noSound == true) {
				btnNoSound.setText("Geluid uit");
				noSound = false;
				Toast.makeText(getApplicationContext(), "Geluid is aan.", Toast.LENGTH_SHORT).show();
			}   else  {
				btnNoSound.setText("Geluid aan");
				noSound = true; 
				Toast.makeText(getApplicationContext(), "Geluid is uit.", Toast.LENGTH_SHORT).show();
			}
		}};
	
	private Button.OnClickListener btnScanDeviceOnClickListener
	= new Button.OnClickListener(){
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			// fountBtDevicesArrayAdapter.clear();
			bluetoothAdapter.cancelDiscovery();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bluetoothAdapter.startDiscovery();
		}};
		
	private Button.OnClickListener btnScanclearlistOnClickListener
	= new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// fountBtDevicesArrayAdapter.clear();
				foundBtDevices.clear();
				fountBtDevicesArrayAdapter.clear();
			}};
			
	private Button.OnClickListener btnExtradaOnClickListener
		= new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				int pid = android.os.Process.myPid();                                                                                       
				android.os.Process.killProcess(pid);
			}};

			@Override
			protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				// TODO Auto-generated method stub
				if(requestCode == REQUEST_ENABLE_BT){
					CheckBlueToothState();
				}
	}
		    // *** OnClickListeners END  ***//

	public void logTextLine(String Line) throws IOException {
				FileWriter f;
				try {
					// open file zonder buffer 
					f = new FileWriter(Environment.getExternalStorageDirectory()+ "/bt_log69.txt" , true);  // true means append !!!!!!!!!!! 

					// append iets ... 
					f.write(Line);

					// sluit de file weer af
					f.flush();
					f.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// ** opm. denk aan de permissies in Manifest.xml
			}
	
    public void parselines(String filename) {
		String btName;
		String loggedAt;
		String macAdres;
    	try {
    		InputStream instream = new FileInputStream(filename);
    		if (instream != null) {
    			InputStreamReader inputreader = new InputStreamReader(instream);
    			BufferedReader buffreader = new BufferedReader(inputreader);
    			String line;
    			int lineCount=0; 
    			do {
    				line = buffreader.readLine();
    				lineCount ++;
    				// line = (String) logLines.get(i) + " ";
    				if (line.contains(";"))  {
    					StringTokenizer tokens = new StringTokenizer(line, ";");
    					btName = tokens.nextToken();
    					loggedAt=tokens.nextToken();
    					macAdres=tokens.nextToken();
    					Boolean isFound=false;
    					if (foundBtDevices.size() > 0)  {		// udate it
    						for (int j=0;j < foundBtDevices.size(); j++){
    							if(macAdres.equals(foundBtDevices.get(j).getMacAdresBtDevice()))   {
    								//update it 
    								foundBtDevices.get(j).setFoundBtDateTime(loggedAt);
    								isFound=true;
    							}
    						} // End for (int j=0;j < foundBtDevices.size(); j++){
    					}  // End foundBtDevices.size() > 0
    					if (isFound==false) {    					//add it
    						Log.d("qqq5","Added "+ btName);
    						foundDevice = new BtEvent();
    						foundDevice.setBtDeviceName(btName);
    						foundDevice.setFoundBtDateTime(loggedAt);
    						foundDevice.setMacAdresBtDevice(macAdres);
    						foundBtDevices.add(foundDevice);
    					}
    				}    				
    			} while (line != null  && lineCount < 15000 ); // na regel 15000 geen juiste csv .... 
    			buffreader.close();
    			sortdalist();
    		}
    	} catch (Exception ex) {
    	} finally {
    		// *** Empty ***
    	}
    }
    
	private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			// stateBluetooth.setText(action);
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				SimpleDateFormat s = new SimpleDateFormat("HH:mm ss   dd-MM-yyyy", Locale.ENGLISH);
				String format = s.format(new Date());
				long unixTime = System.currentTimeMillis() / 1000L;
				foundDevice = new BtEvent();
				foundDevice.setMacAdresBtDevice(device.getAddress());
				foundDevice.setBtDeviceName(device.getName());
				foundDevice.setFoundBtDateTime(format);
				foundDevice.setUnixTime(unixTime);
				boolean deviceAllreadyFound = false; 
						// Check if it exists in array  foundBtDevices 
				for (int i=0; i < foundBtDevices.size(); i++){
					if (foundBtDevices.get(i).getMacAdresBtDevice().equals(foundDevice.getMacAdresBtDevice())) {
							foundBtDevices.get(i).setBtDeviceName(foundDevice.getBtDeviceName());							
							foundBtDevices.get(i).setFoundBtDateTime(foundDevice.getFoundBtDateTime());
							foundBtDevices.get(i).setUnixTime(foundDevice.getUnixTime());
							deviceAllreadyFound  = true;
							Log.d("qqq", foundBtDevices.get(i).getMacAdresBtDevice()+ " "+foundDevice.getMacAdresBtDevice()  );
					}
				}
				if (deviceAllreadyFound  == false) {
					foundBtDevices.add(foundDevice);
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(500);
					if ( noSound  == false  ) {
						Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
						r.play();
					}
				}

							// **** log it ';' seperated to a file ***
				try {
					logTextLine(device.getName()+";"+ format + ";"+  device.getAddress() + ";"+ foundDevice.getUnixTime() + ";"+ ";\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				sortdalist();
			}
		}};
		
		static void sortdalist(){
			// sort on uniTime DESC
			Collections.sort(foundBtDevices);
			fountBtDevicesArrayAdapter.clear();   // Remove all elements..
			long currentUnixTime = System.currentTimeMillis() / 1000L;
			for (int q=0; q < foundBtDevices.size() &&  q < maxLines ;   q++){    // foundBtDevices = arrayList of btEvents
				// check for backTime
				if (backTime == -1){
					fountBtDevicesArrayAdapter.add(foundBtDevices.get(q).getScreenString());
				}  else {
					if (currentUnixTime - backTime  < foundBtDevices.get(q).getUnixTime()  ) {
						fountBtDevicesArrayAdapter.add(foundBtDevices.get(q).getScreenString());
					}
				}
				// fountBtDevicesArrayAdapter.getView(q, context , parent);
			}
			fountBtDevicesArrayAdapter.notifyDataSetChanged();
		}
} /*** End of class MainActivity ***/
