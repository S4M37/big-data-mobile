package gl4.insat.tn.bigdatamobile.widgets;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

import gl4.insat.tn.bigdatamobile.R;
import gl4.insat.tn.bigdatamobile.entities.StateObject;

public class ChartDialog extends CoordinatorLayout {
    private Context context;
    private View rootView;
    private boolean isOpen;
    private CombinedChart chart;
    private ProgressBar progress;

    private List<StateObject> chartObjects;

    public ChartDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ChartDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ChartDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.chart_dialog_layout, this, true);
        rootView = findViewById(R.id.root_view);
        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeView();
            }
        });
        //Comment state

        chart = (CombinedChart) rootView.findViewById(R.id.chart);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        chartObjects = new ArrayList<>();

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDoubleTapToZoomEnabled(false);

        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return chartObjects.get((int) value).getDate();
            }
        });

        chart.getAxisLeft().setDrawGridLines(false);

        chart.getLegend().setEnabled(true);
    }

    public void openView() {
        if (isOpen) {
            return;
        }

        rootView.setVisibility(VISIBLE);
        isOpen = true;

        setChartData();
    }

    public void closeView() {
        if (!isOpen) {
            return;
        }
        rootView.setVisibility(GONE);
        isOpen = false;
    }


    public void setChartData() {
        List<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            StateObject stateObject = new StateObject();
            int value = (int) (Math.random() * 90) + 10;
            stateObject.setNbre(value);
            if (i < 10) {
                stateObject.setDate("0" + i + ":00");
            } else {
                stateObject.setDate(i + ":00");
            }
            chartObjects.add(stateObject);
            chartEntries.add(new BarEntry(i, stateObject.getNbre()));
        }

        LineData lineData = new LineData();

        LineDataSet set = new LineDataSet(chartEntries, "Estimated Traffic intensity");
        set.setColor(Color.parseColor("#feda3d"));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.parseColor("#feda3d"));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(0, 0, 0));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineData.addDataSet(set);
        CombinedData data = new CombinedData();
        data.setData(lineData);
        data.setDrawValues(true);
        data.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Math.round(value) + "";
            }
        });
        chart.setData(data);
        chart.invalidate();
        // add a nice and smooth animation
        chart.animateY(1000);
        chart.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }
}
