package demo.app.adcharge.eu.adchargedemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;
import java.net.MalformedURLException;

import demo.app.adcharge.eu.adchargedemo.databinding.ActivityLoginBinding;
import eu.adcharge.api.AdCharge;
import eu.adcharge.api.ApiException;
import eu.adcharge.api.ApiValidationException;
import eu.adcharge.api.util.TokenHolder;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AdCharge sdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            sdk = new AdCharge(BuildConfig.SERVER_URL, getApplicationContext());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            finish();
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setTasksInProgress(0);
        binding.setLogin("");
        binding.setPassword("");
    }

    public void openRegister(View view) {
        Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
        registerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(registerActivity);
    }

    public void attemptLogin(View view) {
        new LoginTask(binding.getLogin(), binding.getPassword()).execute();
    }

    private class LoginTask extends AsyncTask<String, Void, Void> {
        private final String login;
        private final String pass;
        private ApiValidationException validationException;


        public LoginTask(String login, String password) {
            this.login = login;
            this.pass = password;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                sdk.login(login, pass, BuildConfig.INDIVIDUAL_KEY);
                Intent profileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
                profileActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                profileActivity.putExtra("username", binding.getLogin());
                startActivity(profileActivity);
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
            if (validationException != null)
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
        }
    }


}

