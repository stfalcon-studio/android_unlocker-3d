package com.stfalcon.unlocker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    public static String MY_PREF = "my_pref";
    SensorManager sensorManager = null;
    LinearLayout ll_graph;
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

    TextView proc;
    GraphViewSeries pitchsaveDataSeries, rollsaveDataSeries;
    GraphView graphView;
    double startTime;
    boolean isSensorOn = false;
    boolean isPressed = false;
    boolean toConfirm = false;
    boolean isGestureNotCorrect;
    ArrayList<Double> masShow = new ArrayList<Double>();
    ArrayList<Double> masSave = new ArrayList<Double>();
    ArrayList<Double> masConfirm = new ArrayList<Double>();
    ArrayList<double[]> accDataList = new ArrayList<double[]>();
    ArrayList<double[]> gyrDataList = new ArrayList<double[]>();
    ArrayList<double[]> filterDataList = new ArrayList<double[]>();
    private Switch on_off;
    private Activity context;
    private boolean isCheckGesture;

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

        /*//Quality
        int quality = 0;
        if (UnlockApp.sPref.contains("quality")) {
            quality = (UnlockApp.sPref.getInt("quality", R.id.rb_hard));
        } else {
            quality = (UnlockApp.sPref.getInt("quality", R.id.rb_hard));
            SharedPreferences.Editor editor = UnlockApp.sPref.edit();
            editor.putInt("quality", R.id.rb_hard);
            editor.commit();
        }*/
    }


    /**
     * Инициализация всех view
     */
    public void initView() {
        ll_record = findViewById(R.id.ll_new_gesture);
        ll_start_stop = findViewById(R.id.ll_record_gesture);
        ll_graph = (LinearLayout) findViewById(R.id.ll_graph_view);
        rl_create = findViewById(R.id.rl_create_gesture);
        tv_tap_to_new = (TextView) findViewById(R.id.tv_main_tap_to_new);
        tv_tap_to = (TextView) findViewById(R.id.tv_main_tap_to);
        tv_new_gesture = (TextView) findViewById(R.id.tv_main_new_gesture);
        tv_recording = (TextView) findViewById(R.id.tv_main_recording);
        on_off = (Switch) findViewById(R.id.switch_main);
        ll_record.setOnClickListener(this);
        ll_start_stop.setOnClickListener(this);
    }

    /**
     * Заполнение массива нулевыми значениями для отображения прямой линии на графике
     */
    public void initMass() {
        masShow.clear();
        for (int i = 0; i < 200; i++) {
            masShow.add(0.0);
        }
    }

    /**
     * Инициализация поля отображающего графики
     */
    public void initGraph() {
        graphView = new LineGraphView(this, "Saved");
        graphView.setBackground(null);
        graphView.getGraphViewStyle().setGridColor(getResources().getColor(android.R.color.transparent));
        graphView.getGraphViewStyle().setHorizontalLabelsColor(getResources().getColor(android.R.color.transparent));
        graphView.getGraphViewStyle().setNumHorizontalLabels(0);
        graphView.getGraphViewStyle().setNumVerticalLabels(0);
        graphView.getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(android.R.color.transparent));
        graphView.getGraphViewStyle().setVerticalLabelsWidth(0);
        graphView.setPadding(-50, 0, 0, 0);
        graphView.getGraphViewStyle().setTextSize(0);
        graphView.setScalable(true);
        graphView.setManualYAxisBounds(1.0, -1.0);
    }

    private void viewToRecordGesture() {
        rl_create.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        rl_create.setVisibility(View.GONE);
        //ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_start_stop.setVisibility(View.VISIBLE);
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
        ll_start_stop.setBackground(getResources().getDrawable(R.drawable.bg_blue));
    }

    private void viewToCheckGesture() {
        rl_create.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        rl_create.setVisibility(View.GONE);
        ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_start_stop.setVisibility(View.VISIBLE);
        tv_recording.setText(getString(R.string.label_main_check));
        tv_tap_to.setText(getString(R.string.label_tap_start));

    }

    private void viewToJustCreated() {
        rl_create.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        rl_create.setVisibility(View.VISIBLE);
        ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        ll_start_stop.setVisibility(View.GONE);
        tv_new_gesture.setText(getString(R.string.label_main_just_created));
        tv_tap_to_new.setVisibility(View.GONE);
        rl_create.setBackground(getResources().getDrawable(R.drawable.bg_green));
        ll_record.setBackground(null);
        on_off.setChecked(true);
        ll_record.setOnClickListener(null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ll_record.setOnClickListener(clickListener);
                rl_create.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                rl_create.setBackground(getResources().getDrawable(R.drawable.bg_blue));
                ll_record.setBackground(getResources().getDrawable(R.drawable.gesture_stroke_selector));
            }
        }, 3000);

    }

    private void viewToNotCreated() {
        rl_create.setVisibility(View.VISIBLE);
        ll_start_stop.setVisibility(View.GONE);
        tv_new_gesture.setText(getString(R.string.label_new_gesture));
        rl_create.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_record.setBackground(getResources().getDrawable(R.drawable.gesture_stroke_selector));
        tv_tap_to_new.setVisibility(View.VISIBLE);
    }

    private void viewToNotCorrect() {
        tv_recording.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tv_recording.setText(getString(R.string.label_main_gesture_not_correct));
        tv_tap_to.setText(getString(R.string.label_tap_try_again));
        ll_start_stop.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        ll_start_stop.setBackground(getResources().getDrawable(R.drawable.bg_red));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_new_gesture:
                if (!isSensorOn) {
                    if (!isGestureRecord) {
                        startNewGesture();
                        viewToRecordGesture();
                        isStartRecording = true;
                    }
                }
                break;
            case R.id.ll_record_gesture:
                if (isStartRecording) {
                    stopRecording();
                    viewToConfirmGesture();
                    isStartRecording = false;
                    isConfirmGesture = true;
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
                    if (stopConfirm()) {
                        viewToJustCreated();
                        isGestureCorrect = true;
                    } else {
                        viewToNotCorrect();
                        isGestureNotCorrect = true;
                    }

                    isValidation = false;
                    //isGestureRecord = true;
                    return;
                }
                if (isGestureRecord) {
                    viewToCheckGesture();
                    startConfirmGesture();
                    viewToValidating();
                    isConfirmGesture = false;
                    isValidation = true;

                    return;
                }
                if (isGestureNotCorrect) {
                    startConfirmGesture();
                    viewToValidating();
                    isConfirmGesture = false;
                    isValidation = true;
                    return;
                }
                if (isCheckGesture) {
                    isCheckGesture = false;
                    startConfirmGesture();
                    viewToValidating();
                    isValidation = true;
                    return;
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
            Log.i("Loger", "IS_ON = " + UnlockApp.prefs.getString(UnlockApp.IS_ON, "false"));
        }
        //Инициализация кнопки включения/выключения
        on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UnlockApp.getInstance().saveActivState(isChecked);
                if (isChecked) {
                    if (isSave) {
                        if (isGestureRecord) {
                            viewToCheckGesture();
                            CheckGesture();
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

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    public void onSensorChanged(SensorEvent event) {
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
        isPressed = true;
        masConfirm.clear();
        masSave.clear();
    }

    /**
     * Останавливает запись нового жеста
     */
    public void stopRecording() {
        isSensorOn = false;
        isPressed = false;
        SharedPreferences.Editor editor = UnlockApp.sPref.edit();
        editor.putBoolean("isSave", false);
        editor.commit();
        validating();
        masConfirm.addAll(masSave);
        masSave.clear();
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
        isPressed = true;
        toConfirm = true;
        ll_graph.removeAllViews();
    }

    /**
     * Останавливает запись подтверждающего жеста
     */
    public boolean stopConfirm() {
        isSensorOn = false;
        isPressed = false;
        return validating();
    }

    /**
     * Сравнивает записанные жесты
     */
    public boolean validating() {
        boolean isValid = false;
        ll_graph.removeAllViews();

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
            Toast.makeText(context, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
            return false;
        }
        pArr = pList.get(0);
        rArr = pList.get(1);
        GraphView.GraphViewData[] pitchGraphViewData = new GraphView.GraphViewData[pArr.length];
        for (int i = 0; i < pArr.length; i++) {
            pitchGraphViewData[i] = new GraphView.GraphViewData(i, pArr[i]);
        }
        GraphView.GraphViewData[] rollGraphViewData = new GraphView.GraphViewData[rArr.length];
        for (int i = 0; i < rArr.length; i++) {
            rollGraphViewData[i] = new GraphView.GraphViewData(i, rArr[i]);
        }
        GraphViewSeries rollDataSeries = null;
        if (isStartRecording) {
            rollDataSeries = new GraphViewSeries("roll",
                    new GraphViewSeries.GraphViewSeriesStyle(
                            getResources().getColor(R.color.green_line),
                            getResources().getDimensionPixelSize(R.dimen.line_width)), rollGraphViewData);
        } else {
            rollDataSeries = new GraphViewSeries("roll",
                    new GraphViewSeries.GraphViewSeriesStyle(
                            getResources().getColor(R.color.white_line),
                            getResources().getDimensionPixelSize(R.dimen.line_width)), rollGraphViewData);
        }

        boolean isSave = UnlockApp.sPref.getBoolean("isSave", false);

        if (isSave) {
            double savePitch[] = UnlockApp.loadArrays().get(0);
            double saveRoll[] = UnlockApp.loadArrays().get(1);
            GraphView.GraphViewData[] pitchGraphViewsaveData = new GraphView.GraphViewData[savePitch.length];
            for (int i = 0; i < savePitch.length; i++) {
                pitchGraphViewsaveData[i] = new GraphView.GraphViewData(i, savePitch[i]);
            }
            GraphView.GraphViewData[] rollGraphViewsaveData = new GraphView.GraphViewData[saveRoll.length];
            for (int i = 0; i < saveRoll.length; i++) {
                rollGraphViewsaveData[i] = new GraphView.GraphViewData(i, saveRoll[i]);
            }
            rollsaveDataSeries = new GraphViewSeries("roll1", new GraphViewSeries.GraphViewSeriesStyle(
                    getResources().getColor(R.color.yellow_line),
                    getResources().getDimensionPixelSize(R.dimen.line_width)), rollGraphViewsaveData);
        }

        GraphView graphView = new LineGraphView(
                this // context
                , "New Gesture" // heading
        );
        graphView.setBackground(null);
        graphView.getGraphViewStyle().setGridColor(getResources().getColor(android.R.color.transparent));
        graphView.getGraphViewStyle().setHorizontalLabelsColor(getResources().getColor(android.R.color.transparent));
        graphView.getGraphViewStyle().setNumHorizontalLabels(0);
        graphView.getGraphViewStyle().setNumVerticalLabels(0);
        graphView.getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(android.R.color.transparent));
        graphView.getGraphViewStyle().setVerticalLabelsWidth(0);
        graphView.setPadding(-50, 0, 0, 0);
        graphView.getGraphViewStyle().setTextSize(0);
        //graphView.addSeries(pitchDataSeries); // data
        graphView.addSeries(rollDataSeries); // data
        if (isSave) {
            double savePitch[] = UnlockApp.loadArrays().get(0);
            double saveRoll[] = UnlockApp.loadArrays().get(1);
            graphView.addSeries(rollsaveDataSeries);
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
        if (masShow.size() > 200) {
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
        GraphView.GraphViewData[] accGraphViewsaveData = new GraphView.GraphViewData[mas.size()];
        for (int i = 0; i < mas.size(); i++) {
            accGraphViewsaveData[i] = new GraphView.GraphViewData(i, mas.get(i));
        }
        GraphViewSeries accGraphViewSeries1 = new GraphViewSeries("acc", new GraphViewSeries.GraphViewSeriesStyle(Color.BLACK, 4), accGraphViewsaveData);
        GraphViewSeries accGraphViewSeries = new GraphViewSeries("acc",
                new GraphViewSeries.GraphViewSeriesStyle(getResources().getColor(R.color.green_line),
                        getResources().getDimensionPixelSize(R.dimen.line_width)), accGraphViewsaveData);

        graphView.removeAllSeries();
        graphView.addSeries(accGraphViewSeries1);
        graphView.addSeries(accGraphViewSeries);
        ll_graph.addView(graphView);
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
        double[] savePitch = UnlockApp.loadArrays().get(0);
        double[] saveRoll = UnlockApp.loadArrays().get(1);
        GraphView.GraphViewData[] pitchGraphViewsaveData = new GraphView.GraphViewData[savePitch.length];
        for (int i = 0; i < savePitch.length; i++) {
            pitchGraphViewsaveData[i] = new GraphView.GraphViewData(i, savePitch[i]);
        }
        GraphView.GraphViewData[] rollGraphViewsaveData = new GraphView.GraphViewData[saveRoll.length];
        for (int i = 0; i < saveRoll.length; i++) {
            rollGraphViewsaveData[i] = new GraphView.GraphViewData(i, saveRoll[i]);
        }
        rollsaveDataSeries = new GraphViewSeries("roll1", new GraphViewSeries.GraphViewSeriesStyle(
                getResources().getColor(R.color.white_line),
                getResources().getDimensionPixelSize(R.dimen.line_width)), rollGraphViewsaveData);
        GraphView graphView = new LineGraphView(
                this // context
                , "Saved gesture" // heading
        );
        graphView.addSeries(rollsaveDataSeries);
        ll_graph.addView(graphView);
    }


}

