package com.example.firebaseexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, TriggerRecyclerAdapter.TriggerListener {

    private RecyclerView recyclerView;
    private TriggerRecyclerAdapter triggerRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();
        setRecyclerView();
        setFAB();
    }

    //Set up the view objects
    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Triggers");
        setSupportActionBar(toolbar);
    }

    private void setRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewContent);
    }

    private void setFAB() {
        FloatingActionButton addEntry = findViewById(R.id.fab);
        @SuppressLint("UseCompatLoadingForDrawables") Drawable whiteFAB = getResources().getDrawable(android.R.drawable.ic_input_add).getConstantState().newDrawable();
        whiteFAB.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        addEntry.setImageDrawable(whiteFAB);

        addEntry.setOnClickListener(view -> {
            showAlertDialog();
        });
    }

    private void showAlertDialog() {

        EditText addTriggerText = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Add trigger")
                .setView(addTriggerText)
                .setPositiveButton("Add", (dialog, which) ->
                        addTrigger(addTriggerText.getText().toString())).setNegativeButton("Cancel", null).show();
    }

    private void addTrigger(String input){
        //create trigger object from custom class with default value of 1
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Trigger trigger = new Trigger(input.toLowerCase().trim(), 1, userId);

        FirebaseFirestore.getInstance().collection("triggers").add(trigger).addOnSuccessListener(documentReference ->
                Log.d("ADDING TRIGGER...", "SUCCESS ADDING TRIGGER: " + trigger.getTrigger()))
                .addOnFailureListener(e ->
                Log.e("ADDING TRIGGER...", "FAILURE ADDING TRIGGER: " + trigger.getTrigger() + "... ERROR: " + e.getLocalizedMessage()));
    }
    //override toolbar settings
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            //change authentication state
            AuthUI.getInstance().signOut(this);
            startActivity(new Intent(this, RegisterLoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //keep user signed in by attatching authstatelistener to the lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        //listen for auth state changed
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        if(triggerRecyclerAdapter != null){
            triggerRecyclerAdapter.stopListening();
        }
    }

    //monitor if the json web token expires
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, RegisterLoginActivity.class));
            finish();
        }
        //recreate recyclerview when state changed
        createRecyclerView(firebaseAuth.getCurrentUser());
    }

    private void createRecyclerView(FirebaseUser user){
        Query query = FirebaseFirestore.getInstance()
                .collection("triggers")
                .whereEqualTo("userId", user.getUid());

        //build firestore recycler options
        FirestoreRecyclerOptions<Trigger> options = new FirestoreRecyclerOptions.Builder<Trigger>()
                .setQuery(query, Trigger.class)
                .build();
        triggerRecyclerAdapter = new TriggerRecyclerAdapter(options, this);
        recyclerView.setAdapter(triggerRecyclerAdapter);
        //listen for updates in realtime to add to recyclerview
        triggerRecyclerAdapter.startListening();
    }

    @Override
    public void clickPlusMinus(DocumentSnapshot snapshot, int incrementNum) {
        snapshot.getReference().update("numTimes", FieldValue.increment(incrementNum));
    }
}