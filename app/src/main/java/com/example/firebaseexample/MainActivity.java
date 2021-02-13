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
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, TriggerRecyclerAdapter.TriggerListener {

    private RecyclerView recyclerView;
    private TriggerRecyclerAdapter triggerRecyclerAdapter;
    private PieChart pieChart;
    private final String failTAG = "FAILED OPERATION ";
    private final String successTAG = "SUCCEEDED OPERATION ";
    private List<DocumentSnapshot> snapshotList;
    private List<DocumentSnapshot> documentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();
        setRecyclerView();
        setFAB();
        animateChart();
    }

    private void animateChart() {
        //add animation
        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    //Set up the view objects
    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Triggers");
        setSupportActionBar(toolbar);
    }

    private void setRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewContent);
        pieChart = findViewById(R.id.pieChart);
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
            signOut();
            return;
        }
        //recreate recyclerview when state changed
        createRecyclerView(firebaseAuth.getCurrentUser());
    }

    private void createRecyclerView(FirebaseUser user){
        Query query = FirebaseFirestore.getInstance()
                .collection("triggers")
                .whereEqualTo("userId", user.getUid())
                .orderBy("numTimes", Query.Direction.ASCENDING);

        //build firestore recycler options
        FirestoreRecyclerOptions<Trigger> options = new FirestoreRecyclerOptions.Builder<Trigger>()
                .setQuery(query, Trigger.class)
                .build();

        triggerRecyclerAdapter = new TriggerRecyclerAdapter(options, this);
        recyclerView.setAdapter(triggerRecyclerAdapter);
        //listen for updates in realtime to add to recyclerview
        triggerRecyclerAdapter.startListening();

        //create list of triggers for piechart
        query.addSnapshotListener((value, e) -> {
            if (e != null) {
                Log.w(failTAG, "Listen failed.", e);
                return;
            }
            List<Trigger> triggerList = value.toObjects(Trigger.class);
            setupPieChart();
            populatePieChart(triggerList);
        });
    }

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterTextColor(Color.DKGRAY);
        pieChart.setDrawRoundedSlices(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14);
        pieChart.setHoleRadius(40);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Trigger Insights");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

    }
    private void populatePieChart(List<Trigger> triggerList){


        ArrayList<PieEntry> entries = new ArrayList<>();
        for(Trigger trigger: triggerList){
            float floatValue = (float) trigger.getNumTimes();
            entries.add(new PieEntry(floatValue, trigger.getTrigger()));
        }
        //set colors
        ArrayList<Integer> colors = new ArrayList<>();

        for(int color: ColorTemplate.VORDIPLOM_COLORS){
            colors.add(color);
        }
        for(int color: ColorTemplate.JOYFUL_COLORS){
            colors.add(color);
        }
        for(int color: ColorTemplate.PASTEL_COLORS){
            colors.add(color);
        }

        //write arrays to piechart
        PieDataSet dataSet = new PieDataSet(entries,"Triggers");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    //handle the plus and minus clicks from the interface
    @Override
    public void clickPlusMinus(DocumentSnapshot snapshot, int incrementNum) {
        //create Trigger object with current snapshot
        Trigger trigger = snapshot.toObject(Trigger.class);

        //if we click minus on the trigger and the value is 1, delete it
        if(incrementNum == -1 && trigger.getNumTimes() == 1){
            deleteSnapshot(snapshot);
        }
        //else increment it
        else {
            incrementNumTimes(snapshot, incrementNum);
        }
    }

    //methods for changes to DB
    @Override
    public void clickEditItem(DocumentSnapshot clickedSnapshot) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();

        Trigger clickedTrigger = clickedSnapshot.toObject(Trigger.class);
        EditText editTextField = new EditText(this);

        editTextField.setText(clickedTrigger.getTrigger());
        new AlertDialog.Builder(this).setTitle("Edit Trigger").setView(editTextField)
                .setPositiveButton("Confirm", (dialog, i) -> {

                    String editedTrigger = editTextField.getText().toString().toLowerCase().trim();

                    //get all the documents by the user
                    FirebaseFirestore.getInstance().collection("triggers").whereEqualTo("userId", userID).get().addOnFailureListener(e -> {
                        Log.e(failTAG, "onFailure: fetching entries identical to " + clickedTrigger.getTrigger() +"... by ", e); })
                            .addOnSuccessListener(queryDocumentSnapshots -> {

                                snapshotList = queryDocumentSnapshots.getDocuments();
                                for(DocumentSnapshot loopSnapshot: snapshotList){

                                    Trigger loopTrigger = loopSnapshot.toObject(Trigger.class);
//
                                    //if the trigger we are changing the field to already exists...
                                    if(editedTrigger.equals(loopTrigger.getTrigger().toLowerCase().trim())){
                                        Log.d(successTAG, "clickEditItem: " + loopSnapshot.getData());
                                        //add the numTimes amount of the document with the same name to the existing trigger document
                                        incrementNumTimes(loopSnapshot, clickedTrigger.getNumTimes());
                                        //delete the docuemnt snapshot with the same name
                                        deleteSnapshot(clickedSnapshot);
                                        //break out of loop
                                        break;
                                    }
                                }
                                if(clickedSnapshot.getReference() != null){
                                    updateTrigger(clickedSnapshot, editedTrigger);
                                }
                            });
                }).setNegativeButton("Cancel", null).show();
    }

    private void addTrigger(String input) {
        //create trigger object from custom class with default value of 1
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //get all the documents by the user
        FirebaseFirestore.getInstance().collection("triggers").whereEqualTo("userId", userId).get().addOnFailureListener(e -> {
            Log.e(failTAG, "onFailure: fetching entries identical to " + input +"... by ", e); })
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot loopSnapshot : snapshotList) {

                        Trigger loopTrigger = loopSnapshot.toObject(Trigger.class);
                        String compareTrigger = loopTrigger.getTrigger().toLowerCase().trim();
                        //if the trigger we are changing the field to already exists...
                        if (input.toLowerCase().trim().equals(compareTrigger)) {
                            //add the numTimes amount of the document with the same name to the existing trigger document
                            incrementNumTimes(loopSnapshot, 1);
                            isDuplicate = true;
                            //break out of loop
                            break;
                        }
                    }
                    if (!isDuplicate){
                        Trigger trigger = new Trigger(input.toLowerCase().trim(), 1, userId);
                        FirebaseFirestore.getInstance().collection("triggers").add(trigger).addOnSuccessListener(documentReference ->
                                Log.d("ADDING TRIGGER...", "SUCCESS ADDING TRIGGER: " + trigger.getTrigger()))
                                .addOnFailureListener(e -> Log.e("ADDING TRIGGER...", "FAILURE ADDING TRIGGER: " + trigger.getTrigger() + "... ERROR: ", e));
                    }
                });
    }

    private void updateTrigger(DocumentSnapshot snapshotInput, String triggerChange){
        snapshotInput.getReference().update("trigger", triggerChange).addOnFailureListener(e -> {
            Log.e(failTAG, "updateSnapshotTrigger: " + snapshotInput.getData() + " with trigger " + triggerChange, e);
        }).addOnSuccessListener(aVoid -> {
            Log.d(successTAG, "onSuccess: updating document " + snapshotInput.getData() + "... by " + triggerChange);
        });
    }

    private void incrementNumTimes(DocumentSnapshot snapshotInput, int incrementValue){
        snapshotInput.getReference().update("numTimes", FieldValue.increment(incrementValue)).addOnFailureListener(e -> {
            Log.e(failTAG, "incrementSnapshotNumTimes: " + snapshotInput.getData() + " with value " + incrementValue, e);
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(successTAG, "onSuccess: incrementing document " + snapshotInput.getData() + "... by " + incrementValue);
            }
        });
    }

    private void deleteSnapshot(DocumentSnapshot snapshotInput){
        snapshotInput.getReference().delete().addOnFailureListener(e -> {
            Log.e(failTAG, "deleteSnapshot: " + snapshotInput.getData(), e);
        }).addOnSuccessListener(aVoid -> {
            Log.d(successTAG, "deleteSnapshot: " + snapshotInput.getData());
        });
    }

    //open the login activity and close this one when signing out
    private void signOut(){
        Intent intent = new Intent(this, RegisterLoginActivity.class);
        startActivity(intent);
        finish();
    }
}