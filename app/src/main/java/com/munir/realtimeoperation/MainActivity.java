package com.munir.realtimeoperation;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.collection.LLRBNode;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static class AddressViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView textName;
        public TextView textAddress;
        public TextView textUrl;
        View mView;

        public AddressViewHolder(View view){
            super(view);
            view.setClickable(true);
            mView = view;
            textName = (TextView)view.findViewById(R.id.textName);
            textAddress = (TextView)view.findViewById(R.id.textAddress);
            textUrl = (TextView)view.findViewById(R.id.textURL);
            view.setOnCreateContextMenuListener(this); // Context Menu Listner

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            //menu.setHeaderTitle("Select Action");
            int temp = v.getId();
            menu.add(0,v.getId(),0,"Delete");
        }



    }
    public static final String ADRESS = "addresses";
    private static final String TAG = "MainActivity";
    public static String INTENT_TYPE ;
    public String Key;
    public int pos = 0;
    private Intent intent;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    public static FirebaseDatabase mDatabase;
    public static DatabaseReference mDatabaseReference;
    public FirebaseRecyclerAdapter<AddressBook,AddressViewHolder> mFirebaseAdapter;
    private ProgressBar progressBar;
    private ImageButton deleteButton;
    public CoordinatorLayout cl;
    public FloatingActionButton fab;
    static boolean calledAlready = false;
    private int itemCount = 0;
    static final int REQ = 1;
    SearchView searchView;
    private List<AddressBook> list ;
    public List<AddressBook> filteredList;
    public String searchable = null;
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        cl = (CoordinatorLayout)findViewById(R.id.coordinatorlayout);
        setSupportActionBar(toolbar);
       fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, AddEditAddress.class);
                intent.putExtra(INTENT_TYPE,-1);
                startActivity(intent);
            }
        });
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        list = new ArrayList<AddressBook>();
        filteredList = new ArrayList<AddressBook>();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!calledAlready)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }
        list.clear();
        filteredList.clear();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(ADRESS);
        mDatabaseReference.keepSynced(true);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<AddressBook, AddressViewHolder>(
                AddressBook.class,
                R.layout.address_item,
                AddressViewHolder.class,
                mDatabaseReference.orderByChild("name").startAt(searchable).endAt(searchable+"\uf8ff") // sorting items by name
        ) {
            @Override
            protected void populateViewHolder(final AddressViewHolder viewHolder, final AddressBook model, int position) {
                progressBar.setVisibility(ProgressBar.GONE);
                viewHolder.textName.setText(model.getName());
                viewHolder.textAddress.setText(model.getAddress());
                viewHolder.textUrl.setText(model.getUrl());
                pos = position;

                mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener(){
                    @Override
                    public void onClick(View view, int position) {
                        // Toast.makeText(getApplicationContext(),mFirebaseAdapter.getItem(position).getName() + " is selected!", Toast.LENGTH_SHORT).show();
                        Key = getRef(position).getKey();
                        pos = position;
                        intent = new Intent(MainActivity.this,AddEditAddress.class);
                        intent.putExtra("name",mFirebaseAdapter.getItem(position).getName());
                        intent.putExtra("email",mFirebaseAdapter.getItem(position).getEmail());
                        intent.putExtra("url",mFirebaseAdapter.getItem(position).getUrl());
                        intent.putExtra("address",mFirebaseAdapter.getItem(position).getAddress());
                        intent.putExtra("key",Key);
                        startActivity(intent);
                        // startActivityForResult(intent,REQ);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        Key = getRef(position).getKey();
                        pos = position;

                    }
                } ));

            }

        };



        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                itemCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 || (positionStart >= (itemCount -1) && lastVisiblePosition == (positionStart -1))){
                    mRecyclerView.scrollToPosition(positionStart);

                }
            }
        });
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mFirebaseAdapter);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ){
            if(resultCode == RESULT_OK){
                HashMap<String,Object> result = (HashMap<String,Object>)data.getSerializableExtra("result");
                mDatabaseReference.push().setValue(result);
            }
        }
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
       /* MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("well", " this worked");
                    searchable = newText.toString().toUpperCase().trim();
                    mFirebaseAdapter = new FirebaseRecyclerAdapter<AddressBook, AddressViewHolder>(
                            AddressBook.class,
                            R.layout.address_item,
                            AddressViewHolder.class,
                            mDatabaseReference.orderByChild("name").startAt(searchable).endAt(searchable+"\uf8ff")
                    ) {
                        @Override
                        protected void populateViewHolder(AddressViewHolder viewHolder, AddressBook model, int position) {
                            progressBar.setVisibility(ProgressBar.GONE);
                            viewHolder.textName.setText(model.getName());
                            viewHolder.textAddress.setText(model.getAddress());
                            viewHolder.textUrl.setText(model.getUrl());
                        }
                    };
                    mRecyclerView.setLayoutManager(mLinearLayoutManager);
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
                    mRecyclerView.setAdapter(mFirebaseAdapter);
                    return true;
                }
            }); */


        return true;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle() == "Delete"){
           Snackbar snackbar =  Snackbar.make(cl,"Are you sure to delete " + "  " + mFirebaseAdapter.getItem(pos).getName(),Snackbar.LENGTH_LONG)
                   .setAction("Delete" , new View.OnClickListener(){
                       @Override
                       public void onClick(View v) {
                          // Toast.makeText(MainActivity.this,"Message Delete", Toast.LENGTH_SHORT).show();
                           mDatabaseReference.child(Key).removeValue();
                           Snackbar snackbar1 = Snackbar.make(cl, mFirebaseAdapter.getItem(pos).getName() +  "  " +"is deleted!", Snackbar.LENGTH_SHORT);
                           snackbar1.show();
                       }

                   });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseAdapter.cleanup();
    }


}