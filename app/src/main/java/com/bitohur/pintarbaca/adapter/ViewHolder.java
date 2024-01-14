package com.bitohur.pintarbaca.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bitohur.pintarbaca.R;

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView tvTitle, tvAuthor, tvDesc, tvPage, tvRating;
    ImageView ivBook;
    View view;
    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        view = itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
                return true;
            }
        });

        tvTitle = itemView.findViewById(R.id.tv_book_title);
        tvAuthor = itemView.findViewById(R.id.tv_author);
        tvDesc = itemView.findViewById(R.id.tv_description);
        tvPage = itemView.findViewById(R.id.tv_book_page);
        tvRating = itemView.findViewById(R.id.tv_book_rating);
        ivBook = itemView.findViewById(R.id.iv_image_book);
    }

    private ViewHolder.ClickListener mClickListener;

    public interface ClickListener {
        void onItemClick(View view, int position);

    }

    public void setOnClickListener(ViewHolder.ClickListener clickListener) {
        mClickListener = clickListener;
    }
}
