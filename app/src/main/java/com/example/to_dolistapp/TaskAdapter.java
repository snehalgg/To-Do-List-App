package com.example.to_dolistapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private MainActivity mainActivity;
    private TaskDBHelper dbHelper;

    public TaskAdapter(List<Task> taskList, MainActivity mainActivity, TaskDBHelper dbHelper) {
        this.taskList = taskList;
        this.mainActivity = mainActivity;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.textViewTaskName.setText(task.getTaskName());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTaskName;
        Button buttonEdit;
        Button buttonDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskName = itemView.findViewById(R.id.textViewTaskName);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Task task = taskList.get(getAdapterPosition());
                    showEditDialog(task);
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Task task = taskList.get(getAdapterPosition());
                    mainActivity.deleteTaskFromDatabase(task.getId());
                    taskList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });
        }

        private void showEditDialog(final Task task) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(itemView.getContext());
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
            View dialogView = inflater.inflate(R.layout.edit_dialog, null);
            dialogBuilder.setView(dialogView);

            final EditText editTextTaskName = dialogView.findViewById(R.id.editTextEditTaskName);
            editTextTaskName.setText(task.getTaskName());

            dialogBuilder.setTitle("Edit Task");
            dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String newTaskName = editTextTaskName.getText().toString().trim();
                    task.setTaskName(newTaskName);

                    // Update the task in the database
                    mainActivity.updateTaskInDatabase(task);

                    // Notify the adapter of the change
                    notifyItemChanged(getAdapterPosition());
                }
            });

            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing, just close the dialog
                }
            });

            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
    }
}
