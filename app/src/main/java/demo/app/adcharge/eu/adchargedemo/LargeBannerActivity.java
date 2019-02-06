package demo.app.adcharge.eu.adchargedemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;

import demo.app.adcharge.eu.adchargedemo.databinding.ActivityLargeBannerBinding;
import demo.app.adcharge.eu.adchargedemo.util.SessionHolder;
import eu.adcharge.api.AdCharge;
import eu.adcharge.api.ApiException;
import eu.adcharge.api.entities.AdSession;
import eu.adcharge.api.entities.Feedback;

public class LargeBannerActivity extends AppCompatActivity {

    private ActivityLargeBannerBinding binding;
    private AdCharge sdk;

    public void openProfile(View view) {
        Intent profileActivity = new Intent(getApplicationContext(), ProfileActivity.class);
        profileActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(profileActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            sdk = new AdCharge(BuildConfig.SERVER_URL, getApplicationContext());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            finish();
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_large_banner);
        binding.setTasksInProgress(0);
        Bundle bundle = getIntent().getExtras();
        String sessionId = bundle.getString("session");
        AdSession session = SessionHolder.read(sessionId);
        binding.setSession(session);
        binding.largeImage.setImageBitmap(session.getBigBanner());
    }

    public void like(View view) {
        new FeedbackTask(Feedback.LIKE).execute();
    }

    public void dislike(View view) {
        new FeedbackTask(Feedback.DISLIKE).execute();
    }

    public void hide(View view) {
        new FeedbackTask(Feedback.HIDE_BANNER).execute();
        openProfile(view);
    }

    public void click(View v) {
        try {
            String url = binding.getSession().getUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.putExtra("android.support.customtabs.extra.SESSION", getPackageName());
            i.putExtra("android.support.customtabs.extra.EXTRA_ENABLE_INSTANT_APPS", true);
            i.setData(Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class FeedbackTask extends AsyncTask<String, Void, Void> {
        private final Feedback feedback;

        public FeedbackTask(Feedback feedback) {
            this.feedback = feedback;
        }

        @Override
        protected Void doInBackground(String... strings) {
            binding.setTasksInProgress(binding.getTasksInProgress() + 1);
            try {
                sdk.feedback(binding.getSession(), feedback);
            } catch (IOException e) {
                binding.setError(e.getMessage());
            } catch (ApiException e) {
                binding.setError(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "feedback " + feedback.toString() + " sent", Toast.LENGTH_SHORT).show();
            binding.setTasksInProgress(binding.getTasksInProgress() - 1);
        }
    }
}
