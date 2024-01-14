package com.bitohur.pintarbaca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bitohur.pintarbaca.adapter.Adapter;
import com.bitohur.pintarbaca.model.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.github.muddz.styleabletoast.StyleableToast;

public class MainActivity extends AppCompatActivity {

    List<Model> modelList = new ArrayList<>();
    RecyclerView rv;
    RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db;
    Adapter adapter;

    Button btAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        rv = findViewById(R.id.rv_book);
        btAdd = findViewById(R.id.bt_add);

        rv.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);

        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InputFormActivity.class));
                finish();
            }
        });

        showData();

    }

    private void showData() {
        db.collection("Documents").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                modelList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    Model model = new Model(doc.getString("id"),
                            doc.getString("title"),
                            doc.getString("author"),
                            doc.getString("rating"),
                            doc.getString("description"),
                            doc.getString("page"),
                            doc.getString("imageUrl"),
                            doc.getString("location")
                            );
                    modelList.add(model);
                }
                adapter = new Adapter(MainActivity.this, modelList);
                rv.setAdapter(adapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StyleableToast.makeText(MainActivity.this, e.getMessage(), R.style.toast).show();
            }
        });
    }

    public void deleteData(int i) {
        db.collection("Documents").document(modelList.get(i).getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        StyleableToast.makeText(MainActivity.this, "Deleted...", R.style.toast).show();
                        showData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        StyleableToast.makeText(MainActivity.this, e.getMessage(), R.style.toast).show();
                    }
                });
    }
}