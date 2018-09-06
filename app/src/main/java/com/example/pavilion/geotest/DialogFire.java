package com.example.pavilion.geotest;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by Pavilion on 12-04-2018.
 */

public class DialogFire extends DialogFragment{

    public static int RADIUS_PASSED;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_layout, null))
                .setTitle("Alert!")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        EditText et1=(EditText) inflater.inflate(R.layout.dialog_layout,null).findViewById(R.id.radius_dialog);
                        Log.d("DIALOGGING",et1.getText().toString());
                        int x =Integer.parseInt(et1.getText().toString());
                        RADIUS_PASSED=x;

                    }
                }
                );
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
