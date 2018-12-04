package lee.james.earthquakemapper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    private static final String LOG_TAG = ShakeDetector.class.getSimpleName();

    private static final float SHAKE_THRESHOLD = 2.5f;
    private static final int SHAKE_RESET_THRESHOLD = 1000;

    private OnShakeListener mShakeListener;
    private long mLastShake = System.currentTimeMillis();

    public void setOnShakeListener(OnShakeListener listener) {
        this.mShakeListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mShakeListener != null) {
            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > ShakeDetector.SHAKE_THRESHOLD) {
                long currentTimeMillis = System.currentTimeMillis();

                if (currentTimeMillis > this.mLastShake + ShakeDetector.SHAKE_RESET_THRESHOLD) {
                    this.mLastShake = currentTimeMillis;
                    this.mShakeListener.onShake();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing...
    }

    public interface OnShakeListener {
        void onShake();
    }

}
