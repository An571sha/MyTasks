package com.example.chartsapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends AppCompatActivity {

    private List<Entry> entries;
    private List<BarEntry> barEntries;
    private List<PieEntry> pieEntries;
    private List<TextView> testTextViewArray;
    private int mTEXT_SIZE = 12;
    private int[] decodedColors;
    private Typeface mMoodsFont;
    private TextView testTextView0;
    private TextView testTextView1;
    private TextView testTextView2;
    private TextView testTextView3;
    private TextView testTextView4;
    private Mood awesomeMood;
    private Mood happyMood;
    private Mood neutralMood;
    private Mood badMood;
    private Mood awfulMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testTextViewArray = new ArrayList<>();
        testTextViewArray.add(testTextView0 = findViewById(R.id.testTextView0));
        testTextViewArray.add(testTextView1 = findViewById(R.id.testTextView1));
        testTextViewArray.add(testTextView2 = findViewById(R.id.testTextView2));
        testTextViewArray.add(testTextView3 = findViewById(R.id.testTextView3));
        testTextViewArray.add(testTextView4 = findViewById(R.id.testTextView4));
        mMoodsFont = ResourcesCompat.getFont( this, R.font.diaro_moods);
        setDecodedColors();
        initialiseLineChart();
        intialiseBarChart();
        intialisePieChat();

    }

    public void setDataForLineChart(){
        entries = new ArrayList<Entry>();
        for(int i=0; i < 30; i++){
            int val = ThreadLocalRandom.current().nextInt(0, 30 + 1);
            Log.i("value of i",String.valueOf(i));
            entries.add(new Entry(i*(-1),val));
        }
        Log.i("Array legth", String.valueOf(entries.size()));
        Collections.sort(entries,new EntryXComparator());
   }

    public void setDataForBarChart(){
        barEntries = new ArrayList<>();
        String[] weekdays = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
        for(int i=0; i< weekdays.length; i++) {
            int val = ThreadLocalRandom.current().nextInt(0, 20 + 1);
            if(!weekdays[i].isEmpty()) {
                barEntries.add(new BarEntry(i,val));
            }
        }

//          barEntries.add(new BarEntry(0,10));
//          barEntries.add(new BarEntry(1,0));
//          barEntries.add(new BarEntry(2,20));
//          barEntries.add(new BarEntry(3,30));
//          barEntries.add(new BarEntry(4,8));
//          barEntries.add(new BarEntry(5,7));
//          barEntries.add(new BarEntry(6,11));

    }

    public void setDataforPieChart(){
        pieEntries = new ArrayList<>();
        String[] moods = new String[] {"1","2","3","4","5"};
       for(int i=0; i< moods.length; i++) {
           int val = ThreadLocalRandom.current().nextInt(0, 30);
           awesomeMood = new Mood(Integer.valueOf(moods[i]));
           testTextViewArray.get(i).setText(awesomeMood.getFontResId());
           testTextViewArray.get(i).setTypeface(mMoodsFont);
           pieEntries.add(new PieEntry(val,awesomeMood.getFontResId()));
           Log.i("fontResId",String.valueOf(awesomeMood.getFontResId()));
       }
    }
    public void initialiseLineChart(){
        setDataForLineChart();
        LineChart lineChart = findViewById(R.id.chart);
        LineDataSet lineDataSet = new LineDataSet(entries, "days");
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
        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.valueOf(-(int)value);
            }
        };
        xAxis.setValueFormatter(valueFormatter);

        lineChart.getDescription().setEnabled(false);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
              displayToast(e);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        lineChart.invalidate();


    }

    public void intialiseBarChart(){
        setDataForBarChart();
        final List<String> weekDays = Arrays.asList("sunday","monday","tuesday","wednesday","thursday","friday","saturday");
        BarChart barChart = findViewById(R.id.bar_chart);
        BarDataSet barDataSet = new BarDataSet(barEntries, "Random entries during Weeks");
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
        xAxis.setDrawGridLines(false);
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
        barChart.setTouchEnabled(true);
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
        legend.setTypeface(mMoodsFont);
        Log.i("typeface",legend.getTypeface().toString());
        pieChart.setEntryLabelTypeface(mMoodsFont);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setYOffset(-25);

        Log.i("legendEntries",legend.getEntries().toString());
        pieChart.getDescription().setEnabled(false);
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

    public void displayToast(Entry e){
        if(e.getY() < 1){
            Toast.makeText(MainActivity.this,String.valueOf((int)e.getY()+" "+"entry"+
                    (int)e.getX()+" "+"days ago"),Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this,String.valueOf((int)e.getY()+" "+"entries"+
                    (int)e.getX()+" "+"days ago"),Toast.LENGTH_SHORT).show();
        }
    }





}

