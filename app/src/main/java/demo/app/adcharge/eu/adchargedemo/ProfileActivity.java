package demo.app.adcharge.eu.adchargedemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import demo.app.adcharge.eu.adchargedemo.databinding.ActivityProfileBinding;
import demo.app.adcharge.eu.adchargedemo.util.DatePicker;
import demo.app.adcharge.eu.adchargedemo.util.DatePicker.OnDateSelectedListener;
import demo.app.adcharge.eu.adchargedemo.util.SessionHolder;
import eu.adcharge.api.AdCharge;
import eu.adcharge.api.ApiException;
import eu.adcharge.api.ApiValidationException;
import eu.adcharge.api.BannersPreloadPolicy;
import eu.adcharge.api.GeoPosition;
import eu.adcharge.api.NoAdvertisementFoundException;
import eu.adcharge.api.entities.AdSession;
import eu.adcharge.api.entities.Gender;
import eu.adcharge.api.entities.User;

public class ProfileActivity extends AppCompatActivity implements OnDateSelectedListener {
    private AdCharge sdk;


    private final int MY_PERMISSIONS_REQUEST = 1;
    private ActivityProfileBinding binding;

    public static String formatDate(Date date) {
        if (date == null) {
            return "undefined";
        }
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

    public void saveUser(View v) {
        new UserSaveTask().execute();
    }

    public void logout(View view) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                sdk.logout();
                Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginActivity);
                return null;
            }
        }.execute();
    }

    public void getSession(View view) {
        new AdSessionGetTask().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            sdk = new AdCharge(BuildConfig.SERVER_URL, getApplicationContext());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            finish();
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
            return;
        }
        binding.setTasksInProgress(0);
        binding.setUser(new User());


        new UserGetTask().execute();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                    finish();
                }
                return;
            }
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = DatePicker.newInstance(binding.getUser().getBirthday(), this);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSelected(Date date) {
        User user = binding.getUser();
        user.setBirthday(date);
        binding.setUser(user);
    }


    public void setMale(View view) {
        User user = binding.getUser();
        user.setGender(Gender.MALE);
        binding.setUser(user);
    }

    public void setFemale(View view) {
        User user = binding.getUser();
        user.setGender(Gender.FEMALE);
        binding.setUser(user);
    }


    private class UserSaveTask extends AsyncTask<String, Void, Void> {
        private ApiValidationException validationException;

        @Override
        protected Void doInBackground(String... strings) {
            binding.setTasksInProgress(binding.getTasksInProgress() + 1);
            try {
                sdk.saveUser(binding.getUser());
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
                    binding.setError(field + ": " + message);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
            binding.setTasksInProgress(binding.getTasksInProgress() - 1);
        }
    }


    private class UserGetTask extends AsyncTask<String, Void, User> {
        private ApiValidationException validationException;

        @Override
        protected User doInBackground(String... strings) {
            try {
                binding.setTasksInProgress(binding.getTasksInProgress() + 1);
                return sdk.getUserInfo();
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
        protected void onPostExecute(User user) {
            if (validationException != null) {
                for (ApiValidationException.Error fieldError : validationException.getFieldErrors()) {
                    String field = fieldError.getField();
                    String message = fieldError.getMessages().isEmpty() ? "" : fieldError.getMessages().get(0);
                    binding.setError(field + ": " + message);
                }
            } else {
                binding.setUser(user);
            }
            binding.setTasksInProgress(binding.getTasksInProgress() - 1);
        }
    }

    private class AdSessionGetTask extends AsyncTask<String, Void, AdSession> {
        private NoAdvertisementFoundException noAdvertisementFoundException;

        @Override
        protected AdSession doInBackground(String... strings) {
            try {
                binding.setTasksInProgress(binding.getTasksInProgress() + 1);
                return sdk.getAdvert(new GeoPosition(binding.getLatitude(), binding.getLongitude()), BannersPreloadPolicy.BOTH_BANNERS_PRELOAD);
            } catch (IOException e) {
                binding.setError(e.getMessage());
            } catch (ApiException e) {
                binding.setError(e.getMessage());
            } catch (ApiValidationException e) {
                binding.setError(e.getMessage());
            } catch (NoAdvertisementFoundException e) {
                noAdvertisementFoundException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(AdSession session) {
            super.onPostExecute(session);
            if (noAdvertisementFoundException != null) {
                binding.setError(noAdvertisementFoundException.getMessage());
            } else if (session != null) {
                Toast.makeText(getApplicationContext(), "Session is preloaded", Toast.LENGTH_SHORT).show();
                SessionHolder.save(session);
                Intent incomingCallActivity = new Intent(getApplicationContext(), IncomingCallActivity.class);
                incomingCallActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                incomingCallActivity.putExtra("session", session.getSession_id());
                startActivity(incomingCallActivity);
            }
            binding.setTasksInProgress(binding.getTasksInProgress() - 1);
        }
    }


    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            binding.setLatitude(loc.getLatitude());
            binding.setLongitude(loc.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

}
