package com.example.chartsapplication;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends AppCompatActivity {

    private LineChart lineChart;
    private List<Entry> entries;
    private List<BarEntry> barEntries;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private CardView cardView;
    private TextView textView;
    private BarChart barChart;
    private BarDataSet barDataSet;
    private BarData barData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardView = (CardView) findViewById(R.id.card_view);
        textView = (TextView) findViewById(R.id.info_text);
        intilaiseLineChart();
        intialiseBarChart();
    }

    public void setDataForLineChart(){
        entries = new ArrayList<Entry>();
        for(int i=0; i< 30; i++){
            int val = ThreadLocalRandom.current().nextInt(0, 30 + 1);
            entries.add(new Entry(i,val));
        }
    }

    public void setDataForBarChart(){
        barEntries = new ArrayList<BarEntry>();
        String[] weekdays = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
        for(int i=0; i< weekdays.length; i++) {
            int val = ThreadLocalRandom.current().nextInt(0, 20 + 1);
            if(!weekdays[i].isEmpty()) {
                barEntries.add(new BarEntry(i,val));
            }
        }
    }

    public void intilaiseLineChart(){
        setDataForLineChart();
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

    public void intialiseBarChart(){
        setDataForBarChart();
        final List<String> weekDays = Arrays.asList("sunday","monday","tuesday","wednesday","thursday","friday","saturday");
        final String[] colours = new String[]{"#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
                "#3B3EAC", "#0099C6", "#DD4477", "#66AA00", "#B82E2E", "#316395", "#994499", "#22AA99",
                "#AAAA11", "#6633CC", "#E67300", "#8B0707", "#329262", "#5574A6", "#3B3EAC"};
        int[] decodedColors = new int[colours.length];
        for(int i=0; i<colours.length; i++){
            int n = Color.parseColor(colours[i]);
            decodedColors[i] = n;
        }
        Log.i("colors",String.valueOf(decodedColors.length));
        Log.i("color red", String.valueOf(Color.RED));
        barChart =(BarChart) findViewById(R.id.bar_chart);
        barDataSet = new BarDataSet(barEntries,"RandomNumbers");   //y axis data, "values"
        barDataSet.setColors(decodedColors);
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        XAxis xAxis = barChart.getXAxis();
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return weekDays.get((int)value);
            }
        };
        xAxis.setValueFormatter(formatter);
        xAxis.setGranularity(1.0f);
        barChart.invalidate();

    }

}

