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
	
	public boolean zlevel = false;
	
	//May need to change into having 3 arrays, one with node translations to a range, in addition to hi and lo
	
	public boolean loaded = false;
	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {

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

	    	//Compare ranges to ISBN
	    	//If in range, carry on
	    	boolean success = false;
			for (int i=0; i< rangesize; i++) {
				if (isbn.compareTo(lo[i].toUpperCase(Locale.getDefault())) >= 0 && isbn.compareTo(hi[i].toUpperCase(Locale.getDefault())) <= 0) { //Needs to be changed to call numbers and compareTo for strings (of course)
					success = true;
					target = i;
				}
				
			}	    	
	    	
	    	if (success) { //Will fail if user enters an invalid ISBN
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

