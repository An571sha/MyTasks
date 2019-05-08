package com.example.chartsapplication;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends AppCompatActivity {

    private LineChart lineChart;
    private List<Entry> entries;
    private LineDataSet lineDataSet;
    private LineData lineData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setData();
        lineChart = (LineChart) findViewById(R.id.chart);
        lineDataSet = new LineDataSet(entries,"random numbers");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setLineWidth(1);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(Color.LTGRAY);
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setFadingEdgeLength(20);
        XAxis xAxis =lineChart.getXAxis();
        YAxis yAxis =lineChart.getAxisRight();

        yAxis.setEnabled(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawGridLinesBehindData(false);

        lineChart.invalidate();

    }

    public void setData(){
        entries = new ArrayList<Entry>();
        for(int i=0; i< 100; i++){
            int val = ThreadLocalRandom.current().nextInt(0, 80 + 1);
            entries.add(new Entry(i,val));
        }

    }

    public int[] setLineColor(){
        int[] colors = new int[50];
        for(int i=0; i<50 ;i++){
            int j = R.color.colorAccent+i;
             j = colors[i];
        }
        return colors;
    }
}
