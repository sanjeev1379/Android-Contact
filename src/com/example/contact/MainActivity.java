package com.example.contact;

//import java.net.URI;
import java.util.ArrayList;
import java.util.List;

//import com.example.contact.R.id;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private static final int EDIT = 0, DELETE = 1;
	
	EditText txtname , txtphone , txtemail , txtaddress;
	Button btnadd;
	TabHost tabHost;
	ImageView contactImageView;
	List<Contact> Contacts = new ArrayList<Contact>();
	ListView contactListView;
	Uri imageUri = Uri.parse("android.resourse://com.example.contact/drawable/add_image.png");
	DatabaseHandler dbHandler;
	int longClickedItemIndex;
	ArrayAdapter<Contact> contactAdapter;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        txtname = (EditText) findViewById(R.id.name);
        txtphone = (EditText) findViewById(R.id.phone);
        txtemail = (EditText) findViewById(R.id.email);
        txtaddress = (EditText) findViewById(R.id.address);
        contactImageView = (ImageView) findViewById(R.id.contactImage);
        //ImageView = (ImageView) findViewById(R.id.listImage);
        contactListView = (ListView) findViewById(R.id.listView1);
        
        dbHandler = new DatabaseHandler(getApplicationContext());
        
        registerForContextMenu(contactListView);
        
        contactListView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				longClickedItemIndex = position;
				return false;
			}
		});
        
        tabHost = (TabHost) findViewById(R.id.tabhost);
        
        
        tabHost.setup();
        
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Cteator");
        tabHost.addTab(tabSpec);
        
        tabSpec = tabHost.newTabSpec("contact");
        tabSpec.setContent(R.id.tabStoreContact);
        tabSpec.setIndicator("Contact");
        tabHost.addTab(tabSpec);
        
        btnadd = (Button) findViewById(R.id.btnAdd);
        
        btnadd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Contact contact = new Contact(dbHandler.getContactsCount(), String.valueOf(txtname.getText()), String.valueOf(txtphone.getText()), String.valueOf(txtemail.getText()), String.valueOf(txtaddress.getText()), imageUri );
				if(!contactExists(contact)){
				dbHandler.createContact(contact);
				Contacts.add(contact);
				contactAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), String.valueOf(txtname.getText()) + " has been added to your Contacts !", Toast.LENGTH_SHORT).show();
				return;
				}
				Toast.makeText(getApplicationContext(), String.valueOf(txtname.getText()) + " already exists Please use differenr name !", Toast.LENGTH_SHORT).show();
				
		    }
		});
        
        txtname.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				btnadd.setEnabled(String.valueOf(txtname.getText()).trim().length() > 1);
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				
				
			}
		});
        
        contactImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Contact Image"), 1);
				
			}
		});
        
       if(dbHandler.getContactsCount() != 0)
    	   Contacts.addAll(dbHandler.getAllContacts());
        
        	populateList();
    }
    
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo ){
    	super.onCreateContextMenu(menu, view, menuInfo);
    	
    	menu.setHeaderIcon(R.drawable.edit_icon1);
    	menu.setHeaderTitle("Contact Options");
    	menu.add(Menu.NONE, EDIT, menu.NONE, "Edit Contact");
    	menu.add(Menu.NONE, DELETE, menu.NONE, "Delete Contact");
    	
    }
    
    public boolean onContextItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case EDIT:
    		 Intent intent = new Intent(this, MainActivity.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
    		startActivity(intent);
    		break;
    		
    	case DELETE:
    		dbHandler.deleteContact(Contacts.get(longClickedItemIndex));
    		Contacts.remove(longClickedItemIndex);
    		contactAdapter.notifyDataSetChanged();
    		break;
    	}
		return super.onContextItemSelected(item);
    }
    
  	private boolean contactExists(Contact contact) {
    	String name = contact.getName();
    	int contactCount = Contacts.size();
    	
    	for(int i = 0; i < contactCount; i++){
    		if(name.compareToIgnoreCase(Contacts.get(i).getName()) == 0)
    			return true;
    	}
    	return false;
    }
    
    public void onActivityResult(int reqCode , int resCode , Intent data){
    	if(resCode == RESULT_OK){
    		if(reqCode == 1)
    			imageUri = (Uri) data.getData(); 
    			contactImageView.setImageURI(data.getData());
    	}
    }
    
    
    
    private void populateList(){
    	contactAdapter = new ContactListAdapter();
    	contactListView.setAdapter(contactAdapter);
    }
    
   

    private class ContactListAdapter extends ArrayAdapter<Contact>{
    	public ContactListAdapter(){
    		super (MainActivity.this , R.layout.listview_item, Contacts);
    	   	}
    	
    	@Override
    	public  View getView(int position , View view , ViewGroup parent){
			
    		if(view == null)
    			view = getLayoutInflater().inflate(R.layout.listview_item, parent , false);
    		
    		Contact currentContact = Contacts.get(position);
    		
    		TextView name = (TextView) view.findViewById(R.id.contactName);
    		name.setText(currentContact.getName());
    		TextView phone = (TextView) view.findViewById(R.id.contactPhone);
    		phone.setText(currentContact.getPhone());
    		TextView email = (TextView) view.findViewById(R.id.contactEmail);
    		email.setText(currentContact.getEmail());
    		TextView address = (TextView) view.findViewById(R.id.contactAddress);
    		address.setText(currentContact.getAddress());
    		ImageView listImageView = (ImageView) view.findViewById(R.id.listImage);
    		listImageView.setImageURI(currentContact.getImageURI());
    		
    		
    		
    		return view;
    		
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
