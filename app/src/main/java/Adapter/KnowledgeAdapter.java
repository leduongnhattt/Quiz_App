package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Domain.KnowledgeModel;
import com.example.capstone_app.databinding.KnowledgeItemBinding; // Cần thay đổi theo đúng package của binding

public class KnowledgeAdapter extends RecyclerView.Adapter<KnowledgeAdapter.MyViewHolder> {

    private final List<KnowledgeModel> knowledgeList;
    private final Context context;

    public KnowledgeAdapter(List<KnowledgeModel> knowledgeList, Context context) {
        this.knowledgeList = knowledgeList;
        this.context = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final KnowledgeItemBinding binding;

        public MyViewHolder(KnowledgeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(KnowledgeModel model) {
            binding.knowledgeTitleText.setText(model.getTitle());
            binding.knowledgeContentText.setText(model.getContent());
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        KnowledgeItemBinding binding = KnowledgeItemBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Gán dữ liệu cho từng item trong RecyclerView
        holder.bind(knowledgeList.get(position));
    }

    @Override
    public int getItemCount() {
        return knowledgeList.size();
    }
}
