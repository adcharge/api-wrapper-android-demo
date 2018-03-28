package demo.app.adcharge.eu.adchargedemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import demo.app.adcharge.eu.adchargedemo.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setTasksInProgress(0);
        binding.signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


    }


    private void attemptLogin() {
        Intent main_activity = new Intent(getApplicationContext(), MainActivity.class);
        main_activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        main_activity.putExtra("username", binding.getLogin());
        startActivity(main_activity);
        finish();
    }


}

