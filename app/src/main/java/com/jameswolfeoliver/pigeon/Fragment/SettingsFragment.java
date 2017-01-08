package com.jameswolfeoliver.pigeon.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jameswolfeoliver.pigeon.Managers.SecurityHelper;
import com.jameswolfeoliver.pigeon.R;
import com.jaredrummler.android.device.DeviceName;

public class SettingsFragment extends Fragment {

    TextView deviceName;
    EditText serverPassword;
    ImageView savePassword;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, null);

        deviceName = (TextView) rootView.findViewById(R.id.server_settings_name);
        deviceName.setText(DeviceName.getDeviceName());

        serverPassword = (EditText) rootView.findViewById(R.id.sever_settings_password);
        savePassword = (ImageView) rootView.findViewById(R.id.sever_save_password);
        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecurityHelper.getInstance().storeUserPassword(serverPassword.getText().toString(), new SecurityHelper.TokenCallback() {
                    @Override
                    public void onTokenGenerated() {
                    }

                    @Override
                    public void onTokenGenerationFailed() {
                    }
                });
            }
        });


        return rootView;
    }
}
