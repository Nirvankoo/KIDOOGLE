package com.nirv.proj1;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;
    private SignInButton signInButtonGoogle; // Corrected variable name

    // Google auth
    private static final int RC_SIGN_IN = 9001;
    GoogleApiClient mGoogleApiClient;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        signupEmail = findViewById(R.id.sign_email);
        signupPassword = findViewById(R.id.sign_password);
        signupButton = findViewById(R.id.sign_button);
        loginRedirectText = findViewById(R.id.loggin_redirect_text);
        signInButtonGoogle = findViewById(R.id.google_sign_in_button); // Corrected ID

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage((FragmentActivity) this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult);
                        Toast.makeText(SignUpActivity.this, "Google Play services connection failed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set onClickListener for sign-up button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();

                if (user.isEmpty()) {
                    signupEmail.setError("Email cannot be empty");
                    return;
                }
                if (pass.isEmpty()) {
                    signupPassword.setError("Password cannot be empty");
                    return;
                }

                // Create user with email and password
                auth.createUserWithEmailAndPassword(user, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Set onClickListener for login redirect text
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        // Set onClickListener for Google Sign-In Button
        signInButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(); // Call the signIn() method to initiate Google Sign-In
            }
        });
    }

    // Method to initiate Google Sign-In
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient); // You need to initialize mGoogleApiClient
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // onActivityResult() method for handling the result of Google Sign-In
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    // Method to handle the result of Google Sign-In
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Google sign-in successful, handle it accordingly
            GoogleSignInAccount account = result.getSignInAccount();
            Toast.makeText(this, "Google Sign-In successful: " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
            // Proceed with your desired logic, such as signing in the user with Firebase or navigating to another activity
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        } else {
            // Google sign-in failed, handle it accordingly
            Toast.makeText(this, "Google Sign-In failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}