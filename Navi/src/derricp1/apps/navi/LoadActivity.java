package derricp1.apps.navi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.math.*;

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
	
	Context now = this;
	
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
        int S3 = 3;
        int S4 = 4;
        int S5 = 5;
        final SoundPool pool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        final HashMap<Integer, Integer> soundsMap = new HashMap<Integer, Integer>();
        soundsMap.put(S1, pool.load(this, R.raw.left, 1));
        soundsMap.put(S2, pool.load(this, R.raw.right, 1));
        soundsMap.put(S3, pool.load(this, R.raw.s3, 1));
        soundsMap.put(S4, pool.load(this, R.raw.s4, 1));
        soundsMap.put(S5, pool.load(this, R.raw.s5, 1));

        // Get the message from the intent
        Intent intent = getIntent();
        int target = intent.getIntExtra(Main.EXTRA_MESSAGE, 0); //Will never need the default or it would not even get here. 0 is a choice though
        final boolean ztog = intent.getBooleanExtra(Main.ZOOM_LEVEL, false);
        final boolean voice = intent.getBooleanExtra(Main.VOICES, false);
        final boolean words = intent.getBooleanExtra(Main.WORDS, false);
        
		WifiManager myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		boolean wasEnabled = myWifiManager.isWifiEnabled();
		
		if (wasEnabled == false) {
			Toast.makeText(this, "Wifi Disabled. Please enable Wifi and try again.", Toast.LENGTH_SHORT).show();
			finishAffinity();
		}
		
		ids = new String[SIGNALS]; //The RSSIDs we want
		rssids = new String[SIGNALS]; //Need to expand for more floors
		
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
		
		//0-233: floor 2
		//234-383: floor 3
		//384+: floor 4
		
		int targetfloor = 3;
		if (target < 234) {
			targetfloor = 2;
		}
		if (target > 383) {
			targetfloor = 4;
		}
		
		boolean vare = true;

		
		//part of here too
		if (ticker[0] + ticker[1] + ticker[2] == 0 || (bestmatch != targetfloor)) {
			String failstring = "Book at floor " + targetfloor + ".  Please retry there.";
			Toast.makeText(this, failstring, Toast.LENGTH_LONG).show();
			vare = false;
			finish();
		}
		
		final int finalmatch = bestmatch;
		final boolean isvalid = vare;
		final int targetnode = target;
			
		//Begin loop here
		
		AsyncTask<Void, Bitmap, Void> math = new AsyncTask<Void, Bitmap, Void>(){
			
			int messagetype = 0;

			@Override
			protected Void doInBackground(Void... arg0) {
				
				WifiManager myWifiManager2 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				boolean wasEnabled2 = myWifiManager2.isWifiEnabled();
				int lastnode = -1;
				int currnode = -1;
				int nextnode = -1;
				boolean success = false;
				boolean canspeak = true;
				
				int[] currsig = new int[SIGNALS];
				int tests = 100;
				
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
				fends[2] = 999;
				
				int countdown = 240*15;
				
				//Let's try to get the info for where we are!
				for(int i=0; i<SIGNALS; i++)
					currsig[i] = 0;	
				
				while (success == false && isvalid == true && countdown > 0) {
					
					
					//int test = 0;
					//while (test < 9999) { //this should be taken out once testing is done
						
						for(int i=0; i<SIGNALS; i++)
							thisscan[i] = -1;
				
						if (wasEnabled2 == true){ //Recheck WiFi availability, safety measure
							for(int z=0; z<tests; z++) {
								
								for(int i=0; i<SIGNALS; i++)
									thisscan[i] = -1;
								
								if(myWifiManager2.startScan()){ //Can we start?
									// List available APs
									List<ScanResult> scans = myWifiManager2.getScanResults();
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
						
						//test++; //take this out too
					
					//} //this is the end of the test block
					
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
						finishAffinity(); //should not reach here
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
						finishAffinity(); //should not reach here either
					}
					
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
						finishAffinity(); //should not reach here
					}
					
					ox = new double[obs];
					oy = new double[obs];
					or = new double[obs];
					
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
						finishAffinity(); //should not reach here either
					}					
					
					//Location determination starts here...

					//Let's derive a start node, now that all of the nodes have been read in.
			
			        int startnode = 0; //need to generate
			        double calcs[] = new double[mapsize];
			        
			        boolean newstep = false; //did we change positions?
			        
			        //Currsig[0-2] is our signal, nodess1-3 is the signal strength of each node 0 to n-1 
					for(int i=0; i<mapsize; i++) {
						if (i >= fstarts[floor-2] && i <= fends[floor-2]) {
							calcs[i] = signalDistance(currsig[0], currsig[1], currsig[2], currsig[3], nodess1[i], nodess2[i], nodess3[i], nodess4[i]);
							//scale by distance from last node - the further you are from the last node, the less chance you are there
							if (lastnode > -1) {
								calcs[i] = calcs[i]*Math.min(1,(Math.log10(realDistance(nodex[lastnode], nodey[lastnode], nodex[i], nodey[i])))); //additive heuristic
							}
						}
						else
							calcs[i] = -1; //don't consider if not on the floor.
						
					}        
			        //Calculate closest distance (above)
					
					//get the last position, for voice
					lastnode = currnode;
					
					startnode = currnode;
					int closestnode = 0; //the candidate for switching
					
					for(int i=0; i<mapsize; i++) {
						if (calcs[i] < calcs[closestnode] && calcs[i] > -1) { //Pick nearest neighbor
							closestnode = i;
						}
					}
					
					if (stucknode == -1) { //if first run, set sticking point, where one is
						stickiness = 3;
						startnode = closestnode;
						stucknode = closestnode;
						sticknode = -1;
						newstep = true;
					}
					else {
						if (sticknode == -1) { //shouldn't happen in normal use, but kept to catch errors and reset
							sticknode = closestnode;
							stickiness = 3;
						}
						else {
							if (closestnode == sticknode) { //If tests match, iterate towards updating location
								stickiness--;
								if (stickiness == 0) {
									startnode = closestnode;
									stucknode = closestnode;
									sticknode = -1;
									stickiness = 3;
									newstep = true;
									canspeak = true;
								}								
							}
							else { //reset
								sticknode = closestnode;
								stickiness = 3;
							}
						}
					}
					
					//set the current location
					startnode = stucknode;
					
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
						finishAffinity(); //should not reach here
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
						finishAffinity(); //should not reach here either
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
			        
			        double mult = 1;
			        //we could toggle but there's the leak
			        
			        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			
			        //Determine scaling factor
			        int imageViewHeight = getWindowManager().getDefaultDisplay().getHeight();
			        float factor = ( (float) imageViewHeight / (float) bitmap.getHeight() );
			        
			        int nh = (int)(bitmap.getHeight()*factor*mult);
			        int nw = (int)(bitmap.getWidth()*factor*mult);
			        
			        // Create a new bitmap with the scaling factor
			        //zoom the drawable if zoom option is on
			        Bitmap newbitmap = Bitmap.createScaledBitmap(bitmap, nw, nh, false);
			
			        //Create another bitmap to draw on
			        Bitmap tempBitmap = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_4444);
			        Canvas tempCanvas = new Canvas(tempBitmap);
			        
			        //Turn the canvas back to a new bitmap
			        Bitmap linemap = Bitmap.createBitmap(nw, nh, Bitmap.Config.ARGB_4444);
			        tempCanvas.setBitmap(linemap);
			        //Works as we go
			        
			        myPaint.setColor(Color.BLACK);
			        BLUE.setColor(Color.BLUE);
			        GREEN.setColor(Color.GREEN);
			        
			        myPaint.setStrokeWidth((float) (10*mult)); //make the lines bigger, will only affect the directional lines
			        
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
			        
			        //do again for crown
			        
			        Drawable drawable2 = getResources().getDrawable(R.drawable.crown);
			        Bitmap imgmap = ((BitmapDrawable)drawable2).getBitmap();
			        int imgh = (int)(imgmap.getHeight()*factor*mult);
			        int imgw = (int)(imgmap.getWidth()*factor*mult);
			        
			        Bitmap newimgmap = Bitmap.createScaledBitmap(imgmap, imgw, imgh, false);
			        
			        //Draw full map
			        tempCanvas.drawCircle(scale((float)nodex[pathstarts[linecount-1]], FLOORWIDTH, nw), scale((float)nodey[pathstarts[linecount-1]], FLOORHEIGHT, nh), 10, BLUE); //Draw Circle at start spot
			        tempCanvas.drawBitmap(newimgmap, (scale((float)nodex[pathends[0]], FLOORWIDTH, nw) - (imgw/2)), (scale((float)nodey[pathends[0]], FLOORHEIGHT, nh) - (imgh/2)), GREEN);
			        
			        //try to play sound
			        boolean left = false;
			        boolean right = false;
			        
			        messagetype = 0;
 
			        //nodex, nodey
			        if (currnode > -1 && nextnode > -1 && lastnode > -1 && newstep == true) {
				        if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] <= nodey[lastnode] && nodex[currnode] > nodex[nextnode]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[nextnode]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] < nodex[nextnode]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[nextnode])) {
				        	left = true;
				        }
				        if ((nodex[currnode] == nodex[lastnode] && nodey[currnode] <= nodey[lastnode] && nodex[currnode] < nodex[nextnode]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > nodey[nextnode]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] > nodex[nextnode]) || (nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] < nodey[nextnode])) {
				        	right = true;
				        }
			        }
			        if (left && canspeak) {
			            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            float volume = streamVolumeCurrent / streamVolumeMax; 
			            canspeak = false;

			            if (voice) {
			            	pool.play(soundsMap.get(1), volume, volume, 1, 0, 1); 
			            }
			            if (words) {
			            	messagetype = 1;
			            }
			            
			        }
			        if (right && canspeak) {
			            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			            float volume = streamVolumeCurrent / streamVolumeMax;
			            canspeak = false;

			            if (voice) {
			            	pool.play(soundsMap.get(2), volume, volume, 1, 0, 1); 
			            } 
			            if (words) {
			            	messagetype = 2;
			            }
			        }
			        
			        if (!left && !right && canspeak) { //if no turns, check for obstacles
			        	
			        	int ostart = 0;
			        	//oends[bestmatch-2]
			        	if (finalmatch > 2) {
			        		ostart = oends[finalmatch-3] + 1;
			        	}
			        	
			        	for (int i=0; i<obs; i++) {
			        		if (realDistance(nodex[currnode], nodey[currnode], ox[i], oy[i]) < or[i] && i >= ostart && i <= oends[finalmatch-2]) {
			        			//obstacle!
			        			//check left/right and stuff
						        if ((nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] <= oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] >= ox[i]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] < nodey[lastnode] && nodex[currnode] < ox[i])) {
						            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
						            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
						            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
						            float volume = streamVolumeCurrent / streamVolumeMax;
						            canspeak = false;

						            if (voice) {
						            	pool.play(soundsMap.get(3), volume, volume, 1, 0, 1); 
						            }
						            if (words) {
						            	messagetype = 1;
						            }
						        	break;
						        }
						        if ((nodex[currnode] > nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] > oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] > nodey[lastnode] && nodex[currnode] < ox[i]) || (nodex[currnode] < nodex[lastnode] && nodey[currnode] == nodey[lastnode] && nodey[currnode] <= oy[i]) || (nodex[currnode] == nodex[lastnode] && nodey[currnode] < nodey[lastnode] && nodex[currnode] >= ox[i])) {
						            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
						            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
						            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
						            float volume = streamVolumeCurrent / streamVolumeMax;
						            canspeak = false;

						            if (voice) {
						            	pool.play(soundsMap.get(4), volume, volume, 1, 0, 1); 
						            }
						            if (words) {
						            	messagetype = 2;
						            }
						        	break;
						        }	
			        		}
			        	}
			        }
			        
  
			        
			        try {
			            Thread.sleep(250); //quarter second steps
			        } 
			        catch (InterruptedException e) {
			        	e.printStackTrace();
			        }
			        
			        if (currnode == targetnode) {
			        	success = true;
			        }
			        
			        countdown--;
			        publishProgress(linemap);
			        
				}
				if (success == true) {
		            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		            float volume = streamVolumeCurrent / streamVolumeMax;
		            if (voice) {
		            	pool.play(soundsMap.get(5), volume, volume, 1, 0, 1); //needs to be 5
		            }				
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Bitmap... result) {
				imageView.setImageBitmap(result[0]);
				
				if (messagetype == 1) {
					Toast.makeText(now, "Turn Left.", Toast.LENGTH_SHORT).show();
				}
				if (messagetype == 2) {
					Toast.makeText(now, "Turn Right.", Toast.LENGTH_SHORT).show();
				}				
				
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
    
    public double realDistance(double cx, double cy, double ox, double oy) {
    	
    	return Math.sqrt((Math.abs(cx-ox)*Math.abs(cx-ox)) + (Math.abs(cy-oy)*Math.abs(cy-oy)));
    	
    }
    
}
