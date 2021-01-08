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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.instashop.Helpers.LocalStorage;
import com.makarand.instashop.Models.User;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    AppCompatEditText emailField, passwordField;
    Button login;
    LinearLayout splashScreen, loginContainer;
    RelativeLayout parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        login = findViewById(R.id.loginButton);
        splashScreen = findViewById(R.id.splashScreen);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        parent = findViewById(R.id.parent);
        loginContainer = findViewById(R.id.loginContainer);
        checkUserSignedIn();
    }

    public void login(View v){
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if(!validate(email, password)) return;
        disableLayout();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                           loginUser();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                        enableLayout();
                    }
                });
    }

    private void disableLayout(){
        login.setText("Loading...");
        parent.setClickable(false);
        parent.setAlpha(0.2f);
    }

    private void enableLayout(){
        login.setText("LOGIN");
        parent.setClickable(true);
        parent.setAlpha(1f);
    }

    private boolean validate(String email, String password) {
        if((TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches())){
            emailField.setError("Invalid email id.");
            return false;
        }

        if(password.length() < 4){
            passwordField.setError("Password must be at least 4 letters long");
            return false;
        }

        return true;
    }

    private void checkUserSignedIn(){
        if(auth.getCurrentUser() == null) {
            splashScreen.setVisibility(View.GONE);
            loginContainer.setVisibility(View.VISIBLE);
            return;
        }
        User user = LocalStorage.getInstance().getStoredObject(Constants.LOCAL_STORAGE_KEY_USER_INFO, User.class, getApplicationContext());
        if(user == null)
            loginUser();
        else {
            launchNextActivity(user);
        }
    }

    private void loginUser() {
        String id = auth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.UserTree);
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null) {
                    Toast.makeText(LoginActivity.this, "No internet access.", Toast.LENGTH_SHORT).show();
                    return;
                }
                User user = snapshot.getValue(User.class);
                LocalStorage localStorage = LocalStorage.getInstance();
                localStorage.storeObject(Constants.LOCAL_STORAGE_KEY_USER_INFO, user, getApplicationContext());

                launchNextActivity(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "No internet access.", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void launchNextActivity(User user) {
        if(user.getUserType() == Constants.SELLER)
            startActivity(new Intent(getApplicationContext(), SellerCameraActivity.class));
        else if(user.getUserType() == Constants.BUYER){
            startActivity(new Intent(getApplicationContext(), BuyerActivity.class));
        }
        finish();
    }

    public void goToSignUp(View v){
        startActivity(new Intent(this, SignupActivity.class));
    }
}