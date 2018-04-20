package demo.app.adcharge.eu.adchargedemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import demo.app.adcharge.eu.adchargedemo.databinding.ActivitySmallBannerBinding;
import demo.app.adcharge.eu.adchargedemo.util.SessionHolder;
import eu.adcharge.api.AdCharge;
import eu.adcharge.api.ApiException;
import eu.adcharge.api.ApiValidationException;
import eu.adcharge.api.entities.AdSession;
import eu.adcharge.api.entities.Feedback;

public class SmallBannerActivity extends AppCompatActivity {

    private ActivitySmallBannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_small_banner);
        binding.setTasksInProgress(0);
        Bundle bundle = getIntent().getExtras();
        String sessionId = bundle.getString("session");
        AdSession session = SessionHolder.read(sessionId);
        binding.setSession(session);
        binding.smallImage.setImageBitmap(session.getSmallBanner());
        new FeedbackTask(Feedback.NO_PRESS).execute();
    }

    public void finishCall(View v) {
        Intent smallBannerActivity = new Intent(getApplicationContext(), LargeBannerActivity.class);
        smallBannerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        smallBannerActivity.putExtras(getIntent().getExtras());
        startActivity(smallBannerActivity);
    }

    private class FeedbackTask extends AsyncTask<String, Void, Void> {
        private final Feedback feedback;
        private ApiValidationException validationException;

        public FeedbackTask(Feedback feedback) {
            this.feedback = feedback;
        }

        @Override
        protected Void doInBackground(String... strings) {
            binding.setTasksInProgress(binding.getTasksInProgress() + 1);
            try {
                AdCharge.feedback(binding.getSession(), feedback);
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
                binding.setError("error");
            } else {
                Toast.makeText(getApplicationContext(), "feedback " + feedback.toString() + " sent", Toast.LENGTH_SHORT).show();
            }
            binding.setTasksInProgress(binding.getTasksInProgress() - 1);
        }
    }

}
