package it.release.petmanagment.login;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import it.release.petmanagment.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPWActivity extends AppCompatActivity {

    private FirebaseAuth fgAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pwactivity);
        fgAuth = FirebaseAuth.getInstance();
        TextView email = (TextView) findViewById(R.id.email);
        MaterialButton submit=(MaterialButton) findViewById(R.id.submitBtn);
        submit.setOnClickListener(view -> {
            if (email.getText().toString().isEmpty()) {
                email.setError("email is required");
                email.requestFocus();
                return;}

            fgAuth.sendPasswordResetEmail(email.getText().toString());
            Toast.makeText(ForgotPWActivity.this, "You will receive a new reset password", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }
}