package com.example.chartsapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    private List<BarEntry> barEntriesWordCountList;
    private List<BarEntry> barChartEntriesCountList;
    private List<String> weekDays;
    private List<TextView> testTextViewArray;
    private int mTEXT_SIZE = 12;
    private int[] decodedColors;
    private Typeface mMoodsFont;
    private Mood mood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testTextViewArray = new ArrayList<>();
        intialiseTextViews();
        mMoodsFont = ResourcesCompat.getFont( this, R.font.diaro_moods);
        weekDays = Arrays.asList("sunday","monday","tuesday","wednesday","thursday","friday","saturday");
        setDecodedColors();
        initialiseLineChartEntryCount();
        intialiseBarChartForWordCountPerDay();
        intialiseBarChartForEntryCountPerDay();
        intialiseBarChartForMoodCountPerDay();
        intialisePieChat();

    }

    public void setDataForLineChartEntryCount(){
        entries = new ArrayList<Entry>();
        for(int i=0; i < 30; i++){
            int val = ThreadLocalRandom.current().nextInt(0, 30 + 1);
            entries.add(new Entry(i*(-1),val));
        }
        Collections.sort(entries,new EntryXComparator());
   }

    public void setDataForBarChartMoodCount(){
        barEntries = new ArrayList<>();
        for(int i=0; i< weekDays.size(); i++) {
            int val = ThreadLocalRandom.current().nextInt(0, 20 + 1);
            if(!weekDays.get(i).isEmpty()) {
                barEntries.add(new BarEntry(i,val));
            }
        }
    }


    public void setDataForBarChartEntryCountPerDay(){
        barChartEntriesCountList = new ArrayList<>();
        for(int i=0; i< weekDays.size(); i++) {
            int val = ThreadLocalRandom.current().nextInt(0, 200);
            if(!weekDays.get(i).isEmpty()) {
                barChartEntriesCountList.add(new BarEntry(i,val));
            }
        }
    }

    public void setDataForBarChartWordCountPerDay(){
        barEntriesWordCountList = new ArrayList<>();
        for(int i=0; i< weekDays.size(); i++) {
            int val = ThreadLocalRandom.current().nextInt(0, 200);
            if(!weekDays.get(i).isEmpty()) {
                barEntriesWordCountList.add(new BarEntry(i,val));
            }
        }


    }

    public void setDataforPieChart(){
        pieEntries = new ArrayList<>();
        String[] moods = new String[] {"1","2","3","4","5"};
       for(int i=0; i< moods.length; i++) {
           int val = ThreadLocalRandom.current().nextInt(0, 30);
           mood = new Mood(Integer.valueOf(moods[i]));
           testTextViewArray.get(i).setText(mood.getFontResId());
           testTextViewArray.get(i).setTypeface(mMoodsFont);
           pieEntries.add(new PieEntry(val, mood.getFontResId()));
           Log.i("fontResId",String.valueOf(mood.getFontResId()));
       }
    }

    public void initialiseLineChartEntryCount(){
        setDataForLineChartEntryCount();
        LineChart lineChart = findViewById(R.id.chart);
        LineDataSet lineDataSet = new LineDataSet(entries, "days");
        lineDataSetproperties(lineDataSet);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setFadingEdgeLength(20);
        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxis = lineChart.getAxisRight();
        modifyYAxisforGraph(yAxisLeft,true,0 , mTEXT_SIZE);
        modifyYAxisforGraph(yAxis,false,0, mTEXT_SIZE);
        xAxis.setDrawGridLinesBehindData(false);
        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.valueOf(-(int)value);
            }
        };
        modifyXAxisforGraph(xAxis,true,0,valueFormatter);
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

    public void intialiseBarChartForEntryCountPerDay(){
        setDataForBarChartEntryCountPerDay();
        BarChart barChartEntryCount = findViewById(R.id.bar_chart_entry_count);
        BarDataSet barDataSet = new BarDataSet(barChartEntriesCountList,"Random entries during Week");
        intitaliseBarChart(barChartEntryCount,barDataSet);



    }

    public void intialiseBarChartForMoodCountPerDay(){
        setDataForBarChartMoodCount();
        BarChart barChartMoodCount = findViewById(R.id.bar_chart_mood_per_day);
        BarDataSet barDataSetMoodCount = new BarDataSet(barEntries, "Random Moods during the Week");
        intitaliseBarChart(barChartMoodCount,barDataSetMoodCount);


    }

    public void intialiseBarChartForWordCountPerDay(){
        setDataForBarChartWordCountPerDay();
        BarChart barChartWordCount = findViewById(R.id.bar_chart_word_count);
        BarDataSet barDataSet = new BarDataSet(barEntriesWordCountList, "Words during Week");
        intitaliseBarChart(barChartWordCount,barDataSet);
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
        legend.setTypeface(mMoodsFont);
        setLegendProperties(legend,true,mTEXT_SIZE,-25, Legend.LegendVerticalAlignment.CENTER, Legend.LegendHorizontalAlignment.LEFT, Legend.LegendOrientation.VERTICAL);
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



    public void intialiseTextViews(){

        TextView testTextView0;
        testTextViewArray.add(testTextView0 = findViewById(R.id.testTextView0));
        TextView testTextView1;
        testTextViewArray.add(testTextView1 = findViewById(R.id.testTextView1));
        TextView testTextView2;
        testTextViewArray.add(testTextView2 = findViewById(R.id.testTextView2));
        TextView testTextView3;
        testTextViewArray.add(testTextView3 = findViewById(R.id.testTextView3));
        TextView testTextView4;
        testTextViewArray.add(testTextView4 = findViewById(R.id.testTextView4));

    }

    /** all the methods here can be expanded as we introduce properties in the graph **/

    public void intitaliseBarChart(BarChart barChart, BarDataSet barDataSet){
        barDataSet.setColors(decodedColors);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        YAxis yAxis = barChart.getAxisRight();
        YAxis yAxisLeft = barChart.getAxisLeft();
        modifyYAxisforGraph(yAxis,false,0,mTEXT_SIZE);
        modifyYAxisforGraph(yAxisLeft,true,0,mTEXT_SIZE);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return weekDays.get((int)value);
            }
        };
        modifyXAxisforGraph(xAxis,false,-35,formatter);
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.invalidate();
    }

    public void modifyXAxisforGraph(XAxis xAxis, Boolean gridlines, float labelRotationAngel, ValueFormatter formatter){
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(mTEXT_SIZE);
        xAxis.setDrawGridLines(gridlines);
        xAxis.setDrawGridLinesBehindData(gridlines);
        xAxis.setValueFormatter(formatter);
        xAxis.setLabelRotationAngle(labelRotationAngel);
    }
    public void modifyYAxisforGraph(YAxis yAxis, Boolean enabled , int spaceBottom, int textSize){
        yAxis.setEnabled(enabled);
        yAxis.setTextSize(textSize);
        yAxis.setSpaceBottom(spaceBottom);


    }

    public void lineDataSetproperties(LineDataSet lineDataSet){
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setLineWidth(1);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(Color.LTGRAY);
    }

    //-- add a lineDataProperties() method here for each chart as required ---

    public void setLegendProperties(Legend legend, boolean setEnabled, int mTextSize , int offset, Legend.LegendVerticalAlignment v , Legend.LegendHorizontalAlignment h , Legend.LegendOrientation o){
        legend.setEnabled(setEnabled);
        legend.setFormSize(mTextSize);
        legend.setTextSize(mTextSize);
        legend.setVerticalAlignment(v);
        legend.setHorizontalAlignment(h);
        legend.setOrientation(o);
        legend.setYOffset(offset);

    }






}

