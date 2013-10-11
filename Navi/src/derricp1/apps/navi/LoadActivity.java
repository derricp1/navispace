package derricp1.apps.navi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

//import derricp1.test.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.media.*;

public class LoadActivity extends Activity {

	public final int LEVELS = 100; //Scaling factor for strength
	public final int SIGNALS = 4; //number of signals used for the comparison
	
	public final int FLOORWIDTH = 190; //Floor height/width in feet
	public final int FLOORHEIGHT = 124;
	
	//(0,0) is in top left corner
	
	public String[] ids; //ids of access points (rssids) we are using
	
	public int mapsize = 5; //How many nodes, read in from file (defaults now);
	public double[] nodex;
	public double[] nodey;
	public int[] nodess1;
	public int[] nodess2;
	public int[] nodess3;
	public int[] nodess4;
	
	public String[] rssids; //A copy of ids right now - when we cover multiple floor we'll need to expand this into every possible one we'd need to compare
	
	public double[] calcs; //calculated strength difference
	
    private ImageView imageView;
    private Paint myPaint = new Paint();
    private Paint BLUE = new Paint();
    private Paint GREEN = new Paint();

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Let's draw the map.
        setContentView(R.layout.activity_load);
        
        this.imageView = (ImageView)findViewById(R.id.scroller); //Read in the view that lets us scroll the map
        
        //sound
        int S1 = 1;
        int S2 = 2;
        final SoundPool pool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        final HashMap<Integer, Integer> soundsMap = new HashMap<Integer, Integer>();
        soundsMap.put(S1, pool.load(this, R.raw.left, 1));
        soundsMap.put(S2, pool.load(this, R.raw.right, 1));	 

        // Get the message from the intent
        Intent intent = getIntent();
        final int targetnode = intent.getIntExtra(Main.EXTRA_MESSAGE, 0); //Will never need the default or it would not even get here. 0 is a choice though
        
		final WifiManager myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		boolean wasEnabled = myWifiManager.isWifiEnabled(); //Enables WiFi
		
