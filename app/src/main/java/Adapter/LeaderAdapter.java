package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.capstone_app.databinding.ViewholderLeadersBinding;

import Domain.UserModel;

public class LeaderAdapter extends RecyclerView.Adapter<LeaderAdapter.ViewHolder> {

    private ViewholderLeadersBinding binding;

    @NonNull
    @Override
    public LeaderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ViewholderLeadersBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderAdapter.ViewHolder holder, int position) {
        UserModel userModel = differ.getCurrentList().get(position);
        binding.titleTxt.setText(userModel.getName());

        // Tải ảnh từ URL
        Glide.with(binding.getRoot().getContext()).load(userModel.getPicture()).into(binding.pic);
        binding.rowTxt.setText(String.valueOf(position + 4));
        binding.scoreTxt.setText(String.valueOf(userModel.getScore()));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private final DiffUtil.ItemCallback<UserModel> differentCallback = new DiffUtil.ItemCallback<UserModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserModel oldItem, @NonNull UserModel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserModel oldItem, @NonNull UserModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    public final AsyncListDiffer<UserModel> differ = new AsyncListDiffer<>(this, differentCallback);
}
