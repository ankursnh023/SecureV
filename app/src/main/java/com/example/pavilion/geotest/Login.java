package com.example.pavilion.geotest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG="LOGIN_TAG";
    private static final int RC_SIGN_IN=1;
    private EditText txtEmailLogin;
    private EditText txtPwd;
    private Button b1;
    private GoogleSignInClient mGoogleSignInClient;
    private Button b3;
    TextView t1,t2,t3;
    private SignInButton button;
    private CheckBox cc1;
    private FirebaseAuth firebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    SharedPreferences sp;
    FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        t2 = (TextView) findViewById(R.id.b12);
        t1 = (TextView) findViewById(R.id.t11);
        t3 = (TextView) findViewById(R.id.tt3);
        txtEmailLogin = (EditText) findViewById(R.id.e11);
        txtPwd = (EditText) findViewById(R.id.e12);
        b1 = (Button) findViewById(R.id.b11);
        b3 = (Button) findViewById(R.id.b223);
        mAuth = FirebaseAuth.getInstance();

        button = (SignInButton) findViewById(R.id.b13);
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    final ProgressDialog progressDialog=new ProgressDialog(Login.this);
                    progressDialog.setMessage("Authenticating");
                    progressDialog.setTitle("Please wait...");
                    progressDialog.show();
                    Toast.makeText(Login.this,"Called",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getBaseContext(),CustomDialogActivity.class);
                    startActivity(intent);
                    //progressDialog.dismiss();
                    finish();
                }
            }
        };
        cc1 = (CheckBox) findViewById(R.id.c1);
        firebaseAuth = FirebaseAuth.getInstance();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sp.contains("email")) {
            txtEmailLogin.setText(sp.getString("email", ""));
        }
        if (sp.contains("passwrd")) {
            txtPwd.setText(sp.getString("passwrd", ""));
        }


        cc1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                SharedPreferences.Editor editor = sp.edit();


                editor.putString("email", txtEmailLogin.getText().toString());
                editor.putString("passwrd", txtPwd.getText().toString());
                editor.commit();


            }
        });


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sUsername = txtEmailLogin.getText().toString();
                String spass = txtPwd.getText().toString();
                if (sUsername.matches("") || spass.matches("")) {
                } else {
                    final ProgressDialog progressDialog = ProgressDialog.show(Login.this, "Please wait...", "Proccessing...", true);

                    (firebaseAuth.signInWithEmailAndPassword(txtEmailLogin.getText().toString(), txtPwd.getText().toString()))
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();

                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(Login.this, CustomDialogActivity.class);
                                        i.putExtra("Email", firebaseAuth.getCurrentUser().getEmail());
                                        startActivity(i);
                                    } else {
                                        Log.e("ERROR", task.getException().toString());
                                        Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                }
            }
        });


        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent i = new Intent(Login.this, Register.class);
               startActivity(i);
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleApiClient=new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(Login.this,"You got an error",Toast.LENGTH_SHORT).show();
            }
        })
        .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
        .build();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
        //updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }
}




