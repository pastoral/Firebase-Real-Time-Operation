package com.munir.realtimeoperation;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by munirul.hoque on 8/16/2016.
 */
public class AddEditAddress extends AppCompatActivity {
    Button bOK,bCancel;
    AddressBook addressBook;
    int position;
    EditText pName,pEmail,pUrl,pAddress;
    CoordinatorLayout cl;
    Intent intent;
    public Bundle bundle;
    public String key;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_edit_address);

        cl = (CoordinatorLayout) findViewById(R.id.cdlayout);
        pName = (EditText) findViewById(R.id.edit_text_address_detail_name);
        pEmail = (EditText) findViewById(R.id.edit_text_address_detail_email);
        pUrl = (EditText) findViewById(R.id.edit_text_address_detail_url);
        pAddress = (EditText) findViewById(R.id.edit_text_address_detail_address);

        bOK = (Button) findViewById(R.id.bOk);
        bCancel = (Button) findViewById(R.id.bCancel);
        intent = getIntent();
        bundle = intent.getExtras();
        position = bundle.getInt(MainActivity.INTENT_TYPE);
        key = bundle.getString("key");
        if( position!= -1){
            showAddress();
        }
    }

    public void addEditAddress(View view){
       /* HashMap<String, Object> result = new HashMap<>();
        HashMap<String, Object> resultUpdate = new HashMap<>();
        result.put("name" , pName.getText().toString());
        result.put("address" , pEmail.getText().toString());
        result.put("url" , pUrl.getText().toString());
        result.put("email", pEmail.getText().toString());
        intent.putExtra("result",result);
        setResult(Activity.RESULT_OK,intent);
        if(position==-1) {
            MainActivity.mDatabaseReference.push().setValue(result);
        }
        else{
            MainActivity.mDatabaseReference.child(bundle.getString("key").toString()).push().setValue(result);
        }*/

        AddressBook addressBook = new AddressBook(pName.getText().toString(),
                pAddress.getText().toString(),
                pUrl.getText().toString(),
                pEmail.getText().toString());
        Map<String,Object> postValues = addressBook.toMap();
        if(pName.getText().toString().length()>0 && pAddress.getText().toString().length() >0) {
            if (position == -1) {
                MainActivity.mDatabaseReference.push().setValue(addressBook);
            } else {
                //Map<String,Object> addressUpdate = new HashMap<>();

                //MainActivity.mDatabaseReference.child(bundle.getString("key").toString()).setValue(postValues);

                MainActivity.mDatabaseReference.child(key).updateChildren(postValues);
            }
            finish();
        }
        else{
            Snackbar.make(cl,"Name and Address can not be empty",Snackbar.LENGTH_SHORT).show();
        }
    }


    private void showAddress(){
        pName.setText(bundle.getString("name").toString());
        pEmail.setText(bundle.getString("email").toString());
        pUrl.setText(bundle.getString("url").toString());
        pAddress.setText(bundle.getString("address").toString());
    }

    public void cancel(View view){
        finish();
    }

}