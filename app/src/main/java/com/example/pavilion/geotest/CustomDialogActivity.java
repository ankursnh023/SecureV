package com.example.pavilion.geotest;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CustomDialogActivity extends FragmentActivity {

    EditText user_radius;
    Button button;
    int r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_dialog);
        user_radius=(EditText)findViewById(R.id.user_radius);
        button=(Button)findViewById(R.id.set_radius_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r=Integer.parseInt(user_radius.getText().toString());
                Intent intent=new Intent(CustomDialogActivity.this,MapsActivity.class);
                intent.putExtra("USER_RADIUS",r);
                startActivity(intent);
                finish();
            }
        });

    }
}
