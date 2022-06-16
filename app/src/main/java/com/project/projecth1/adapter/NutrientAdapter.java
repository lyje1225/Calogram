//장웅희 개발자, 곽민승 개발자
package com.project.projecth1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.projecth1.R;
import com.project.projecth1.entity.Nutrient;
import com.project.projecth1.listener.OnItemClickListener;

import java.util.ArrayList;

public class NutrientAdapter extends RecyclerView.Adapter<NutrientAdapter.ViewHolder> {
    private OnItemClickListener listener;
    private ArrayList<Nutrient> items;

    public NutrientAdapter(OnItemClickListener listener, ArrayList<Nutrient> items) {
        this.listener = listener;
        this.items = items;
    }

    /* 추가 */
    public void add(Nutrient data, int position) {
        position = position == -1 ? getItemCount()  : position;
        // 영양정보 추가
        this.items.add(position, data);
        // 추가된 영양정보를 리스트에 적용하기 위함
        notifyItemInserted(position);
    }

    /* 삭제 */
    public Nutrient remove(int position){
        Nutrient data = null;

        if (position < getItemCount()) {
            data = this.items.get(position);
            // 영양정보 삭제
            this.items.remove(position);
            // 삭제된 영양정보를 리스트에 적용하기 위함
            notifyItemRemoved(position);
        }

        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nutrient, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtName.setText(this.items.get(position).getName());    // 영양성분

        // 수치값
        long value = this.items.get(position).getValue();
        if (value < 1000) {
            // mg
            holder.txtValue.setText(value + "mg");
        } else {
            // g
            holder.txtValue.setText((value / 1000) + "g");
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView txtName, txtValue;

        public ViewHolder(View view) {
            super(view);

            this.txtName = view.findViewById(R.id.txtName);
            this.txtValue = view.findViewById(R.id.txtValue);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // 영양성분 선택
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(view, position);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            // 영양성분 삭제하기
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemLongClick(view, position);
            }

            // 다른데서는 처리할 필요없음 true
            return true;
        }
    }
}
