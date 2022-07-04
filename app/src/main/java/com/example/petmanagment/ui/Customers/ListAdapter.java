package com.example.petmanagment.ui.Customers;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petmanagment.R;
import com.example.petmanagment.ui.Customers.Pet.PetActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {
    //creo un adapter per la recycle view
    FirebaseUser user;
    FirebaseFirestore db;
    ArrayList<String> list;

    public ListAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.customers_list_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvNameSurname.setText(list.get(position));
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        db.collection(user.getEmail()).document(holder.tvNameSurname.getText().toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                holder.tvPhone.setText(documentSnapshot.getString("phone"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvNameSurname;
        TextView tvPhone;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameSurname = itemView.findViewById(R.id.tvnamesurname);
            tvPhone = itemView.findViewById(R.id.tvphone);
            itemView.setOnClickListener(view -> {
                Intent clientWindow = new Intent(view.getContext(), PetActivity.class);
                clientWindow.putExtra("ID", tvPhone.getText());
                clientWindow.putExtra("NameLastName", tvNameSurname.getText());
                view.getContext().startActivity(clientWindow);
            });

        }
    }
}
