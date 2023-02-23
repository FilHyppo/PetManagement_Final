package it.release.petmanagment.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import it.release.petmanagment.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DateActivity extends AppCompatActivity{
    private static final int PERMISSION_GRANTED = 1;
    private Spinner spinner;
    private Spinner spinner2;
    private String selected;
    private String selectedP;
    private List<String>pets;
    private Spinner pSpinner;
    private CalendarView calendar;
    private String selectedDate;
    private TextView sDate;
    private String hour;
    final int callbackId = 42;
    GregorianCalendar storeDate;
    TextView myDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        final Spinner cSpinner = (Spinner) findViewById(R.id.spinner);
        pSpinner = (Spinner) findViewById(R.id.spinner2);
        FirebaseUser  user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List <String> customers = new ArrayList<>();
        pets = new ArrayList<>();
        calendar = (CalendarView) findViewById (R.id.calendarView);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        selectedDate = sdf.format(new Date(calendar.getDate()));
        sDate = (TextView)  findViewById(R.id.selectedDate);
        sDate.setText(selectedDate);
        Button confirmBtn = (Button) findViewById(R.id.confirmBtn);
        myDates = (TextView) findViewById(R.id.meetingView);

        checkPermission(callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);


        db.collection(user.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot q: queryDocumentSnapshots){
                if(!q.getId().contains("Date"))
                    customers.add(q.getId());
            }
        }).addOnCompleteListener(task -> {
            final ArrayAdapter<String> content = new ArrayAdapter<String>
                    (getApplicationContext(), R.layout.spinner_item, customers);
            content.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cSpinner.setAdapter(content);
        });

        myDates.setOnClickListener(view -> {
            Intent myDatesNavigator = new Intent(this, MyDatesActivity.class);
            startActivity(myDatesNavigator);
        });

       cSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               if(!pets.isEmpty()) pets.clear();
               selected = adapterView.getItemAtPosition(i).toString();
               db.collection(user.getEmail()).document(selected).collection(selected).get().addOnSuccessListener(queryDocumentSnapshots -> {
                   for(QueryDocumentSnapshot q : queryDocumentSnapshots){
                       pets.add(q.getId());
                   }
               }).addOnCompleteListener(task1 -> {
                   final ArrayAdapter<String> content2 = new ArrayAdapter<>
                           (getApplicationContext(), R.layout.spinner_item, pets);
                   content2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                   pSpinner.setAdapter(content2);
               });
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {
           }
       });

       pSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               selectedP = adapterView.getItemAtPosition(i).toString();

           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
       });

       calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
           @Override
           public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
               sDate.setText(String.format("%s/%s/%s",String.valueOf(i2), String.valueOf(i1+1),String.valueOf(i)));
               storeDate = new GregorianCalendar(i,i1,i2);
           }
       });

       confirmBtn.setOnClickListener(view -> {
           EditText time = (EditText)findViewById(R.id.editTextTime);
           Meeting m = new Meeting(selected, selectedP, sDate.getText().toString(), time.getText().toString());
           db.collection(user.getEmail()).document("Date"+ selected+selectedP).set(m).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void unused) {
                   Toast.makeText(getApplicationContext(), "Date addedd succesfully", Toast.LENGTH_LONG).show();
               }
           });
       });

    }
    private void checkPermission(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions)
            ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }

}