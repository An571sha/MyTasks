package com.example.chartsapplication;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends AppCompatActivity {

    private List<Entry> entries;
    private List<BarEntry> barEntries;
    private List<PieEntry> pieEntries;
    private int mTEXT_SIZE = 12;
    private int[] decodedColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDecodedColors();
        initialiseLineChart();
        intialiseBarChart();
        intialisePieChat();

    }

    public void setDataForLineChart(){
        entries = new ArrayList<>();
        for(int i=0; i< 30; i++){
            int val = ThreadLocalRandom.current().nextInt(0, 30 + 1);
            entries.add(new Entry(i,val));
        }
    }

    public void setDataForBarChart(){
        barEntries = new ArrayList<>();
        String[] weekdays = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
//        for(int i=0; i< weekdays.length; i++) {
//            int val = ThreadLocalRandom.current().nextInt(0, 20 + 1);
//            if(!weekdays[i].isEmpty()) {
//                barEntries.add(new BarEntry(i,val));
//            }
//        }

          barEntries.add(new BarEntry(0,10));
          barEntries.add(new BarEntry(1,0));
          barEntries.add(new BarEntry(2,20));
          barEntries.add(new BarEntry(3,30));
          barEntries.add(new BarEntry(4,8));
          barEntries.add(new BarEntry(5,7));
          barEntries.add(new BarEntry(6,11));

    }

    public void setDataforPieChart(){
        pieEntries = new ArrayList<>();
        String[] moods = new String[] {"Awesome","Happy","Neutral","Bad","Awful"};
       for(int i=0; i< moods.length; i++) {
            int val = ThreadLocalRandom.current().nextInt(1, 80);
            if(!moods[i].isEmpty()) {
                pieEntries.add(new PieEntry(val,moods[i]));
            }
        }
//        pieEntries.add(new PieEntry(1,moods[0]));
//        pieEntries.add(new PieEntry(1,moods[1]));
//        pieEntries.add(new PieEntry(1,moods[2]));
//        pieEntries.add(new PieEntry(1,moods[3]));
//        pieEntries.add(new PieEntry(96,moods[4]));



    }
    public void initialiseLineChart(){
        setDataForLineChart();
        LineChart lineChart = findViewById(R.id.chart);
        LineDataSet lineDataSet = new LineDataSet(entries, "random numbers");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setLineWidth(1);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(Color.LTGRAY);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setFadingEdgeLength(20);
        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxis = lineChart.getAxisRight();
        yAxisLeft.setTextSize(mTEXT_SIZE);
        yAxis.setEnabled(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(mTEXT_SIZE);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawGridLinesBehindData(false);
        lineChart.invalidate();

    }

    public void intialiseBarChart(){
        setDataForBarChart();
        final List<String> weekDays = Arrays.asList("sunday","monday","tuesday","wednesday","thursday","friday","saturday");
        Log.i("colors",String.valueOf(decodedColors.length));
        Log.i("color red", String.valueOf(Color.RED));
        BarChart barChart = findViewById(R.id.bar_chart);
        BarDataSet barDataSet = new BarDataSet(barEntries, "Random entries during Weeks");   //y axis data, "values"
        barDataSet.setColors(decodedColors);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        YAxis yAxis = barChart.getAxisRight();
        yAxis.setEnabled(false);
        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setTextSize(mTEXT_SIZE);
        yAxisLeft.setSpaceBottom(0);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return weekDays.get((int)value);
            }
        };
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-35);
        xAxis.setTextSize(mTEXT_SIZE);
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
        barChart.invalidate();


    }

    public void intialisePieChat(){
        setDataforPieChart();
        PieChart pieChart = findViewById(R.id.pie_chart);
        PieDataSet pieDataSet = new PieDataSet(pieEntries,"");
        pieDataSet.setDrawValues(false);
        pieDataSet.setColors(decodedColors);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.setDrawSliceText(false);
        pieChart.setDrawHoleEnabled(false);
        Legend legend = pieChart.getLegend();
        legend.setFormSize(mTEXT_SIZE);
        legend.setTextSize(mTEXT_SIZE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setXOffset(10);
        legend.setYOffset(20);
        pieChart.invalidate();
    }

    public void setDecodedColors(){
        final String[] colours = new String[]{"#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
                "#3B3EAC", "#0099C6", "#DD4477", "#66AA00", "#B82E2E", "#316395", "#994499", "#22AA99",
                "#AAAA11", "#6633CC", "#E67300", "#8B0707", "#329262", "#5574A6", "#3B3EAC"};
        decodedColors = new int[colours.length];
        for(int i=0; i<colours.length; i++){
            int n = Color.parseColor(colours[i]);
            decodedColors[i] = n;
        }
    }

}

