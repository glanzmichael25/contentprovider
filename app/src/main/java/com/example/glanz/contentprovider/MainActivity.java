package com.example.glanz.contentprovider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView favoritesListView;
    private  final Uri DATA_URI = ContactsContract.Data.CONTENT_URI;

    //the columns
    private  final int NAME_COLUMN = 1;
    private  final int PHONE_NUMBER_COLUMN = 2;
    private  final int PHONE_TYPE_COLUMN = 3;

    private Cursor cursor;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get reference to the widget
        favoritesListView = (ListView) findViewById(R.id.favoritesListView);

        //set listener
        favoritesListView.setOnItemClickListener(this);
    }

    @Override
    public void  onResume(){
        super.onResume();

        //set up variables for the cursor
        String[] columns = {
                ContactsContract.Data._ID,//primary key unique to a row
                ContactsContract.Contacts.DISPLAY_NAME, //person's name
                ContactsContract.Data.DATA1, //phone number
                ContactsContract.Data.DATA2, //phone label
        };

        String where = ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND " +
                ContactsContract.Data.DATA2 + "=2 AND " + ContactsContract.Contacts.STARRED + "='1'";

        String orderBy = ContactsContract.Data.TIMES_CONTACTED + " DESC";

        //get the cursor
        cursor = getContentResolver().query(DATA_URI, columns, where, null, orderBy);

        //set up variables for the adapter
        int layoutID = R.layout.favorites;
        String[] fromColumns = {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.DATA2,
                ContactsContract.Data.DATA1
        };

        int[] toView = {
                R.id.nameTextView,
                R.id.labelTextView,
                R.id.numberTextView
        };

        //get adapter from cursor
        adapter = new SimpleCursorAdapter(this, layoutID, cursor, fromColumns, toView,0);

        //convert values from DATA2 column to readable values
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if(columnIndex == PHONE_TYPE_COLUMN){
                    int phoneType = cursor.getInt(columnIndex);
                    TextView tv = (TextView) view;
                    switch (phoneType){
                        case 1:
                            tv.setText("Home");
                            return true;
                        case 2:
                            tv.setText("Work");
                            return true;
                        case 3:
                            tv.setText("Mobile");
                            return  true;
                        default:
                            tv.setText("Other");
                            return true;
                    }
                }
                return  false;
            }
        });

        favoritesListView.setAdapter(adapter);
    }
    @SuppressLint("MissingPermission")
    private  void makeCall(CharSequence phoneNumber){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.setData(Uri.parse("tel: " + phoneNumber));
        startActivity(callIntent);
    }
    private  void  makeText(CharSequence phoneNumber){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public  void  onItemClick(AdapterView<?> adapter, View view, int position, long id){
        //get the name and phone number of the contact
        cursor.moveToPosition(position);
        final String name = cursor.getString(NAME_COLUMN);
        final CharSequence phonNumber = cursor.getString(PHONE_NUMBER_COLUMN);

        //display a dialog to call or text contact
        new AlertDialog.Builder(this).setMessage("Call or text " + name + " " + phonNumber + " ?")
                .setPositiveButton("Call", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        makeCall(phonNumber);
                    }
                })
                .setNeutralButton("Text", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        makeText(phonNumber);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
