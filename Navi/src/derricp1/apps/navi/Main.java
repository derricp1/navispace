package derricp1.apps.navi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.*;
import java.util.Locale;

public class Main extends Activity {
	
	public final static String EXTRA_MESSAGE = "derricp1.apps.MESSAGE"; //Message to display to user
	public final static String ZOOM_LEVEL = "derricp1.apps.MESSAGE";
	public int rangesize = 455; //number of ranges (node sets) - Needs to be changed to strings due to call numbers
	public String[] lo; //ranges
	public String[] hi; //ranges
	
	public int[] lonum;
	public int[] hinum;
	
	public boolean zlevel = false;
	
	//May need to change into having 3 arrays, one with node translations to a range, in addition to hi and lo
	
	public boolean loaded = false;
	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {

		/*
		
	    Intent intent = new Intent(this, LoadActivity.class); //Makes an intent to pass the user's node
	    EditText editText = (EditText) findViewById(R.id.input_message);
	    //String message = editText.getText().toString();
	    String isbn = editText.getText().toString();
	    int target = 0;
	    CharSequence cs = editText.getText();
	    if (cs != null && cs.length() > 0) {
	    	
	    	if (loaded == false) {
	    		
				//Load range file
	    		InputStream is = getResources().openRawResource(R.raw.range);				
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				
				try {
					String str = br.readLine(); //Reads the range size
					rangesize = Integer.parseInt(str);
					str = br.readLine(); //Discards hash
					lo = new String[rangesize];
					hi = new String[rangesize];
				} 
				catch (IOException e) {
					finish(); //should not reach here
				}

				//sets pairs of numbers to range
				try {
					for (int i=0; i< rangesize; i++) {
						String str = br.readLine(); //Reads in data
						lo[i] = str;
						str = br.readLine(); //Reads in data
						hi[i] = str;
						
						str = br.readLine(); //Discards hash

					}
				} 
				catch (IOException e) {
					finish(); //should not reach here either
				}
	    	}
	    	
	    	int lowcomp = 0;
	    	int hicomp = 0;

	    	//Compare ranges to ISBN
	    	//If in range, carry on
	    	boolean success = false;
			for (int i=0; i<rangesize; i++) { //needs to be fixed, doesn't handle numbers and such correctly
				lowcomp = isbn.compareTo(lo[i]);
				hicomp = isbn.compareTo(hi[i]);
				if (lowcomp >= 0 && hicomp <= 0) {
					success = true;
					target = i;
				}
				
			}
			
			*/
		
	    Intent intent = new Intent(this, LoadActivity.class); //Makes an intent to pass the user's node
	    EditText editText = (EditText) findViewById(R.id.input_message);
	    //String message = editText.getText().toString();
	    String isbn = editText.getText().toString();
	    
	    String isbnchars = ""; //splitting of the isbn
	    int isbnnums = -1;
	    
	    int breakpoint = 0;
	    char[] chararray = isbn.toCharArray();
	    for (int q=0; q<isbn.length(); q++) {
	    	if (isbnchars == "" && Character.isDigit(chararray[q]) == true) {
	    		isbnchars = isbn.substring(0,q);
	    		breakpoint = q;
	    	}
	    	if (isbnchars != "" && isbnnums == -1) {
	    		if (q == isbn.length() - 1 && isbnnums == -1) { //last character
	    			String tempstr = (isbn.substring(breakpoint,q+1));
		    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
		    			isbnnums = Integer.parseInt(tempstr);
	    		}
	    		if (Character.isDigit(chararray[q]) == false && isbnnums == -1) { //not a number anymore
		    		String tempstr = (isbn.substring(breakpoint,q));
		    		if (tempstr.length() > 0)
		    			isbnnums = Integer.parseInt(tempstr);	    			
	    		}
	    	}
	    }
	    
	    
	    int target = 0;
	    CharSequence cs = editText.getText();
	    if (cs != null && cs.length() > 0) {
	    	
	    	if (loaded == false) {
	    		
				//Load range file
	    		InputStream is = getResources().openRawResource(R.raw.range);				
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				
				try {
					String str = br.readLine(); //Reads the range size
					rangesize = Integer.parseInt(str);
					str = br.readLine(); //Discards hash
					lo = new String[rangesize];
					hi = new String[rangesize];
					lonum = new int[rangesize];
					hinum = new int[rangesize];
				} 
				catch (IOException e) {
					finish(); //should not reach here
				}

				//sets pairs of numbers to range
				try {
					for (int i=0; i< rangesize; i++) {
						
						String str = br.readLine(); //Reads in data
						
					    String ics = ""; //splitting of the isbn
					    int ins = -1;
					    breakpoint = 0;
					    chararray = str.toCharArray();
					    
					    for (int q=0; q<str.length(); q++) {
					    	if (ics == "" && Character.isDigit(chararray[q]) == true) {
					    		ics = str.substring(0,q);
					    		breakpoint = q;
					    	}
					    	if (ics != "" && ins == -1) {
					    		if (q == str.length() - 1 && ins == -1) { //last character
					    			String tempstr = (str.substring(breakpoint,q+1));
						    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
						    			ins = Integer.parseInt(tempstr);
					    		}
					    		if (Character.isDigit(chararray[q]) == false && ins == -1) { //not a number anymore
						    		String tempstr = (str.substring(breakpoint,q));
						    		if (tempstr.length() > 0)
						    			ins = Integer.parseInt(tempstr);	    			
					    		}
					    	}
					    }
					    
					    lo[i] = ics;
					    lonum[i] = ins;

						str = br.readLine(); //Reads in data
						
					    ics = ""; //splitting of the isbn
					    ins = -1;
					    breakpoint = 0;
					    chararray = str.toCharArray();
					    
					    for (int q=0; q<str.length(); q++) {
					    	if (ics == "" && Character.isDigit(chararray[q]) == true) {
					    		ics = str.substring(0,q);
					    		breakpoint = q;
					    	}
					    	if (ics != "" && ins == -1) {
					    		if (q == str.length() - 1 && ins == -1) { //last character
					    			String tempstr = (str.substring(breakpoint,q+1));
						    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
						    			ins = Integer.parseInt(tempstr);
					    		}
					    		if (Character.isDigit(chararray[q]) == false && ins == -1) { //not a number anymore
						    		String tempstr = (str.substring(breakpoint,q));
						    		if (tempstr.length() > 0)
						    			ins = Integer.parseInt(tempstr);	    			
					    		}
					    	}
					    }
					    
					    hi[i] = ics;
					    hinum[i] = ins;
						
						str = br.readLine(); //Discards hash

					}
				} 
				catch (IOException e) {
					finish(); //should not reach here either
				}
	    	}
	    	
	    	int lowcomp = 0;
	    	int hicomp = 0;

	    	//Compare ranges to ISBN
	    	//If in range, carry on
	    	boolean success = false;
			for (int i=0; i<rangesize; i++) { //needs to be fixed, doesn't handle numbers and such correctly
				lowcomp = isbnchars.compareToIgnoreCase(lo[i]);
				hicomp = isbnchars.compareToIgnoreCase(hi[i]);
				if ((lowcomp >= 0 && hicomp <= 0)) {
					if ((isbnnums >= lonum[i] && isbnnums <= hinum[i]) || lonum[i]+hinum[i] == -2){
						success = true;
						target = i;
					}
				}
				
			}
	    	
	    	if (success == true) { //Will fail if user enters an invalid ISBN
			    intent.putExtra(EXTRA_MESSAGE, target); //Places the id number of the node of the shelf where the ISBN would be
			    intent.putExtra(ZOOM_LEVEL, zlevel);
			    startActivity(intent);
	    	}
	    	else {
	    		Toast.makeText(this, "Invalid ISBN", Toast.LENGTH_SHORT).show();
	    	}
	    
	    }
	    else {
	    	Toast.makeText(this, "Please Enter an ISBN", Toast.LENGTH_SHORT).show();
	    }
	}
	
    public void zup(View v) {
    	ToggleButton but = (ToggleButton) findViewById(R.id.toggleButton1);
    	if (but.isChecked())
    		zlevel = true;
    	else
    		zlevel = false;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