		if (!wasEnabled) {
			Toast.makeText(this, "Wifi Disabled. Please enable Wifi and try again.", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		ids = new String[SIGNALS]; //The RSSIDs we want
		rssids = new String[SIGNALS]; //Need to expand for more floors
		final int[] currsig = new int[SIGNALS];
		final int tests = 100;
		
		String[] s1 = new String[4];
		String[] s2 = new String[4];
		String[] s3 = new String[4];
		int[] ticker = new int[3];
		
		s1[0] = "d8:c7:c8:6e:7b:60"; //second floor
		s1[1] = "d8:c7:c8:ab:21:52";
		s1[2] = "d8:c7:c8:ab:2e:e8";
		s1[3] = "d8:c7:c8:6e:81:a8";
		s2[0] = "d8:c7:c8:ab:2f:88"; //third floor
		s2[1] = "d8:c7:c8:ab:29:f8";
		s2[2] = "d8:c7:c8:ab:26:d2";
		s2[3] = "d8:c7:c8:ab:29:80";
		s3[0] = "d8:c7:c8:ab:23:08"; //fourth floor
		s3[1] = "d8:c7:c8:ab:29:ea";
		s3[2] = "d8:c7:c8:ab:23:01";
		s3[3] = "d8:c7:c8:ab:2c:51";
		
		//check to determine floor
		if (myWifiManager.isWifiEnabled()){ //Recheck WiFi availability, safety measure
			if(myWifiManager.startScan()){ //Can we start?
				// List available APs
				List<ScanResult> scans = myWifiManager.getScanResults();
				if (scans != null && !scans.isEmpty()){
					for (ScanResult scan : scans) {
						
						for(int i=0; i<4; i++) {
							if (s1[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
								ticker[0] = ticker[0] + 1;
							}
							if (s2[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
								ticker[1] = ticker[1] + 1;
							}
							if (s3[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
								ticker[2] = ticker[2] + 1;
							}
						}
						
					}
				}
			}				
		}
		
		int bestmatch = 4;
		if (ticker[0] >= ticker[1] && ticker[0] >= ticker[2]) {
			bestmatch = 2;
		}
		if (ticker[1] >= ticker[0] && ticker[1] >= ticker[2]) {
			bestmatch = 3;
		}
		
		switch (bestmatch) {
		
		case 2:
			ids[0] = s1[0];
			ids[1] = s1[1];
			ids[2] = s1[2];
			ids[3] = s1[3];
			break;
		case 3:
			ids[0] = s2[0];
			ids[1] = s2[1];
			ids[2] = s2[2];
			ids[3] = s2[3];
			break;			
		default:
			ids[0] = s3[0];
			ids[1] = s3[1];
			ids[2] = s3[2];
			ids[3] = s3[3];
			break;
		
		}
		
		final int floor = bestmatch;
		
		final int thisscan[] = new int[SIGNALS];
		
		//failed floor match
		//0-233: floor 2
		//234-383: floor 3
		//384+: floor 4
		
		int targetfloor = 3;
		if (targetnode < 234) {
			targetfloor = 2;
		}
		if (targetnode > 383) {
			targetfloor = 4;
		}
		
		boolean checking = true;
		if (bestmatch != targetfloor && checking) {
			String failstring = "Book at floor " + targetfloor + " Restart this app there.";
			Toast.makeText(this, failstring, Toast.LENGTH_SHORT).show();
			finish();
		}
			
		//Begin loop here
		
		AsyncTask<Void, Bitmap, Void> math = new AsyncTask<Void, Bitmap, Void>(){

			@Override
			protected Void doInBackground(Void... arg0) {
				
				int lastnode = -1;
				int currnode = -1;
				int nextnode = -1;
				boolean success = false;
				
				int stickiness = 3;
				int sticknode = -1; //node that's trying to be moved to
				
				//Let's try to get the info for where we are!
				for(int i=0; i<SIGNALS; i++)
					currsig[i] = 0;	
				
				while (!success) {
					
					for(int i=0; i<SIGNALS; i++)
						thisscan[i] = -1;
			
					if (myWifiManager.isWifiEnabled()){ //Recheck WiFi availability, safety measure
						for(int z=0; z<tests; z++) {
							
							for(int i=0; i<SIGNALS; i++)
								thisscan[i] = -1;
							
							if(myWifiManager.startScan()){ //Can we start?
								// List available APs
								List<ScanResult> scans = myWifiManager.getScanResults();
								if (scans != null && !scans.isEmpty()){
									for (ScanResult scan : scans) {
										int found = -1;
										
										for(int i=0; i<SIGNALS; i++) {
											if (ids[i].compareTo(scan.BSSID) == 0) { //Check each target to see if match
												found = i;
											}
										}
										
										if (found > -1) {
											thisscan[found] = WifiManager.calculateSignalLevel(scan.level, LEVELS);
										}
										
									}
								}
							}
							
							for(int i=0; i<SIGNALS; i++)
								currsig[i] = currsig[i] + thisscan[i];
							
						}
					}

					for(int q=0; q<SIGNALS; q++) {
						currsig[q] = (int) Math.ceil(currsig[q]/tests);
					}	
					
			        //Read in absolute coordinates of nodes
					InputStream is = getResources().openRawResource(R.raw.nodelocs);
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					
					try {
						String str = br.readLine(); //Reads the number of nodes - should be the same as before
						mapsize = Integer.parseInt(str);
						nodex = new double[mapsize];
						nodey = new double[mapsize];
						nodess1 = new int[mapsize];
						nodess2 = new int[mapsize];
						nodess3 = new int[mapsize];
						nodess4 = new int[mapsize];
						
						//Sets up arrays
						
						str = br.readLine(); //Discards hash
					} 
					catch (IOException e) {
						finish(); //should not reach here
					}
					
					try { //Read in strength and location data
						for (int i=0; i< mapsize; i++) {
							String str = br.readLine(); //Reads in data
							nodex[i] = Double.parseDouble(str);
							str = br.readLine(); //Reads in data
							nodey[i] = Double.parseDouble(str);
							str = br.readLine(); //Reads in data
							nodess1[i] = Integer.parseInt(str);
							str = br.readLine(); //Reads in data
							nodess2[i] = Integer.parseInt(str);
							str = br.readLine(); //Reads in data
							nodess3[i] = Integer.parseInt(str);
							str = br.readLine(); //Reads in data
							nodess4[i] = Integer.parseInt(str);
							
							str = br.readLine(); //Discards hash
			
						}
					} 
					catch (IOException e) {
						finish(); //should not reach here either
					}
					
					//Let's derive a start node, now that all of the nodes have been read in.
			
			        int startnode = 0; //need to generate
			        double calcs[] = new double[mapsize];
			        
			        //Currsig[0-2] is our signal, nodess1-3 is the signal strength of each node 0 to n-1 
					for(int i=0; i<mapsize; i++) {
						calcs[i] = signalDistance(currsig[0], currsig[1], currsig[2], currsig[3], nodess1[i], nodess2[i], nodess3[i], nodess4[i]);
					}        
			        //Calculate closest distance (above)
					
					//get the last position, for voice
					lastnode = currnode;
					
					startnode = currnode;
					int closestnode = 0; //the candidate for switching
					
					for(int i=0; i<mapsize; i++) {
						if (calcs[i] < calcs[closestnode]) { //Pick nearest neighbor
							closestnode = i;
						}
					}
					
					if (sticknode == -1) {
						stickiness = 3;
						startnode = closestnode;
						sticknode = startnode;
					}
					else if (closestnode == sticknode) {
						stickiness--;
						if (stickiness == 0) {
							startnode = closestnode;
							stickiness = 3;
						}
					}
					else {
						stickiness = 3;
						sticknode = closestnode;
					}
					
					//set for voice
					currnode = startnode;
					
					//All nodes have been read
					//Lets generate neighbors from the next file
					InputStream is2 = getResources().openRawResource(R.raw.neighbors);
					InputStreamReader isr2 = new InputStreamReader(is2);
					BufferedReader br2 = new BufferedReader(isr2);	
					
					try {
						String str = br2.readLine(); //Reads the number of nodes - should be the same as before
						mapsize = Integer.parseInt(str);
						
						str = br2.readLine(); //Discards hash
					} 
					catch (IOException e) {
						finish(); //should not reach here
					}
					
					EdgeWeightedDigraph dg = new EdgeWeightedDigraph(mapsize);
					
					try { //Gets every set of neighbors for each node as in the neighbors file
						for (int i=0; i< mapsize; i++) {
							String str = br2.readLine(); //Reads in data
							while (str.compareTo("#########") != 0) {
								int j = Integer.parseInt(str);
								if (i != j) { //Double checking neighbor with self case - should not occur on well-made file
									double dist = Math.sqrt(((nodex[i]-nodex[j])*(nodex[i]-nodex[j]))+((nodey[i]-nodey[j])*(nodey[i]-nodey[j])));
									dist = Math.sqrt(dist);
									
									DirectedEdge e = new DirectedEdge(i, j, dist);
									dg.addEdge(e);
									
								}
								str = br2.readLine();
							}
						}
					} 
					catch (IOException e) {
						finish(); //should not reach here either
					}		
					
					//Create Map with this info
					DijkstraSP distances = new DijkstraSP(dg,startnode);
			        
					//Work with it
					int[] pathstarts;
					int[] pathends;
					int counting = 0;
					
			        //Unless the map is poorly constructed, this won't fail
			        for (DirectedEdge e : distances.pathTo(targetnode)) {
			            counting++;
			        }
			        
			        pathstarts = new int[counting];
			        pathends = new int[counting];
			        counting--;
			        int linecount = 0;
			    	
			        for (DirectedEdge e : distances.pathTo(targetnode)) { //Read in the start and end of each node in the shortest path to draw it
			            pathstarts[counting] = e.from();
			            pathends[counting] = e.to();               
			            linecount++;
			            counting--;
			        }
			
			        // The image is coming from resource folder but it could also 
			        // load from the Internet or whatever
			        
			        Drawable drawable;
			        
			        switch (floor) {
			        	case 2:
			        		drawable = getResources().getDrawable(R.drawable.f2);
			        		break;
			        	case 3:
			        		drawable = getResources().getDrawable(R.drawable.f3);
			        		break;
			        	default:
			        		drawable = getResources().getDrawable(R.drawable.f4);
			        		break; //this will be for 4, the only other choice
			        }
			        		
			        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			
			        //Determine scaling factor
			        int imageViewHeight = getWindowManager().getDefaultDisplay().getHeight();
			        float factor = ( (float) imageViewHeight / (float) bitmap.getHeight() );
			        int nh = (int)(bitmap.getHeight()*factor);
			        int nw = (int)(bitmap.getWidth()*factor);
			        
			        // Create a new bitmap with the scaling factor
			        Bitmap newbitmap = Bitmap.createScaledBitmap(bitmap, nw, nh, false);
			
			        //Create another bitmpa to draw on
			        Bitmap tempBitmap = Bitmap.createBitmap(nw, nh, Bitmap.Config.RGB_565);
			        Canvas tempCanvas = new Canvas(tempBitmap);
			        
			        //Turn the canvas back to a new bitmap
			        Bitmap linemap = Bitmap.createBitmap(nw, nh, Config.RGB_565);
			        tempCanvas.setBitmap(linemap);
			        //Works as we go
			        
			        myPaint.setColor(Color.BLACK);
			        BLUE.setColor(Color.BLUE);
			        GREEN.setColor(Color.GREEN);
			        
			        //Draw the image bitmap into the canvas
			        tempCanvas.drawBitmap(newbitmap, 0, 0, null);
			        
			        //Draw Lines
			        for (int i=0; i<linecount; i++) {
			        	float sx = scale((float)nodex[pathstarts[i]], FLOORWIDTH, nw);
			        	float sy = scale((float)nodey[pathstarts[i]], FLOORHEIGHT, nh);
			        	float ex = scale((float)nodex[pathends[i]], FLOORWIDTH, nw);
			        	float ey = scale((float)nodey[pathends[i]], FLOORHEIGHT, nh);   	
			        	tempCanvas.drawLine(sx, sy, ex, ey, myPaint);
			        	
			        	nextnode = pathends[0];
			        }
			        
			        tempCanvas.drawCircle(scale((float)nodex[pathstarts[linecount-1]], FLOORWIDTH, nw), scale((float)nodey[pathstarts[linecount-1]], FLOORHEIGHT, nh), 10, BLUE); //Draw Circle at start spot
			        tempCanvas.drawCircle(scale((float)nodex[pathends[0]], FLOORWIDTH, nw), scale((float)nodey[pathends[0]], FLOORHEIGHT, nh), 10, GREEN); //Draw Circle at end spot
			        
			        //try to play sound
			        boolean left = false;
			        boolean right = false;

			        
			        //nodex, nodey
			        if (currnode > -1 && nextnode > -1 && lastnode > -1) {
				        if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] < nodey[lastnode] && nodex[currnode] > nodex[nextnode]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[nextnode]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] < nodex[nextnode]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[nextnode])) {
				        	left = true;
				        }
				        if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] < nodey[lastnode] && nodex[currnode] < nodex[nextnode]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[nextnode]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] > nodex[nextnode]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[nextnode])) {
				        	right = true;
				        }
			        }
			        if (left) {
			            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            float volume = streamVolumeCurrent / streamVolumeMax;  

			            pool.play(soundsMap.get(1), volume, volume, 1, 0, 1);       	
			        }
			        if (right) {
			            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            float volume = streamVolumeCurrent / streamVolumeMax;  

			            pool.play(soundsMap.get(2), volume, volume, 1, 0, 1); 	        	
			        }
			        
			        try {
			            Thread.sleep(1000);
			        } 
			        catch (InterruptedException e) {
			        	e.printStackTrace();
			        }
			        
			        if (currnode == targetnode) {
			        	success = true;
			        }
			        
			        publishProgress(linemap);
			        
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Bitmap... result) {
				imageView.setImageBitmap(result[0]);
			}
			
			
		};
		
		Void v = null;
		math.execute(v);

        
    }   

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    

    public double signalDistance(int us0, int us1, int us2, int us3, int loc0, int loc1, int loc2, int loc3) {
    	
    	double result = Math.sqrt((Math.abs(us0-loc0)*Math.abs(us0-loc0)) + (Math.abs(us1-loc1)*Math.abs(us1-loc1)) + (Math.abs(us2-loc2)*Math.abs(us2-loc2)) + (Math.abs(us3-loc3)*Math.abs(us3-loc3)));
    	//Uses Euclidean distances as in the formula on the sheet (q=2)
    	return result;
    	
    }
    
    public float scale(float inloc, int inmax, int indim) {
    	
    	float scaleloc = (float)((inloc/inmax)*indim);
    	
    	return scaleloc;
    	
    }
    
}
