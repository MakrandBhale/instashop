package com.makarand.instashop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makarand.instashop.Helpers.LocalStorage;
import com.makarand.instashop.Models.User;

import static android.view.View.NO_ID;

public class SignupActivity extends AppCompatActivity {
    AppCompatEditText name, email, password;
    MaterialButtonToggleGroup userTypeToggle;
    Button signupButton;
    LinearLayout parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        userTypeToggle = findViewById(R.id.userTypeToggle);
        signupButton = findViewById(R.id.signupButton);
        parent = findViewById(R.id.parent);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
                disableLayout();
            }
        });
    }

    private void disableLayout(){
        signupButton.setText("Loading...");
        parent.setClickable(false);
        parent.setAlpha(0.2f);
    }

    private void enableLayout(){
        signupButton.setText("SIGN UP");
        parent.setClickable(true);
        parent.setAlpha(1f);
    }

    private void signup() {
        final String nameString = name.getText().toString().trim();
        final String emailString = email.getText().toString().trim();
        String passwordString = password.getText().toString().trim();
        final int userType = getUserType();
        if(!validate(nameString, emailString, passwordString, userType)) {
            enableLayout();
            return;
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            submitUserData(nameString, emailString, userType);
                        } else {
                            Toast.makeText(SignupActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void submitUserData(String nameString, String emailString, int userType) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userReference = database.getReference(Constants.UserTree);
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final User user = new User(nameString, emailString, id, userType);

        userReference.child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String message = "";
                if(task.isSuccessful()){
                    message = "User created successfully.";
                    if(user.getUserType() == Constants.SELLER)
                        startActivity(new Intent(getApplicationContext(), SellerCameraActivity.class));
                    else if(user.getUserType() == Constants.BUYER){
                        startActivity(new Intent(getApplicationContext(), BuyerActivity.class));
                    }
                    LocalStorage localStorage = LocalStorage.getInstance();
                    localStorage.storeObject(Constants.LOCAL_STORAGE_KEY_USER_INFO, user, getApplicationContext());
                    finish();
                } else {
                    message = "Error occurred";
                }
                enableLayout();
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getUserType() {
        int checkedType = userTypeToggle.getCheckedButtonId();
        if(checkedType == R.id.buyerToggle) return Constants.BUYER;
        if(checkedType == R.id.sellerToggle) return Constants.SELLER;
        return NO_ID;
    }

    private boolean validate(String nameString, String emailString, String passwordString, int userType) {

        if(nameString.length() < 3) {
            name.setError("Name must be at least 3 characters long.");
            return false;
        }

        if((TextUtils.isEmpty(emailString) && !Patterns.EMAIL_ADDRESS.matcher(emailString).matches())){
            email.setError("Invalid email id.");
            return false;
        }

        if(passwordString.length() < 4){
            password.setError("Password must be at least 4 letters long");
            return false;
        }

        if (userType == NO_ID) {
            Toast.makeText(this, "Select user type", Toast.LENGTH_SHORT).show();
            userTypeToggle.requestFocus();
            return false;
        }

        return true;
    }

    public void goToLogin(View v){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}