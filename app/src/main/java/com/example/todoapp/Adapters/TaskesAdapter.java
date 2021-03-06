package com.example.todoapp.Adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.AsyncTask.UpdateAsyncTask;
import com.example.todoapp.Models.TaskesModel;
import com.example.todoapp.R;
import com.example.todoapp.ReminderReceiver;
import com.example.todoapp.RoomDB.RoomFactory;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class TaskesAdapter extends RecyclerView.Adapter<TaskesAdapter.AllTaskesViewHolder> {

    List<TaskesModel> taskesList;
    OnItemClick itemClick;
    OnViewClick viewClicked;
    onBoxClick boxClicked;
    Context context;
    AlarmManager alarmManager;
    PendingIntent alarmIntent;
    Calendar calendar;

    public TaskesAdapter(List<TaskesModel> taskesList, OnItemClick itemClick, OnViewClick viewClicked, onBoxClick boxClicked, Context context) {
        this.taskesList = taskesList;
        this.itemClick = itemClick;
        this.viewClicked = viewClicked;
        this.boxClicked = boxClicked;
        this.context = context;
    }

    //=================================================================
    @NonNull
    @Override
    public AllTaskesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taskes_item_rv, parent, false);
        return new AllTaskesViewHolder(view);
    }

    //=================================================================
    @Override
    public void onBindViewHolder(@NonNull AllTaskesViewHolder holder, int position) {
        TaskesModel model = taskesList.get(position);
        holder.details.setText(model.getDetails());
        holder.date.setText(model.getDate());
        holder.done.setChecked(model.isCkecked());
        holder.remender.setChecked(model.isRemender());

        if (model.getCrossOut() == 1) {
            holder.details.setPaintFlags(holder.details.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            new UpdateAsyncTask(RoomFactory.getTaskessDb(context).getTaskesDao()).execute(model);
        } else {
            holder.details.setPaintFlags(holder.details.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            new UpdateAsyncTask(RoomFactory.getTaskessDb(context).getTaskesDao()).execute(model);
        }

        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.done.isChecked()) {
                    model.setCkecked(true);
                    holder.done.setChecked(true);
                    holder.details.setPaintFlags(holder.details.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    model.setCrossOut(1);
                    new UpdateAsyncTask(RoomFactory.getTaskessDb(context).getTaskesDao()).execute(model);
                } else {
                    model.setCkecked(false);
                    holder.done.setChecked(false);
                    holder.details.setPaintFlags(holder.details.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    model.setCrossOut(0);
                    new UpdateAsyncTask(RoomFactory.getTaskessDb(context).getTaskesDao()).execute(model);
                }

            }

        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.onItemClicked(v, position);
            }
        });
        holder.remender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.remender.isChecked()) {
                    model.setRemender(true);
                    holder.remender.setChecked(true);
                    new UpdateAsyncTask(RoomFactory.getTaskessDb(context).getTaskesDao()).execute(model);
                    showTimePickerDialog(holder);

                } else {
                    model.setRemender(false);
                    holder.remender.setChecked(false);
                    new UpdateAsyncTask(RoomFactory.getTaskessDb(context).getTaskesDao()).execute(model);
                }
            }
        });
    }

    //=====================================================================
    @Override
    public int getItemCount() {
        return taskesList.size();
    }

    //======================================================================
    private void showTimePickerDialog(AllTaskesViewHolder holder) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // timePickerDialog=new TimePickerDialog(context, ,c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                calendar = Calendar.getInstance();
                calendar.set(0, 0, 0, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
                String reminderTime = simpleDateFormat.format(calendar.getTime());
                //int myTime =Integer.parseInt(reminderTime);
                Intent alertIntent = new Intent(context, ReminderReceiver.class);
                alertIntent.putExtra("spicificTime", reminderTime);
                String task = holder.details.getText().toString();
                alertIntent.putExtra("Taskmsg", task);
                PendingIntent pintent = PendingIntent.getBroadcast(context, 0, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * 1, pintent);

            }
        }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), false);

        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                holder.remender.setChecked(false);
            }
        });

        timePickerDialog.show();
    }

    //=======================================================================
    private void crossOutTheTaske(@NonNull AllTaskesViewHolder holder) {
        if (!holder.details.getPaint().isStrikeThruText()) {
            holder.details.setPaintFlags(holder.details.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.details.setPaintFlags(holder.details.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    //=========================================================================
    public interface OnItemClick {
        void onItemClicked(View view, int position);
    }

    public interface OnViewClick {
        void onSwitchClick(View view, int position);
    }

    public interface onBoxClick {
        void onCheackBokClicked(View view, int position);
    }

    //===========================*ViewHolder*=======================================
    class AllTaskesViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView details;
        TextView date;
        MaterialCheckBox done;
        SwitchMaterial remender;


        public AllTaskesViewHolder(@NonNull View itemView) {
            super(itemView);
            // title = itemView.findViewById(R.id.title_tv);
            details = itemView.findViewById(R.id.task_tv);
            date = itemView.findViewById(R.id.date_tv);
            done = itemView.findViewById(R.id.done_cb);
            remender = itemView.findViewById(R.id.remender_sw);

        }
    }
}
