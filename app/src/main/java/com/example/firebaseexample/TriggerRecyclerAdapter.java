package com.example.firebaseexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class TriggerRecyclerAdapter extends FirestoreRecyclerAdapter<Trigger, TriggerRecyclerAdapter.TriggerViewHolder> {

    //TriggerListener triggerListener;

    public TriggerRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Trigger> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TriggerViewHolder holder, int position, @NonNull Trigger triggerModel) {
        String numTimes = String.valueOf(triggerModel.getNumTimes());
        holder.triggerText.setText(triggerModel.getTrigger());
        holder.triggerCount.setText(numTimes);
    }

    @NonNull
    @Override
    public TriggerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.trigger_item, parent, false);
        return new TriggerViewHolder(view);
    }

    class TriggerViewHolder extends RecyclerView.ViewHolder{
        TextView triggerText, triggerCount;
        public TriggerViewHolder(@NonNull View itemView) {
            super(itemView);
            triggerText = itemView.findViewById(R.id.triggerTextView);
            triggerCount = itemView.findViewById(R.id.countTextView);
        }
    }
}
