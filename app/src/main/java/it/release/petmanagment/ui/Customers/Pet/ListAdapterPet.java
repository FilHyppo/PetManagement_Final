package it.release.petmanagment.ui.Customers.Pet;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.release.petmanagment.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ListAdapterPet extends RecyclerView.Adapter<ListAdapterPet.MyViewHolder> {
    //creo un adapter per la recycle view
    FirebaseUser user;
    String customerName;
    FirebaseFirestore db;
    ArrayList<String> list;

    public ListAdapterPet(ArrayList<String> list, String name) {
        this.list = list;
        this.customerName=name;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pets_list_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tvPet.setText(list.get(position));
        holder.tvcustomer.setText(customerName);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvPet;
        TextView tvcustomer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPet = itemView.findViewById(R.id.tvpetname);
            tvcustomer=itemView.findViewById(R.id.tvpadrone);
            itemView.setOnClickListener(view -> {
                Intent clientWindow = new Intent(view.getContext(), FinalPetActivity.class);
                clientWindow.putExtra("Name", tvPet.getText());
                clientWindow.putExtra("CustomerName",tvcustomer.getText());
                view.getContext().startActivity(clientWindow);
            });

        }
    }

}
