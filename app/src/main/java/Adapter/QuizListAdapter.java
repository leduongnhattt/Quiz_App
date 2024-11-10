package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone_app.databinding.QuizItemRecyclerRowBinding;

import java.util.List;

import Activity.QuizActivity;
import Domain.QuizModel;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.MyViewHolder> {

    private final List<QuizModel> quizModelList;
    private final Context context;

    public QuizListAdapter(List<QuizModel> quizModelList, Context context) {
        this.quizModelList = quizModelList;
        this.context = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final QuizItemRecyclerRowBinding binding;

        public MyViewHolder(QuizItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(QuizModel model, Context context) {

            binding.quizTitleText.setText(model.getTitle());
            binding.quizSubtitleText.setText(model.getSubtitle());
            binding.quizTimeText.setText(model.getTime() + " min");

            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(context, QuizActivity.class);
                QuizActivity.questionModelList = model.getQuestionList();
                QuizActivity.time = model.getTime();
                intent.putExtra("quizCategory", model.getCategory());
                intent.putExtra("quizId", model.getId());
                context.startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        QuizItemRecyclerRowBinding binding = QuizItemRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Gán dữ liệu cho từng item trong RecyclerView
        holder.bind(quizModelList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return quizModelList.size();
    }
}
