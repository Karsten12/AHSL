package com.fonsecakarsten.ahsl.SplashScreen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fonsecakarsten.ahsl.Log_In.Log_In_Activity;
import com.fonsecakarsten.ahsl.R;


public class Credits_Fragment extends Fragment {

    public Credits_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.credits_fragment, container, false);

        Button Done = (Button) rootView.findViewById(R.id.Leave_Button);
        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.drawable.ic_alert);
                builder.setTitle("Beta testing warning")
                        .setMessage("This app is currently in beta and not all features may be present and or working. If you find an error please send report promptly. Thank you.")

                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                Intent LogInIntent = new Intent(getActivity(), Log_In_Activity.class);
                                startActivity(LogInIntent);
                                getActivity().finish();

                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        return rootView;
    }
}
