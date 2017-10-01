/**
 * Created by aphoe on 9/30/2017.
 */

import android.support.multidex.MultiDexApplication;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.config.AWSConfiguration;


/**
 * Application class responsible for initializing singletons and other common components.
 */
public class Application extends MultiDexApplication {
    private static final String LOG_TAG = Application.class.getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();
        initializeApplication();

    }

    private void initializeApplication() {

       AWSConfiguration awsConfiguration = new AWSConfiguration(getApplicationContext());

       // If IdentityManager is not created, create it
       if (IdentityManager.getDefaultIdentityManager() == null) {
               IdentityManager identityManager =
                    new IdentityManager(getApplicationContext(), awsConfiguration);
               IdentityManager.setDefaultIdentityManager(identityManager);
       }

    }
}