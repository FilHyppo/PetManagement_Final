package it.release.petmanagment.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.example.petmanagment.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MyDatesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dates);
         EditText multiLine = (EditText) findViewById(R.id.editTextTextMultiLine);
         multiLine.setFocusable(false);
         multiLine.setText("");
         FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
         FirebaseFirestore db = FirebaseFirestore.getInstance();
         db.collection(user.getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
             @Override
             public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                 for(QueryDocumentSnapshot q: queryDocumentSnapshots){
                     if(q.getId().contains("Date")){
                         String text;
                         text = String.format("%s, %s, %s, %s \n\n", q.get("date"), q.get("hour"), q.get("name"), q.get("pet"));
                         System.out.println(text);
                         multiLine.setText(multiLine.getText()+text);
                     }
                 }
             }
         });

    }
}