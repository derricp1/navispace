package derricp1.apps.navi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class TestActivity extends Activity implements SensorEventListener {
	
	public int t_signals = 10;
	public int t_tests = 100;
	public int[] t_scans = new int[t_signals];
	public int[] t_this = new int[t_signals];
	public String[] t_ids = new String[t_signals];
	
	public float NOISE = (float) 0.5;
	public float alpha = (float) 0.8;

	private boolean mInitialized = false;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private SensorManager mSensorManager2;
	private Sensor mGyroscope;
	
	private float mLastX, mLastY, mLastZ;
	private float gravity[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager2 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mGyroscope = mSensorManager2.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager2.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
		
		gravity = new float[3];
		for (int i=0; i<3; i++)
			gravity[i] = (float) 9.8;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	
	public void signaltest(View view) {
		
		//on button test, display stuff
		//or not lol
		
		//Print out signal strengths at points
		
/*		t_ids[0] = "d8:c7:c8:6e:81:a9";
		t_ids[1] = "d8:c7:c8:ab:2e:e8";
		t_ids[2] = "d8:c7:c8:6e:81:a8";
		t_ids[3] = "d8:c7:c8:ab:21:50";
		t_ids[4] = "d8:c7:c8:6e:81:a8";
		t_ids[5] = "d8:c7:c8:ab:2e:e9";
		t_ids[6] = "d8:c7:c8:6e:7f:a9";
		t_ids[7] = "d8:c7:c8:ab:21:52";
		t_ids[8] = "d8:c7:c8:ab:2e:e0";
		t_ids[9] = "d8:c7:c8:ab:21:58";	
		
		t_ids[0] = "d8:c7:c8:ab:29:82";
		t_ids[1] = "d8:c7:c8:ab:29:81";
		t_ids[2] = "d8:c7:c8:ab:29:f1";
		t_ids[3] = "d8:c7:c8:ab:2c:18";
		t_ids[4] = "d8:c7:c8:ab:29:b8";
		t_ids[5] = "d8:c7:c8:ab:2c:19";
		t_ids[6] = "d8:c7:c8:ab:2f:80";
		t_ids[7] = "d8:c7:c8:ab:26:d8";
		t_ids[8] = "d8:c7:c8:ab:2f:81";
		t_ids[9] = "d8:c7:c8:ab:26:d1";	
		
		t_ids[0] = "d8:c7:c8:ab:29:ea";
		t_ids[1] = "d8:c7:c8:ab:23:0a";
		t_ids[2] = "d8:c7:c8:ab:2e:99";
		t_ids[3] = "d8:c7:c8:ab:2e:d9";
		t_ids[4] = "d8:c7:c8:ab:2e:91";
		t_ids[5] = "d8:c7:c8:ab:29:e8";
		t_ids[6] = "d8:c7:c8:ab:2a:00";
		t_ids[7] = "d8:c7:c8:ab:2c:2a";
		t_ids[8] = "d8:c7:c8:ab:2a:08";
		t_ids[9] = "d8:c7:c8:ab:2c:50";	
		
		boolean tester = true;
		
		while (tester == true) {		
			
			WifiManager myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			
			for(int z=0; z<t_tests; z++) {
				
				for(int i=0; i<t_signals; i++)
					t_this[i] = -1;
				
				if(myWifiManager.startScan()){ //Can we start?
					// List available APs
					List<ScanResult> scans = myWifiManager.getScanResults();
					if (scans != null && !scans.isEmpty()){
						for (ScanResult scan : scans) {
							int found = -1;
							
							for(int i=0; i<t_signals; i++) {
								if (t_ids[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
									found = i;
								}
							}
							
							if (found > -1) {
								t_this[found] = WifiManager.calculateSignalLevel(scan.level, 100);
							}
							
						}
					}
				}
				
				for(int i=0; i<t_signals; i++)
					t_scans[i] = t_scans[i] + t_this[i];
				
			}
	
			for(int q=0; q<t_signals; q++) {
				t_scans[q] = (int) Math.ceil(t_scans[q]/t_tests);
			}
			
			tester = false;
		
		}	
		
		TextView row = (TextView) findViewById(R.id.row1); 
		row.setText(Integer.toString(t_scans[0]));
		row = (TextView) findViewById(R.id.row2); 
		row.setText(Integer.toString(t_scans[1]));
		row = (TextView) findViewById(R.id.row3); 
		row.setText(Integer.toString(t_scans[2]));
		row = (TextView) findViewById(R.id.row4); 
		row.setText(Integer.toString(t_scans[3]));
		row = (TextView) findViewById(R.id.row5); 
		row.setText(Integer.toString(t_scans[4]));
		row = (TextView) findViewById(R.id.row6); 
		row.setText(Integer.toString(t_scans[5]));
		row = (TextView) findViewById(R.id.row7); 
		row.setText(Integer.toString(t_scans[6]));
		row = (TextView) findViewById(R.id.row8); 
		row.setText(Integer.toString(t_scans[7]));
		row = (TextView) findViewById(R.id.row9); 
		row.setText(Integer.toString(t_scans[8]));
		row = (TextView) findViewById(R.id.row10); 
		row.setText(Integer.toString(t_scans[9]));*/
		
		//Make sure reading in works
		
/*		int SIGNALS = 10;
		int mapsize = 455;
		int[][] nodess;
		double[] nodex;
		double[] nodey;
		
		WifiManager myWifiManager2 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		boolean wasEnabled2 = myWifiManager2.isWifiEnabled();
		int lastnode = -1;
		int currnode = -1;
		int nextnode = -1;
		boolean success = false;
		boolean canspeak = true;
		
		int[] currsig = new int[SIGNALS];
		int tests = 333;
		
		int stickiness = 3;
		int sticknode = -1; //node that's trying to be moved to
		int stucknode = -1; //node we are "stuck" at
		
		//obstacles
		int obs = 5;
		double[] ox;
		double[] oy;
		double[] or;
		int[] oends = new int[3]; //where the points at each end
		
		int[] fstarts = new int[3]; //where the floor node ranges lie.
		int[] fends = new int[3]; //where the floor node ranges lie.
		
		//0-233: floor 2
		//234-383: floor 3
		//384+: floor 4
		fstarts[0] = 0;
		fends[0] = 233;
		fstarts[1] = 234;
		fends[1] = 383;
		fstarts[2] = 384;
		fends[2] = 454;
		
		int countdown = 240*15;
		
        //Read in absolute coordinates of nodes
		InputStream is = getResources().openRawResource(R.raw.nodelocs);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		try {
			String str = br.readLine(); //Reads the number of nodes - should be the same as before
			mapsize = Integer.parseInt(str);
			
			//Sets up arrays
			
			str = br.readLine(); //Discards hash
		} 
		catch (IOException e) {
			finish(); //should not reach here
		}
		
		nodess = new int[SIGNALS][mapsize];
		nodex = new double[mapsize];
		nodey = new double[mapsize];
		
		//read in obstacles
		InputStream isb = getResources().openRawResource(R.raw.obs);
		InputStreamReader isrb = new InputStreamReader(isb);
		BufferedReader brb = new BufferedReader(isrb);					
		
		try {
			String str = brb.readLine(); //Reads the number of obstacles and the end index per floor
			obs = Integer.parseInt(str);
			str = brb.readLine();
			oends[0] = Integer.parseInt(str);
			str = brb.readLine();
			oends[1] = Integer.parseInt(str);
			oends[2] = obs-1;
			str = brb.readLine(); //Discards hash
		} 
		catch (IOException e) {
			finish(); //should not reach here
		}
		
		ox = new double[obs];
		oy = new double[obs];
		or = new double[obs];
		
		try { //Read in strength and location data
			for (int i=0; i< mapsize; i++) {
				String str = br.readLine(); //Reads in data
				nodex[i] = Double.parseDouble(str);
				str = br.readLine(); //Reads in data
				nodey[i] = Double.parseDouble(str);
				
				for (int j=0; j<SIGNALS; j++) {
					
					str = br.readLine();
					nodess[j][i] = Integer.parseInt(str);					
					
				}

				str = br.readLine(); //Discards hash

			}
		} 
		catch (IOException e) {
			finish(); //should not reach here either
		}
		
		try { //Read in location data for obstacles
			for (int i=0; i< obs; i++) {
				String str = brb.readLine(); //Reads in data
				ox[i] = Double.parseDouble(str);
				str = brb.readLine(); //Reads in data
				oy[i] = Double.parseDouble(str);
				str = brb.readLine(); //Reads in data
				or[i] = Double.parseDouble(str);
				
				str = brb.readLine(); //Discards hash

			}
		} 
		catch (IOException e) {
			finish(); //should not reach here either
		}	*/
		
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		//not changing anyway
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		//Left turns seem to have a positive x briefly, right turns negative
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){ 
			
			float x, y, z;
			
	        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
	        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
	        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

	        x = event.values[0] - gravity[0];
	        y = event.values[1] - gravity[1];
	        z = event.values[2] - gravity[2];
			
			if (!mInitialized) {
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				mInitialized = true;
			}
			else {
				float deltaX = mLastX - x;
				float deltaY = mLastY - y;
				float deltaZ = mLastZ - z;
				if (Math.abs(deltaX) < NOISE) deltaX = (float)0.0;
				if (Math.abs(deltaY) < NOISE) deltaY = (float)0.0;
				if (Math.abs(deltaZ) < NOISE) deltaZ = (float)0.0;
				if (Math.abs(x) < NOISE) x = (float)0.0;
				if (Math.abs(y) < NOISE) y = (float)0.0;
				if (Math.abs(z) < NOISE) z = (float)0.0;
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				
				TextView row = (TextView) findViewById(R.id.row1); 
				row.setText(Float.toString(x));
				row = (TextView) findViewById(R.id.row2); 
				row.setText(Float.toString(y));
				row = (TextView) findViewById(R.id.row3); 
				row.setText(Float.toString(z));
				
				//TextView row = (TextView) findViewById(R.id.row1); 
				//row.setText(Float.toString(deltaX));
				//row = (TextView) findViewById(R.id.row2); 
				//row.setText(Float.toString(deltaY));
				//row = (TextView) findViewById(R.id.row3); 
				//row.setText(Float.toString(deltaZ));
			}
		}
		if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			
			if (!mInitialized) {
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				mInitialized = true;
			}
			else {
				float deltaX = mLastX - x;
				float deltaY = mLastY - y;
				float deltaZ = mLastZ - z;
				if (Math.abs(deltaX) < NOISE) deltaX = (float)0.0;
				if (Math.abs(deltaY) < NOISE) deltaY = (float)0.0;
				if (Math.abs(deltaZ) < NOISE) deltaZ = (float)0.0;
				if (Math.abs(x) < NOISE) x = (float)0.0;
				if (Math.abs(y) < NOISE) y = (float)0.0;
				if (Math.abs(z) < NOISE) z = (float)0.0;
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				
				TextView row = (TextView) findViewById(R.id.row4); 
				row.setText(Float.toString(x));
				row = (TextView) findViewById(R.id.row5); 
				row.setText(Float.toString(y));
				row = (TextView) findViewById(R.id.row6); 
				row.setText(Float.toString(z));
				
				//TextView row = (TextView) findViewById(R.id.row4); 
				//row.setText(Float.toString(deltaX));
				//row = (TextView) findViewById(R.id.row5); 
				//row.setText(Float.toString(deltaY));
				//row = (TextView) findViewById(R.id.row6); 
				//row.setText(Float.toString(deltaZ));
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager2.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

}
