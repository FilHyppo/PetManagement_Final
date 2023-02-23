package it.release.petmanagment.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import it.release.petmanagment.HomeActivity;
import com.example.petmanagment.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth lAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lAuth = FirebaseAuth.getInstance();
        TextView username = (TextView) findViewById(R.id.username);
        TextView password = (TextView) findViewById(R.id.password);


        MaterialButton signinBtn = (MaterialButton) findViewById(R.id.signinBtn);
        MaterialButton loginBtn = (MaterialButton) findViewById(R.id.loginBtn);
        MaterialButton forgotpasswordBtn = (MaterialButton) findViewById(R.id.forgotpasswordBtn);

        SharedPreferences sharedPreferences = getSharedPreferences("Application", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("termsAndConditions", false)) {
            Intent condAndTerms = new Intent(getApplicationContext(), TermsAndConditions.class);
            startActivity(condAndTerms);
        }

        loginBtn.setOnClickListener(v -> {
            String email = username.getText().toString();
            String pw = password.getText().toString();
            if (email.isEmpty()) {
                username.setError("email is required");
                username.requestFocus();
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                username.setError("Insert a valid email");
                username.requestFocus();
                return;
            }
            if (pw.isEmpty()) {
                password.setError("password is required");
                password.requestFocus();
                return;
            }
            lAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(task -> {
                if (task.isSuccessful() && lAuth.getCurrentUser().isEmailVerified()) {
                    Toast.makeText(MainActivity.this, "login succesful", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "login failed, verify your account credentials or verify your email", Toast.LENGTH_LONG).show();
                }
            });

        });

        signinBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
            startActivity(intent);

        });
        forgotpasswordBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPWActivity.class);
            startActivity(intent);

        });
    }
}