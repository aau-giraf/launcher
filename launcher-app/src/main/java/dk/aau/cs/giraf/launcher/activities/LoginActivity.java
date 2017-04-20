package dk.aau.cs.giraf.launcher.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import dk.aau.cs.giraf.activity.GirafActivity;
import dk.aau.cs.giraf.launcher.R;

public class LoginActivity extends GirafActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_activity2);
    }

    public void LoginBtnPressed(View view){
        Toast.makeText(this,"Test",Toast.LENGTH_LONG).show();
    }
}
