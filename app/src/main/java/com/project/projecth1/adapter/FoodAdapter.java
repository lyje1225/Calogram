//장웅희 개발자
package com.project.projecth1.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.projecth1.R;
import com.project.projecth1.entity.ExerciseItem;
import com.project.projecth1.entity.FoodItem;
import com.project.projecth1.listener.OnItemClickListener;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private OnItemClickListener listener;
    private ArrayList<FoodItem> items;

    public FoodAdapter(OnItemClickListener listener, ArrayList<FoodItem> items) {
        this.listener = listener;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtMealKind.setText("[" + Utils.getMealKind(this.items.get(position).food.getMealKind()) + "]"); // 아침/점심/저녁
        holder.txtFoodName.setText(this.items.get(position).food.getFoodName());    // 음식명

        // 수량
        int count = this.items.get(position).food.getFoodCount();
        holder.txtFoodCount.setText(Utils.formatComma(count) + "개"); // 수량

        // 총 칼로리
        int totalCalorie = count * this.items.get(position).food.getCalorie();
        holder.txtTotalCalorie.setText(Utils.formatComma(totalCalorie) + "kcal");

        // 영양정보
        StringBuilder nutrients = new StringBuilder();
        for (int i=0; i<this.items.get(position).food.getNutrients().size(); i++) {
            String[] data = this.items.get(position).food.getNutrients().get(i).split("@");
            nutrients.append(data[0]).append(" ");

            // 수치값
            if (Utils.isNumeric(data[1])) {
                // 수량에 맞게 내용량 적용
                long value = Long.parseLong(data[1]) * count;
                if (value < 1000) {
                    // mg 으로 표시
                    nutrients.append(value).append("mg");
                } else {
                    // g 으로 표시
                    nutrients.append(value / 1000).append("g");
                }
            } else {
                nutrients.append("0g");
            }

            if (i < this.items.get(position).food.getNutrients().size()-1) {
                nutrients.append(", ");
            }
        }
        holder.txtNutrients.setText(nutrients.toString());
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMealKind, txtFoodName, txtFoodCount, txtTotalCalorie, txtNutrients;

        public ViewHolder(View view) {
            super(view);

            this.txtMealKind = view.findViewById(R.id.txtMealKind);
            this.txtFoodName = view.findViewById(R.id.txtFoodName);
            this.txtFoodCount = view.findViewById(R.id.txtFoodCount);
            this.txtTotalCalorie = view.findViewById(R.id.txtTotalCalorie);
            this.txtNutrients = view.findViewById(R.id.txtNutrients);
        }
    }
}