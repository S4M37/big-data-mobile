package gl4.insat.tn.bigdatamobile.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import gl4.insat.tn.bigdatamobile.R;
import gl4.insat.tn.bigdatamobile.config.Const;
import gl4.insat.tn.bigdatamobile.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        showFragment(HomeFragment.newInstance());

        hasPermissions(this, PERMISSIONS);
    }

    public void showFragment(final Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment != null) {
            fragmentTransaction.replace(R.id.main_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    //Request Permissions
    private String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((MainActivity) context, permissions, Const.MY_PERMISSIONS_REQUEST_LOCALISATION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Const.MY_PERMISSIONS_REQUEST_LOCALISATION: {

            }
            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }

}
