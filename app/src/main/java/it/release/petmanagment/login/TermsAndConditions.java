package it.release.petmanagment.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import it.release.petmanagment.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TermsAndConditions extends AppCompatActivity {


    private CheckBox termsCheckBox;
    private Button checkButton;
    private MaterialAlertDialogBuilder materialAlertDialogBuilder;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        SharedPreferences sharedPreferences = this.getSharedPreferences("Application", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        termsCheckBox = (CheckBox) findViewById(R.id.check_id);
        checkButton = (Button) findViewById(R.id.terms_button);

        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);

        checkButton.setEnabled(false);

        checkButton.setOnClickListener(view -> {
            Intent homeActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(homeActivity);
        });

        termsCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                materialAlertDialogBuilder.setTitle("Terms and conditions");
                materialAlertDialogBuilder.setMessage("I accept the processing of personal data");
                materialAlertDialogBuilder.setPositiveButton("Accept", (dialogInterface, i) -> {
                    checkButton.setEnabled(true);
                    editor.putBoolean("termsAndConditions", true);
                    editor.apply();
                    dialogInterface.dismiss();
                });
                materialAlertDialogBuilder.setNegativeButton("Decline", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    termsCheckBox.setChecked(false);
                });
                materialAlertDialogBuilder.show();
            }
            else{
                checkButton.setEnabled(false);
            }
        });
    }
}