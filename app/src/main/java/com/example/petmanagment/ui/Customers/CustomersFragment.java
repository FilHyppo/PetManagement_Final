package com.example.petmanagment.ui.Customers;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petmanagment.R;
import com.example.petmanagment.databinding.FragmentCustomersBinding;
import com.example.petmanagment.login.Customer;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class CustomersFragment extends Fragment {


    private FragmentCustomersBinding binding;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText firstname, lastname, address, mobile, email;
    private Button cancel, confirm;
    ArrayList<String> customers = new ArrayList<>();
    ArrayList<String> flag = new ArrayList<>();
    RecyclerView recyclerView;


    private DatabaseReference dataref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CustomersViewModel customersViewModel =
                new ViewModelProvider(this).get(CustomersViewModel.class);

        customers.add("ciao");
        customers.add("luca");
        customers.add("riccardogay");
        customers.add("francesco");
        customers.add("marco");


        binding = FragmentCustomersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        dataref = FirebaseDatabase.getInstance().getReference();

        EditText searchCustomer = (EditText) root.findViewById(R.id.search_customer_editText);
        recyclerView = root.findViewById(R.id.recyclerView);
        final ImageButton add_customer = root.findViewById(R.id.button2);


        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(new ListAdapter(customers));
        final Handler handler = new Handler();
        final Runnable runnable = () -> {
            if (!searchCustomer.getText().toString().isEmpty()) {
                for (String l : customers) {
                    if (l.startsWith(searchCustomer.getText().toString())) {
                        flag.add(l);
                    }
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(new ListAdapter(flag));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(new ListAdapter(customers));
            }

        };

        add_customer.setOnClickListener(view -> addUser());


        searchCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.post(runnable);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeCallbacksAndMessages(runnable);
                flag.clear();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return root;
    }

    public void addUser() {

        dialogBuilder = new AlertDialog.Builder(getContext());
        final View popup = getLayoutInflater().inflate(R.layout.popwindow, null);
        firstname = (EditText) popup.findViewById(R.id.firstname);
        lastname = (EditText) popup.findViewById(R.id.lastname);
        mobile = (EditText) popup.findViewById(R.id.phone);
        email = (EditText) popup.findViewById(R.id.email);

        confirm = (Button) popup.findViewById(R.id.cbutton);

        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.show();

        confirm.setOnClickListener(view -> {
            Customer c = new Customer(firstname.getText().toString(), lastname.getText().toString(), email.getText().toString(), mobile.getText().toString());
            //  writeNewUser(c);
            dialog.dismiss();

        });
    }

    String deletedCustomer = null;

    //ItemTouchHelper serve per implementare l'eliminazione dalla lista scorrendo verso sinistra
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {


        //onMove probabilmente non serve
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT) {
                deletedCustomer = customers.get(position);

                customers.remove(position);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                //la snackbar serve per tornare indietro in caso di errore nella cancellazione
                Snackbar.make(recyclerView, "Customer " + deletedCustomer + " deleted", Snackbar.LENGTH_LONG).setAction("Undo", view -> {
                            customers.add(position, deletedCustomer);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                        }).setActionTextColor(getResources().getColor(R.color.orange))
                        .setTextColor(getResources().getColor(R.color.black))
                        .setBackgroundTint(getResources().getColor(R.color.white))
                        .show();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.delete_icon)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void writeNewUser(Customer customer) {

        dataref.child("users").setValue(customer);
    }
}