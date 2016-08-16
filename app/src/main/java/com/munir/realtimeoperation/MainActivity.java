package com.munir.realtimeoperation;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static class AddressViewHolder extends RecyclerView.ViewHolder{
        public TextView textName;
        public TextView textAddress;
        public TextView textUrl;
        View mView;

        public AddressViewHolder(View view){
            super(view);
            mView = view;
            textName = (TextView)view.findViewById(R.id.textName);
            textAddress = (TextView)view.findViewById(R.id.textAddress);
            textUrl = (TextView)view.findViewById(R.id.textURL);

        }

    }
    public static final String ADRESS = "addresses";
    private static final String TAG = "MainActivity";
    public int pos = 0;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private DatabaseReference mDatabaseReference;
    private FirebaseRecyclerAdapter<AddressBook,AddressViewHolder> mFirebaseAdapter;
    private ProgressBar progressBar;
    private int itemCount = 0;
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        //mLinearLayoutManager.setStackFromEnd(true);
        // mRecyclerView.setLayoutManager(mLinearLayoutManager);
        new Wait().execute();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<AddressBook, AddressViewHolder>(
                AddressBook.class,
                R.layout.address_item,
                AddressViewHolder.class,
                mDatabaseReference.child(ADRESS)
        ) {
            @Override
            protected void populateViewHolder(final AddressViewHolder viewHolder, final AddressBook model, int position) {
                viewHolder.textName.setText(model.getName());
                viewHolder.textAddress.setText(model.getAddress());
                viewHolder.textUrl.setText(model.getUrl());
                pos = position;



               /* viewHolder.mView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Log.w(TAG, "You clicked on " + pos);
                    }
                });*/
                mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener(){
                    @Override
                    public void onClick(View view, int position) {
                       // Toast.makeText(getApplicationContext(),mFirebaseAdapter.getItem(position).getName() + " is selected!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,AddressDetails.class);
                        intent.putExtra("Position", position);
                        startActivity(intent);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
    private class Wait extends AsyncTask<Void, Void,Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            itemCount = mFirebaseAdapter.getItemCount();
            return itemCount;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer > 0){
                progressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
