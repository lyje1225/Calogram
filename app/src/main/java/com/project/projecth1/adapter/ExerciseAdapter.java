//차상현 개발자, 이윤제 개발자
package com.project.projecth1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.projecth1.R;
import com.project.projecth1.entity.Exercise;
import com.project.projecth1.entity.ExerciseItem;
import com.project.projecth1.listener.OnItemClickListener;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private OnItemClickListener listener;
    private ArrayList<ExerciseItem> items;

    public ExerciseAdapter(OnItemClickListener listener, ArrayList<ExerciseItem> items) {
        this.listener = listener;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtSports.setText(this.items.get(position).exercise.getSports());    // 운동종목
        holder.txtExerciseTime.setText(Utils.formatComma(this.items.get(position).exercise.getExerciseTime()) + "분");   // 운동시간
        holder.txtCalorie.setText(Utils.formatComma(this.items.get(position).exercise.getCalorie()) + "kcal");  // 칼로리
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSports, txtExerciseTime, txtCalorie;

        public ViewHolder(View view) {
            super(view);

            this.txtSports = view.findViewById(R.id.txtSports);
            this.txtExerciseTime = view.findViewById(R.id.txtExerciseTime);
            this.txtCalorie = view.findViewById(R.id.txtCalorie);
        }
    }
}

