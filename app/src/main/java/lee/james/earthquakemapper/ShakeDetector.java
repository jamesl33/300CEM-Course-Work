package lee.james.earthquakemapper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Class which utilises the devices accelerometer to determine if the phone has been shaken
 */
public class ShakeDetector implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 2.5f;
    private static final int SHAKE_RESET_THRESHOLD = 1000;

    private OnShakeListener mShakeListener;
    private long mLastShake = System.currentTimeMillis();

    /**
     * Allow the setting of a function which will run when the device is shaken
     *
     * @param listener - The function run when the user shakes the device
     */
    void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }

    /**
     * Callback run when the sensor value changes
     * @param event - The shake event. In this case values from the accelerometer
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mShakeListener != null) {
            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > ShakeDetector.SHAKE_THRESHOLD) {
                long currentTimeMillis = System.currentTimeMillis();

                if (currentTimeMillis > mLastShake + ShakeDetector.SHAKE_RESET_THRESHOLD) {
                    mLastShake = currentTimeMillis;
                    mShakeListener.onShake();
                }
            }
        }
    }

    /**
     * Empty function to satisfy Java
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing...
    }

    /**
     * Interface so that other classes can implement the onShake() action
     */
    public interface OnShakeListener {
        void onShake();
    }

}
