package com.example.pavilion.geotest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Register extends AppCompatActivity {
    private EditText txtEmailAddress;
    private TextView tt;
    private EditText txtPassword;
    private EditText ph;
    private FirebaseAuth firebaseAuth;
    private Button b22;
    DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        tt=(TextView)findViewById(R.id.t21);
        b22=(Button)findViewById(R.id.b22);
        txtEmailAddress = (EditText) findViewById(R.id.e21);
        txtPassword = (EditText) findViewById(R.id.e22);
        ph = (EditText) findViewById(R.id.e23);
        firebaseAuth = FirebaseAuth.getInstance();

        rootRef= FirebaseDatabase.getInstance().getReference();




        b22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent p =new Intent(Register.this,Login.class);
                startActivity(p);
            }
        });
    }
    public void btnRegistrationUser_Click(View v) {

        final ProgressDialog progressDialog = ProgressDialog.show(Register.this, "Please wait...", "Processing...", true);
        (firebaseAuth.createUserWithEmailAndPassword(txtEmailAddress.getText().toString(), txtPassword.getText().toString())).addOnCompleteListener(new  OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                DatabaseReference rootRefchild=rootRef.push();
                DatabaseReference securev=rootRefchild.child("email");
                DatabaseReference securev1=rootRefchild.child("password");
                DatabaseReference securev2=rootRefchild.child("phoneno");
                securev.setValue(txtEmailAddress.getText().toString());
                securev1.setValue(txtPassword.getText().toString());
                securev2.setValue(ph.getText().toString());


                if (task.isSuccessful()) {
                    Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();

                } else {
                    Log.e("ERROR", task.getException().toString());
                    Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });}


}
