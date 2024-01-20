package com.bitohur.pintarbaca.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bitohur.pintarbaca.InputFormActivity;
import com.bitohur.pintarbaca.MainActivity;
import com.bitohur.pintarbaca.R;
import com.bitohur.pintarbaca.model.Model;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;


public class Adapter extends RecyclerView.Adapter<ViewHolder> {

    MainActivity listActivity;
    List<Model> modelList;
    Context context;

    public Adapter(MainActivity listActivity, List<Model> modelList) {
        this.listActivity = listActivity;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(listActivity);
                String[] options = {"Update", "Delete"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            String id = modelList.get(position).getId();
                            String title = modelList.get(position).getTitle();
                            String author = modelList.get(position).getAuthor();
                            String description = modelList.get(position).getDescription();
                            String rating = modelList.get(position).getRating();
                            String page = modelList.get(position).getPage();
                            String image = modelList.get(position).getImageUrl();
                            String location = modelList.get(position).getLocation();

                            Intent intent = new Intent(listActivity, InputFormActivity.class);
                            intent.putExtra("pId", id);
                            intent.putExtra("pTitle", title);
                            intent.putExtra("pAuthor", author);
                            intent.putExtra("pRating", rating);
                            intent.putExtra("pPage", page);
                            intent.putExtra("pImage", image);
                            intent.putExtra("pDescription", description);
                            intent.putExtra("pLocation", location);

                            listActivity.startActivity(intent);
                        }

                        if (i == 1) {
                            listActivity.deleteData(position);
                        }
                    }
                }).create().show();
            }
        });
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(modelList.get(position).getTitle());
        holder.tvDesc.setText(modelList.get(position).getDescription());
        holder.tvAuthor.setText(modelList.get(position).getAuthor());
        holder.tvPage.setText(modelList.get(position).getPage() + " Halaman");
        holder.tvRating.setText("Rating " + modelList.get(position).getRating());
        String imageUrl = modelList.get(position).getImageUrl();
        Glide.with(listActivity).load(imageUrl).into(holder.ivBook);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
