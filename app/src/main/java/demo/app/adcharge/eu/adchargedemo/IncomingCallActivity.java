package demo.app.adcharge.eu.adchargedemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;

import demo.app.adcharge.eu.adchargedemo.databinding.ActivityIncomingCallBinding;
import demo.app.adcharge.eu.adchargedemo.util.SessionHolder;
import eu.adcharge.api.AdCharge;
import eu.adcharge.api.ApiException;
import eu.adcharge.api.ApiValidationException;
import eu.adcharge.api.SessionInvalidatedException;
import eu.adcharge.api.entities.AdSession;

public class IncomingCallActivity extends AppCompatActivity {

    private ActivityIncomingCallBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_call);
        binding.setTasksInProgress(0);
        Bundle bundle = getIntent().getExtras();
        String sessionId = bundle.getString("session");
        AdSession session = SessionHolder.read(sessionId);
        binding.setSession(session);
    }

    public void getIncomingCall(View v) {
        binding.setCall(true);
    }

    public void checkSession(View v) {
        new CheckSessionTask().execute();
    }

    private class CheckSessionTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            binding.setTasksInProgress(binding.getTasksInProgress() + 1);
            try {
                AdCharge.validateSession(binding.getSession());
                Intent smallBannerActivity = new Intent(getApplicationContext(), SmallBannerActivity.class);
                smallBannerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                smallBannerActivity.putExtras(getIntent().getExtras());
                startActivity(smallBannerActivity);
            } catch (IOException e) {
                binding.setError(e.getMessage());
            } catch (ApiException e) {
                binding.setError(e.getMessage());
            } catch (ApiValidationException e) {
                binding.setError("error");
            } catch (SessionInvalidatedException e) {
                Intent profileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
                profileActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(profileActivity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            binding.setTasksInProgress(binding.getTasksInProgress() - 1);
        }
    }
}
