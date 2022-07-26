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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petmanagment.R;
import com.example.petmanagment.databinding.FragmentCustomersBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.UUID;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class CustomersFragment extends Fragment {


    private FragmentCustomersBinding binding;
    private AlertDialog dialog;
    private EditText firstname;
    private EditText lastname;
    private EditText mobile;
    private EditText email;
    private String nome, cognome;
    private Button confirm;
    ArrayList<String> customers = new ArrayList<>();
    ArrayList<String> flag = new ArrayList<>();
    RecyclerView recyclerView;
    private DatabaseReference dataref;
    FirebaseUser user;
    FirebaseFirestore db;
    ListAdapter listAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CustomersViewModel customersViewModel =
                new ViewModelProvider(this).get(CustomersViewModel.class);


        binding = FragmentCustomersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        dataref = FirebaseDatabase.getInstance().getReference();

        EditText searchCustomer = (EditText) root.findViewById(R.id.search_customer_editText);
        recyclerView = root.findViewById(R.id.recyclerView);
        final ImageButton add_customer = root.findViewById(R.id.button2);

        getCustomers(customers);
        listAdapter = new ListAdapter(customers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(listAdapter);

        final Handler handler = new Handler();
        final Runnable runnable = () -> {
            getCustomers(customers);
            if (!searchCustomer.getText().toString().isEmpty()) {
                for (String l : customers) {
                    if (l.startsWith(searchCustomer.getText().toString()) || l.contains(searchCustomer.getText().toString())) {
                        flag.add(l);
                    }
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(new ListAdapter(flag));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(listAdapter);
            }

        };

        add_customer.setOnClickListener(view -> elaborateUser("add", -1));


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

    public void elaborateUser(String operation, int eventualPosition) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
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
            switch (operation) {
                case "add":
                    Customer c = new Customer(firstname.getText().toString(), lastname.getText().toString(), mobile.getText().toString(), email.getText().toString(), UUID.randomUUID().toString());
                    addNewCustomer(c);
                    // getCustomers(customers);
                    break;
                case "modify":
                    updateCustomers(customers.get(eventualPosition), firstname.getText().toString(), lastname.getText().toString(), mobile.getText().toString(), email.getText().toString());
                    if (!firstname.getText().toString().isEmpty() || !lastname.getText().toString().isEmpty())
                        customers.set(eventualPosition, String.format("%s\t%s", firstname.getText(), lastname.getText()));
                    break;
            }
            getCustomers(customers);
            dialog.dismiss();

        });
    }

    String deletedCustomer = null;
    String modifiedCustomer = null;


    //ItemTouchHelper serve per implementare l'eliminazione dalla lista scorrendo verso sinistra
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {


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
                Snackbar snackbar = Snackbar.make(recyclerView, "Customer " + deletedCustomer + " deleted", Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback());
                snackbar.addCallback(new Snackbar.Callback() {

                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    deleteCustomers(deletedCustomer);
                                }
                            }
                        }).setAction("Undo", view -> {
                            customers.add(position, deletedCustomer);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                        }).setActionTextColor(getResources().getColor(R.color.orange))
                        .setTextColor(getResources().getColor(R.color.black))
                        .setBackgroundTint(getResources().getColor(R.color.white))
                        .show();
            } else {
                modifiedCustomer = customers.get(position);
                elaborateUser("modify", position);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .setIconHorizontalMargin(16)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.delete_icon)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_blue))
                    .addSwipeRightActionIcon(R.drawable.edit_icon)
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

    public void addNewCustomer(Customer customer) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        ArrayList<Customer> current_customer = new ArrayList<>();
        current_customer.add(customer);
        db.collection(user.getEmail().toString()).document(customer.getName().toString() + '\t' + customer.getLastName().toString()).set(customer, SetOptions.merge()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Customer added successfully", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getCustomers(ArrayList<String> c) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        db.collection(user.getEmail().toString()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (!c.contains(document.getId()) && !document.getId().contains("Date"))
                            c.add(document.getId());
                        System.out.println(customers);
                        listAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void deleteCustomers(String customerName) {
        db.collection(user.getEmail().toString()).document(customerName).delete();
    }

    public void updateCustomers(String customerName, String name, String lastName, String mobile, String email) {
        if (!name.isEmpty()) {
            db.collection(user.getEmail()).document(customerName).update("name", name);
            nome = name;
        } else {
            db.collection(user.getEmail()).document(customerName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    nome = documentSnapshot.getString("name");
                }
            });
        }
        if (!lastName.isEmpty()) {
            db.collection(user.getEmail()).document(customerName).update("lastName", lastName);
            cognome = lastName;
        } else {
            db.collection(user.getEmail()).document(customerName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    cognome = documentSnapshot.getString("lastName");
                }
            });
        }
        if (!email.isEmpty())
            db.collection(user.getEmail()).document(customerName).update("email", email);
        if (!mobile.isEmpty())
            db.collection(user.getEmail()).document(customerName).update("phone", mobile);
        db.collection(user.getEmail()).document(customerName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String fileName;
                fileName = nome + '\t' + cognome;
                db.collection(user.getEmail().toString()).document(fileName).set(documentSnapshot.getData(), SetOptions.merge()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Customer edited successfully", Toast.LENGTH_LONG).show();
                        if (!fileName.equals(customerName))
                            db.collection(user.getEmail()).document(customerName).collection(customerName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for(QueryDocumentSnapshot q: queryDocumentSnapshots)
                                    {
                                        db.collection(user.getEmail()).document(fileName).collection(fileName).document(q.getId()).set(q.getData(),SetOptions.merge());
                                    }
                                }
                            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    db.collection(user.getEmail()).document(customerName).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            customers.clear();
                                            getCustomers(customers);
                                        }
                                    });
                                }
                            });

                    }
                });
            }
        });
    }
}
