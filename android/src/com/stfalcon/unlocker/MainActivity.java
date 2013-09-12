package com.stfalcon.unlocker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.stfalcon.unlocker.LockGraphView.DataSeries;
import com.stfalcon.unlocker.LockGraphView.GraphData;
import com.stfalcon.unlocker.LockGraphView.LockGraphView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    public static final String MY_PREF = "my_pref";
    SensorManager sensorManager = null;
    LinearLayout ll_graph;
    LockGraphView lockGraphView;
    private View ll_record;
    private View ll_start_stop;
    private View rl_create;
    private TextView tv_recording;
    private TextView tv_new_gesture;
    private TextView tv_tap_to;
    private TextView tv_tap_to_new;

    boolean isGestureRecord = false;
    boolean isStartRecording;
    boolean isConfirmGesture;
    boolean isValidation;
    boolean isGestureCorrect;
    boolean isTryAgain;

    double startTime;
    boolean isSensorOn = false;
    boolean isPressed = false;
    boolean toConfirm = false;
    boolean isGestureNotCorrect;
    final ArrayList<Double> masShow = new ArrayList<Double>();
    final ArrayList<Double> masSave = new ArrayList<Double>();
    final ArrayList<Double> masConfirm = new ArrayList<Double>();
    final ArrayList<double[]> accDataList = new ArrayList<double[]>();
    final ArrayList<double[]> gyrDataList = new ArrayList<double[]>();
    ArrayList<double[]> filterDataList = new ArrayList<double[]>();
    private Switch on_off;
    private Activity context;
    private boolean isCheckGesture;
    private final int viewGraphMassSize = 50;
    Typeface robotoThin;
    private View.OnClickListener clickListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        clickListener = this;
        initView();
        initGraph();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        startService(new Intent(this, LockService.class));
        viewToNotCreated();
    }

    /**
     * Инициализация всех view
     */
    public void initView() {
        robotoThin = Typeface.createFromAsset(this.getAssets(), "Roboto-Thin.ttf");
        ll_record = findViewById(R.id.ll_new_gesture);
        ll_start_stop = findViewById(R.id.ll_record_gesture);
        ll_graph = (LinearLayout) findViewById(R.id.ll_graph_view);
        rl_create = findViewById(R.id.rl_create_gesture);
        tv_tap_to_new = (TextView) findViewById(R.id.tv_main_tap_to_new);
        tv_tap_to = (TextView) findViewById(R.id.tv_main_tap_to);
        tv_new_gesture = (TextView) findViewById(R.id.tv_main_new_gesture);
        tv_new_gesture.setTypeface(robotoThin);
        tv_recording = (TextView) findViewById(R.id.tv_main_recording);
        tv_recording.setTypeface(robotoThin);
        on_off = (Switch) findViewById(R.id.switch_main);
        ll_record.setOnClickListener(this);
        ll_start_stop.setOnClickListener(this);
        findViewById(R.id.ll_copy_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = "http://www.stfalcon.com";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Заполнение массива нулевыми значениями для отображения прямой линии на графике
     */
    public void initMass() {
        masShow.clear();
        for (int i = 0; i < viewGraphMassSize; i++) {
            masShow.add(0.0);
        }
    }

    /**
     * Инициализация поля отображающего графики
     */
    public void initGraph() {
        lockGraphView = new LockGraphView(this);
        lockGraphView.setMaxMin(50, 2.5, 0, -2.5);
    }

    private void viewToRecordGesture() {
        rl_create.setVisibility(View.GONE);
        ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_start_stop.setVisibility(View.VISIBLE);
        ll_start_stop.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue));
        tv_recording.setText(getString(R.string.main_label_recording));
        tv_tap_to.setText(getString(R.string.label_tap_to_stop));
    }

    private void viewToConfirmGesture() {
        tv_recording.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tv_recording.setText(getString(R.string.label_main_confirm_gesture));
        tv_tap_to.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tv_tap_to.setText(getString(R.string.label_tap_start));
    }

    private void viewToValidating() {
        tv_recording.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tv_recording.setText(getString(R.string.label_main_validating));
        tv_tap_to.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tv_tap_to.setText(getString(R.string.label_tap_to_stop));
        ll_start_stop.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue));
    }

    private void viewToCheckGesture() {
        rl_create.setVisibility(View.GONE);
        rl_create.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue));
        ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_start_stop.setVisibility(View.VISIBLE);
        tv_recording.setText(getString(R.string.label_main_check));
        tv_tap_to.setText(getString(R.string.label_tap_start));
    }

    private void viewToJustCreated() {
        rl_create.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        rl_create.setVisibility(View.VISIBLE);
        ll_start_stop.setVisibility(View.GONE);
        tv_new_gesture.setText(getString(R.string.label_main_just_created));
        tv_tap_to_new.setVisibility(View.GONE);
        rl_create.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_green));
        ll_record.setBackgroundDrawable(null);
        on_off.setChecked(true);
        ll_record.setOnClickListener(null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isGestureRecord = true;
                ll_record.setOnClickListener(clickListener);
                rl_create.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                tv_new_gesture.setText(getString(R.string.label_new_gesture));
                rl_create.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue));
                ll_record.setBackgroundDrawable(getResources().getDrawable(R.drawable.gesture_stroke_selector));
                on_off.setEnabled(true);
            }
        }, 3000);

    }

    private void viewToNotCreated() {
        rl_create.setVisibility(View.VISIBLE);
        ll_start_stop.setVisibility(View.GONE);
        tv_new_gesture.setText(getString(R.string.label_new_gesture));
        rl_create.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_record.setBackgroundDrawable(getResources().getDrawable(R.drawable.gesture_stroke_selector));
        tv_tap_to_new.setVisibility(View.VISIBLE);
    }

    private void viewToNotCorrect() {
        tv_recording.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tv_recording.setText(getString(R.string.label_main_gesture_not_correct));
        tv_tap_to.setText(getString(R.string.label_tap_try_again));
        ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_start_stop.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_red));
    }

    private void viewToTryAgain() {
        tv_recording.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tv_recording.setText(getString(R.string.labal_try_again));
        tv_tap_to.setText(getString(R.string.label_tap_try_again));
        ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_start_stop.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_red));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_new_gesture:
                if (!isSensorOn) {
                    startNewGesture();
                    viewToRecordGesture();
                    isStartRecording = true;
                }
                break;
            case R.id.ll_record_gesture:
                if (isStartRecording) {
                    if (stopRecording(true)) {
                        viewToConfirmGesture();
                        isConfirmGesture = true;
                    } else {
                        viewToTryAgain();
                        isTryAgain = true;
                    }
                    isStartRecording = false;
                    isGestureRecord = false;
                    return;
                }
                if (isConfirmGesture) {
                    startConfirmGesture();
                    viewToValidating();
                    isConfirmGesture = false;
                    isValidation = true;
                    return;
                }
                if (isValidation) {
                    isCheckGesture = false;
                    isValidation = false;
                    if (stopConfirm()) {
                        isGestureCorrect = true;
                        viewToJustCreated();
                    } else {
                        isGestureNotCorrect = true;
                        viewToNotCorrect();
                    }
                    return;
                }
                if (isGestureNotCorrect) {
                    startConfirmGesture();
                    viewToValidating();
                    isConfirmGesture = false;
                    isValidation = true;
                    isGestureNotCorrect = false;
                    return;
                }
                if (isCheckGesture) {
                    isCheckGesture = false;
                    isConfirmGesture = false;
                    startConfirmGesture();
                    viewToValidating();
                    isValidation = true;
                    return;
                }
                if (isTryAgain) {
                    isTryAgain = false;
                    startNewGesture();
                    viewToRecordGesture();
                    isStartRecording = true;
                }
                break;

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        UnlockApp.sPref = getSharedPreferences(MY_PREF, 0);
        final boolean isSave = UnlockApp.sPref.getBoolean("isSave", false);
        if (isSave) {
            on_off.setEnabled(true);
        } else {
            on_off.setEnabled(false);
        }
        if (UnlockApp.prefs.getString(UnlockApp.IS_ON, "false").equals("true")) {
            on_off.setChecked(true);
        }
        //Инициализация кнопки включения/выключения
        on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UnlockApp.getInstance().saveActivState(isChecked);
                final boolean isSaveGesture = UnlockApp.sPref.getBoolean("isSave", false);
                if (isChecked) {
                    if (isSaveGesture) {
                        if (!isGestureCorrect) {
                            on_off.setEnabled(true);
                            viewToCheckGesture();
                            CheckGesture();
                            isConfirmGesture = true;
                            isCheckGesture = true;
                        }
                    } else {
                        on_off.setChecked(false);
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    /**
     * Метод возвращающий значения сенсора, который был задействован
     *
     * @param event
     */

    public void onSensorChanged(final SensorEvent event) {
        synchronized (this) {
            {
                if (isSensorOn) {
                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_ACCELEROMETER:
                            double[] accData = Comparison.lowFilter(event.values[0], event.values[1], event.values[2]);
                            accDataList.add(accData);
                            break;
                        case Sensor.TYPE_GYROSCOPE:
                            double[] gyrData = Comparison.lowFilter(event.values[0], event.values[1], event.values[2]);
                            gyrDataList.add(gyrData);
                            getPoint(event.values[0], event.values[1], event.values[2]);
                            break;
                    }
                }

            }
        }
    }

    /**
     * Стартует запись нового жеста
     */
    public void startNewGesture() {
        initMass();
        startTime = System.currentTimeMillis();
        accDataList.clear();
        gyrDataList.clear();
        filterDataList.clear();
        isSensorOn = true;
        masConfirm.clear();
        masSave.clear();
    }

    /**
     * Останавливает запись нового жеста
     */
    public boolean stopRecording(boolean toSave) {
        isSensorOn = false;
        if (toSave) {
            SharedPreferences.Editor editor = UnlockApp.sPref.edit();
            editor.putBoolean("isSave", false);
            editor.commit();
            if (validating()) {
                masConfirm.addAll(masSave);
                return true;
            } else {
                return false;
            }
        }
        masSave.clear();
        return false;
    }

    /**
     * Стартует запись подтверждающего жеста
     */
    public void startConfirmGesture() {
        initMass();
        startTime = System.currentTimeMillis();
        accDataList.clear();
        gyrDataList.clear();
        filterDataList.clear();
        isSensorOn = true;
        toConfirm = true;
        ll_graph.removeAllViews();
    }

    /**
     * Останавливает запись подтверждающего жеста
     */
    public boolean stopConfirm() {
        isSensorOn = false;
        return validating();
    }

    /**
     * Сравнивает записанные жесты
     */
    public boolean validating() {
        boolean isValid = false;
        ll_graph.removeAllViews();
        DataSeries rollSaveDataSeries = null;
        filterDataList = filterData();
        double[] pArr = new double[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            pArr[i] = filterDataList.get(i)[0];
        }
        double[] rArr = new double[filterDataList.size()];
        for (int i = 0; i < filterDataList.size(); i++) {
            rArr[i] = filterDataList.get(i)[1];
        }
        List<double[]> pList = Comparison.prepareArrays(pArr, rArr);
        if (pList == null) {
            toConfirm = false;
            isStartRecording = false;
            isGestureRecord = false;
            return false;
        }
        pArr = pList.get(0);
        rArr = pList.get(1);
        GraphData[] rollGraphViewData = new GraphData[rArr.length];
        for (int i = 0; i < rArr.length; i++) {
            rollGraphViewData[i] = new GraphData(i, rArr[i]);
        }
        DataSeries rollDataSeries;
        if (isStartRecording) {
            rollDataSeries = new DataSeries(rollGraphViewData);
            rollDataSeries.setLineStyle(getResources().getColor(R.color.green_line), getResources().getDimension(R.dimen.line_width));
        } else {
            rollDataSeries = new DataSeries(rollGraphViewData);
            rollDataSeries.setLineStyle(getResources().getColor(R.color.white_line), getResources().getDimension(R.dimen.line_width));
        }

        boolean isSave = UnlockApp.sPref.getBoolean("isSave", false);

        if (isSave) {
            double saveRoll[] = UnlockApp.loadArrays().get(1);
            GraphData[] rollGraphViewSaveData = new GraphData[saveRoll.length];
            for (int i = 0; i < saveRoll.length; i++) {
                rollGraphViewSaveData[i] = new GraphData(i, saveRoll[i]);
            }
            rollSaveDataSeries = new DataSeries(rollGraphViewSaveData);
            rollSaveDataSeries.setLineStyle(getResources().getColor(R.color.yellow_line), getResources().getDimension(R.dimen.line_width));
        }
        LockGraphView graphView = new LockGraphView(this);
        graphView.setMaxMin(0.0008, -0.0008);
        graphView.setData(new DataSeries[]{rollDataSeries}); // data

        if (isSave) {
            double savePitch[] = UnlockApp.loadArrays().get(0);
            double saveRoll[] = UnlockApp.loadArrays().get(1);
            graphView.setMaxMin(0.0008, -0.0008);
            graphView.setData(new DataSeries[]{rollDataSeries, rollSaveDataSeries});
            double[] x = pArr;
            double[] x1 = savePitch;
            double[] y = rArr;
            double[] y1 = saveRoll;

            double xPirsonKoef = Comparison.pirsonCompare(x, x1);
            double yPirsonKoef = Comparison.pirsonCompare(y, y1);
            UnlockApp.FACTOR factor = UnlockApp.getInstance().getFactors();
            boolean unlock = (xPirsonKoef + yPirsonKoef >= factor.getFactor())
                    && ((xPirsonKoef > factor.getPitchFactor() && yPirsonKoef > factor.getRollFactor())
                    || (yPirsonKoef > factor.getPitchFactor() && xPirsonKoef > factor.getRollFactor()));
            if (toConfirm && unlock) {
                UnlockApp.confArrays(pArr, rArr);
                isValid = true;
            }
        }
        ll_graph.addView(graphView);
        if (!isSave) {
            UnlockApp.saveArrays(pArr, rArr);
            isValid = true;
        }
        toConfirm = false;
        return isValid;
    }

    public void CheckGesture() {
        showSaveGraph();
    }

    /**
     * Создает точку из данных полученых от гироскопа и записывает её в массив
     *
     * @param x
     * @param y
     * @param z
     */
    public void getPoint(Float x, Float y, Float z) {
        double acc = x + y + z;
        double point = UnlockApp.lowPassFilterAcc(acc);
        masShow.add(point);
        masSave.add(point);
        if (masShow.size() > viewGraphMassSize) {
            masShow.remove(0);
        }
        showOnGraph(masShow);
    }

    /**
     * Отображает простой график в реальном времени без применения фильтров
     *
     * @param mas
     */
    private void showOnGraph(ArrayList<Double> mas) {
        ll_graph.removeAllViews();
        GraphData[] accGraphViewSaveData = new GraphData[mas.size()];
        for (int i = 0; i < mas.size(); i++) {
            accGraphViewSaveData[i] = new GraphData(i, mas.get(i));
        }
        DataSeries[] dataSerieses = new DataSeries[1];
        dataSerieses[0] = new DataSeries(accGraphViewSaveData);
        dataSerieses[0].setLineStyle(getResources().getColor(R.color.green_line), 4);
        lockGraphView.setData(dataSerieses);
        ll_graph.addView(lockGraphView);
    }

    /**
     * Применяет фильтр подавляющий шумы
     *
     * @return
     */
    public ArrayList<double[]> filterData() {
        ArrayList<double[]> filterData = new ArrayList<double[]>();
        int len;
        if (accDataList.size() > gyrDataList.size()) len = gyrDataList.size();
        else len = accDataList.size();
        for (int i = 0; i < len; i++) {
            filterData.add(UnlockApp.complementaryFilter(accDataList.get(i), gyrDataList.get(i)));
        }
        return filterData;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Опображает график сохраненный в Preferences
     */
    private void showSaveGraph() {
        DataSeries rollSaveDataSeries = null;
        ll_graph.removeAllViews();
        double[] saveRoll = UnlockApp.loadArrays().get(1);

        GraphData[] rollGraphViewSaveData = new GraphData[saveRoll.length];
        for (int i = 0; i < saveRoll.length; i++) {
            rollGraphViewSaveData[i] = new GraphData(i, saveRoll[i]);
        }
        LockGraphView graphSaveView = new LockGraphView(this);
        rollSaveDataSeries = new DataSeries(rollGraphViewSaveData);
        rollSaveDataSeries.setLineStyle(getResources().getColor(R.color.white_line), getResources().getDimension(R.dimen.line_width));
        graphSaveView.setBackgroundDrawable(null);
        graphSaveView.setMaxMin(0.0008, -0.0008);
        graphSaveView.setData(new DataSeries[]{rollSaveDataSeries});
        ll_graph.addView(graphSaveView);
    }

    @Override
    public void onBackPressed() {
        if (isGestureRecord) {
            super.onBackPressed();
        }
        if (isStartRecording || isGestureNotCorrect || isConfirmGesture || isValidation || isCheckGesture) {
            viewToNotCreated();
            stopRecording(false);
            isStartRecording = false;
            isGestureNotCorrect = false;
            isConfirmGesture = false;
            isValidation = false;
            isCheckGesture = false;
            if (!isGestureRecord) {
                on_off.setChecked(false);
            }
            isGestureRecord = false;
            return;
        }
        super.onBackPressed();
    }
}

