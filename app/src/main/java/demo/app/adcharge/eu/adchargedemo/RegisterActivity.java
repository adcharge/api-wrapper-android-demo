package demo.app.adcharge.eu.adchargedemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import demo.app.adcharge.eu.adchargedemo.databinding.ActivityRegisterBinding;
import eu.adcharge.api.AdCharge;
import eu.adcharge.api.ApiException;
import eu.adcharge.api.ApiValidationException;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        binding.setTasksInProgress(0);
        binding.setLogin("");
        binding.setPassword("");
    }

    public void openLogin(View view) {
        Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginActivity);
    }

    public void attemptRegister(View view) {
        new RegisterUserTask(binding.getLogin(), binding.getPassword()).execute();
    }

    private class RegisterUserTask extends AsyncTask<String, Void, Void> {
        private final String login;
        private final String pass;
        private ApiValidationException validationException;


        public RegisterUserTask(String login, String password) {
            this.login = login;
            this.pass = password;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                AdCharge.registerSubscriberUser(login, pass, BuildConfig.INDIVIDUAL_KEY);
                Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginActivity);
            } catch (IOException e) {
                binding.setError(e.getMessage());
            } catch (ApiException e) {
                binding.setError(e.getMessage());
            } catch (ApiValidationException e) {
                validationException = e;//to be processed in ui thread
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (validationException != null) {
                for (ApiValidationException.Error fieldError : validationException.getFieldErrors()) {
                    String field = fieldError.getField();
                    String message = fieldError.getMessages().isEmpty() ? "" : fieldError.getMessages().get(0);
                    if ("username".equals(field)) {
                        binding.login.setError(message);
                    } else if ("password".equals(field)) {
                        binding.password.setError(message);
                    } else {
                        binding.setError(message);
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
