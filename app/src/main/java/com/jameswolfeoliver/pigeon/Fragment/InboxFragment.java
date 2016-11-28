package com.jameswolfeoliver.pigeon.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.SecurityHelper;

import java.security.NoSuchAlgorithmException;

public class InboxFragment extends Fragment {

    private Button submit;
    private Button validate;
    private EditText password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, null);
        submit = (Button) rootView.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Can store", Boolean.toString(SecurityHelper.getInstance().storeUserPassword(password.getText().toString(), new SecurityHelper.TokenCallback() {
                    @Override
                    public void onTokenGenerated() {
                        Log.d("Generated", "true");

                    }

                    @Override
                    public void onTokenGenerationFailed() {
                        Log.d("Generated", "false");
                    }
                })));
            }
        });
        validate = (Button) rootView.findViewById(R.id.validate);
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Can authenticate", Boolean.toString(SecurityHelper.getInstance().checkUserPassword(password.getText().toString(), new SecurityHelper.AuthenticationCallback() {
                    @Override
                    public void onUserAuthenticated() {
                        Log.d("Authenticated", "true");
                    }

                    @Override
                    public void onUserAuthenticationFailed() {
                        Log.d("Authenticated", "false");
                    }
                })));
            }
        });
        password = (EditText) rootView.findViewById(R.id.password);
        return rootView;
    }
}
