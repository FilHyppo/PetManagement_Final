package it.release.petmanagment.ui.Customers.Pet;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petmanagment.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PetActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseFirestore db;
    String id;
    FirebaseStorage storage;
    String info;
    StorageReference storageRef;
    RecyclerView recyclerView;
    ArrayList<String> pets = new ArrayList<>();
    String deletedPet = null;
    String modifiedPet = null;
    ListAdapterPet listAdapterPet;
    private EditText name;
    private EditText race;
    private CheckBox typology_dog;
    private CheckBox typology_cat;
    private AlertDialog dialog;
    private ImageButton add_pet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);
        TextView customerName = findViewById(R.id.customer_name);
        ImageView icon = (ImageView) findViewById(R.id.imageView3);
        TextView infoLink = (TextView) findViewById(R.id.customer_info);
        TextView custoInfo = (TextView) findViewById(R.id.custoInfo);
        custoInfo.setCursorVisible(false);
        custoInfo.setTextSize(14);

        custoInfo.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.recyclerView);
        add_pet = findViewById(R.id.addpet);
        add_pet.setOnClickListener(view -> elaboratePet("add", -1));

        Bundle extras = getIntent().getExtras();
        info = extras.getString("NameLastName");
        customerName.setText(info);

        Intent intent = new Intent(PetActivity.this, FinalPetActivity.class);
        intent.putExtra("NameLastName", info);


        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();
        assert user != null;

        db.collection(user.getEmail()).document(info).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name, lastName, phone, email;
                String content;

                id = documentSnapshot.getString("uuid");
                name = documentSnapshot.getString("name");
                lastName = documentSnapshot.getString("lastName");
                phone = documentSnapshot.getString("phone");
                email = documentSnapshot.getString("email");

                content = String.format("Name: %s\nSurname: %s\nPhone: %s\nEmail: %s\n", name, lastName, phone, email);
                custoInfo.setText(content);

                assert id != null;

                storageRef.child(id).getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageView icon1 = (ImageView) findViewById(R.id.imageView3);
                    ImageView smallCamera = (ImageView) findViewById(R.id.imcamera);
                    smallCamera.setVisibility(View.INVISIBLE);
                    Glide
                            .with(getApplicationContext())
                            .load(uri)
                            .into(icon1);
                }).addOnFailureListener(e -> System.out.println("no image"));
            }
        });

        icon.setOnClickListener(view -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, 3);
            ImageView camera_icon = findViewById(R.id.imcamera);
            camera_icon.setVisibility(View.INVISIBLE);
        });

        getPets(pets);
        listAdapterPet = new ListAdapterPet(pets, info);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(listAdapterPet);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        infoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (custoInfo.getVisibility() == View.VISIBLE)
                    custoInfo.setVisibility(View.INVISIBLE);
                else
                    custoInfo.setVisibility(View.VISIBLE);
            }
        });

    }


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
                deletedPet = pets.get(position);

                pets.remove(position);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                //la snackbar serve per tornare indietro in caso di errore nella cancellazione
                Snackbar snackbar = Snackbar.make(recyclerView, "Customer " + deletedPet + " deleted", Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback());
                snackbar.addCallback(new Snackbar.Callback() {

                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    deletePets(deletedPet);
                                }
                            }
                        }).setAction("Undo", view -> {
                            pets.add(position, deletedPet);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        }).setActionTextColor(getResources().getColor(R.color.orange))
                        .setTextColor(getResources().getColor(R.color.black))
                        .setBackgroundTint(getResources().getColor(R.color.white))
                        .show();
            } else {
                modifiedPet = pets.get(position);
                elaboratePet("modify", position);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(getApplicationContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .setIconHorizontalMargin(16)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.delete_icon)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue))
                    .addSwipeRightActionIcon(R.drawable.edit_icon)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            ImageView icon = (ImageView) findViewById(R.id.imageView3);
            icon.setImageURI(selectedImage);
            StorageReference imageRef = storageRef.child(id);
            UploadTask imageLoader = (UploadTask) imageRef.putFile(selectedImage).addOnSuccessListener(taskSnapshot -> Toast.makeText(PetActivity.this, "Upload ok", Toast.LENGTH_LONG))
                    .addOnFailureListener(e -> Toast.makeText(PetActivity.this, "No upload", Toast.LENGTH_LONG));
        }
    }

    public void addNewPet(Pet pet) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        ArrayList<Pet> current_pet = new ArrayList<>();
        current_pet.add(pet);
        db.collection(user.getEmail())
                .document(info)
                .collection(info)
                .document(pet.getName())
                .set(pet, SetOptions.merge()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Pet added successfully", Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void elaboratePet(String operation, int eventualPosition) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popwindow_pet, null);
        name = (EditText) popup.findViewById(R.id.firstname);
        race = (EditText) popup.findViewById(R.id.race);
        typology_dog = (CheckBox) popup.findViewById(R.id.checkBoxDog);
        typology_cat = (CheckBox) popup.findViewById(R.id.checkBoxCat);
        Button confirm = (Button) popup.findViewById(R.id.pet_button);
        typology_dog.setChecked(true);
        typology_dog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                typology_cat.setChecked(false);
                typology_dog.setChecked(true);
            }
        });
        typology_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                typology_dog.setChecked(false);
                typology_cat.setChecked(true);
            }

        });
        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.show();

        confirm.setOnClickListener(view -> {
            switch (operation) {
                case "add":
                    Pet c;
                    if (typology_dog.isChecked())
                        c = new Pet(name.getText().toString(), race.getText().toString(), "Dog", UUID.randomUUID().toString());
                    else
                        c = new Pet(name.getText().toString(), race.getText().toString(), "Cat", UUID.randomUUID().toString());
                    addNewPet(c);
                    break;
                case "modify":
                    if (typology_dog.isChecked()) {
                        updatePets(pets.get(eventualPosition), name.getText().toString(), race.getText().toString(), "Dog");
                        typology_cat.setChecked(false);
                    } else if (typology_cat.isChecked()) {
                        updatePets(pets.get(eventualPosition), name.getText().toString(), race.getText().toString(), "Cat");
                        typology_dog.setChecked(false);
                    }
                    if (!name.getText().toString().isEmpty() || !race.getText().toString().isEmpty())
                        pets.set(eventualPosition, String.format("%s", name.getText()));
                    break;
            }
            getPets(pets);
            dialog.dismiss();

        });

    }

    public void updatePets(String petname, String name, String race, String typology) {
        db.collection(user.getEmail())
                .document(info)
                .collection(info)
                .document(petname).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String fileName;
                        if (name.isEmpty())
                            fileName = petname;
                        else
                            fileName = name;
                        System.out.println(fileName);
                        db.collection(user.getEmail()).document(info).collection(info).document(fileName).set(documentSnapshot.getData(), SetOptions.merge()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PetActivity.this, "Pet edited successfully", Toast.LENGTH_LONG).show();
                                if (!fileName.equals(petname))
                                    db.collection(user.getEmail())
                                            .document(info)
                                            .collection(info)
                                            .document(petname).delete();
                                if (!name.isEmpty())
                                    db.collection(user.getEmail())
                                            .document(info)
                                            .collection(info)
                                            .document(fileName)
                                            .update("name", name);
                                if (!race.isEmpty())
                                    db.collection(user.getEmail())
                                            .document(info)
                                            .collection(info)
                                            .document(fileName)
                                            .update("race", race);
                                if (!typology.isEmpty())
                                    db.collection(user.getEmail())
                                            .document(info)
                                            .collection(info)
                                            .document(fileName)
                                            .update("typology", typology);

                                pets.clear();
                                getPets(pets);
                            }
                        });
                    }
                });

    }

    public void getPets(ArrayList<String> c) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        db.collection(user.getEmail())
                .document(info)
                .collection(info)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        System.out.println(document.getData());
                        if (!c.contains(document.getId()))
                            c.add(document.getId());
                        listAdapterPet.notifyDataSetChanged();
                    }
                });
    }

    public void deletePets(String name) {
        db.collection(user.getEmail())
                .document(info)
                .collection(info)
                .document(name)
                .delete();

    }

}