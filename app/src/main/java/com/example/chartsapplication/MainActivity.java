package com.example.chartsapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private List<Entry> lineChartWordCountList;
    private List<Entry> lineChartEntriesCountList;
    private List<Entry> lineChartMoodCountList;
    private List<Float> lineChartWordLabels;
    private List<Float> lineChartEntriesLabels;
    private List<Float> lineChartMoodLabels;
    private List<BarEntry> barChartMoodCountList;
    private List<PieEntry> pieEntries;
    private List<BarEntry> barChartEntriesWordCountList;
    private List<BarEntry> barChartEntriesCountList;
    private List<String> weekDays;
    private List<TextView> testTextViewArray;
    private int mTEXT_SIZE = 12;
    private int[] decodedColors;
    private Typeface mMoodsFont;
    private Mood mood;
    private String currentSelectedDropDownItem;
    private String moodTextForToast;
    private LineChart lineChartForMood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testTextViewArray = new ArrayList<>();
        setSpinner();
        weekDays = Arrays.asList(new DateFormatSymbols(getApplicationContext().getResources().getConfiguration().locale).getWeekdays());
        weekDays = weekDays.subList(1, 8);
        intialiseTextViews();
        mMoodsFont = ResourcesCompat.getFont(this, R.font.diaro_moods);
        setDecodedColors();


    }

    public void setSpinner() {
        final Spinner spinner = findViewById(R.id.spinner_select_days);
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.select_array_spinner, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (barChartEntriesWordCountList != null && barChartEntriesCountList != null) {
                    barChartEntriesCountList.clear();
                    barChartEntriesWordCountList.clear();
                    lineChartWordCountList.clear();
                    lineChartEntriesCountList.clear();
                    lineChartMoodCountList.clear();
                    barChartMoodCountList.clear();
                }

                try {

                    currentSelectedDropDownItem = String.valueOf(spinner.getSelectedItem());
                    setJSONForSpinner(currentSelectedDropDownItem);
                    intialiseBarChartForEntryCountPerDay();
                    intialiseBarChartForWordCountPerDay();
                    intialiseLineChartForEntryCount();
                    intialiseLineChartForWordCount();
                    intialiseBarChartForMoodCountPerDay();
                    intialiseLinechartForAverageMood();
                    intialisePieChat();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void intialiseBarChartForEntryCountPerDay() {
        BarChart barChartEntryCount = findViewById(R.id.bar_chart_entry_count);
        BarDataSet barDataSet = new BarDataSet(barChartEntriesCountList, "Random entries during Week");
        initialiseBarChart(barChartEntryCount, barDataSet);


    }

    public void intialiseBarChartForMoodCountPerDay() {
        BarChart barChartMoodCount = findViewById(R.id.bar_chart_mood_per_day);
        BarDataSet barDataSetMoodCount = new BarDataSet(barChartMoodCountList, "Random Moods during the Week");
        initialiseBarChart(barChartMoodCount, barDataSetMoodCount);
        YAxis yAxisLeft = barChartMoodCount.getAxisLeft();
        modifYAxisForMoodsChart(yAxisLeft);


    }

    public void intialiseBarChartForWordCountPerDay() {
        BarChart barChartWordCount = findViewById(R.id.bar_chart_word_count);
        BarDataSet barDataSet = new BarDataSet(barChartEntriesWordCountList, "Words during Week");
        initialiseBarChart(barChartWordCount, barDataSet);
    }

    public void intialiseLineChartForWordCount() {
        LineChart lineChart = findViewById(R.id.line_chart_word_count);
        LineDataSet lineDataSet = new LineDataSet(lineChartWordCountList, "Words");
        initialiseLineChart(lineChart, lineDataSet, "words", "word", lineChartWordLabels);
    }

    public void intialiseLineChartForEntryCount() {
        LineChart lineChart = findViewById(R.id.line_chart_entry_count);
        LineDataSet lineDataSet = new LineDataSet(lineChartEntriesCountList, "Entries");
        initialiseLineChart(lineChart, lineDataSet, "entries", "entry", lineChartEntriesLabels);

    }

    public void intialiseLinechartForAverageMood() {
        lineChartForMood = findViewById(R.id.line_chart_average_mood);
        LineDataSet lineDataSet = new LineDataSet(lineChartMoodCountList, "Moods");
        initialiseLineChart(lineChartForMood, lineDataSet, "moods", "mood", lineChartMoodLabels);
        YAxis yAxisLeft = lineChartForMood.getAxisLeft();
        modifYAxisForMoodsChart(yAxisLeft);

    }


    public void intialisePieChat() {
        PieChart pieChart = findViewById(R.id.pie_chart);
        Log.i("pie", pieEntries.toString());
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setDrawValues(false);
        pieDataSet.setColors(decodedColors);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.setDrawSliceText(false);
        pieChart.setDrawHoleEnabled(false);
        Legend legend = pieChart.getLegend();
        setLegendProperties(legend, true, mTEXT_SIZE, 10, Legend.LegendVerticalAlignment.TOP,
                Legend.LegendHorizontalAlignment.LEFT, Legend.LegendOrientation.VERTICAL);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    public String setAndSerializeMoodsForYAxisAndToast(int moodId) {
        Mood mood = new Mood(moodId);
        if (mood.getMoodTextResId() == 0) {
            return "";
        } else {

            return getString(mood.getMoodTextResId());
        }
    }

    public void setDecodedColors() {
        final String[] colours = new String[]{"#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
                "#3B3EAC", "#0099C6", "#DD4477", "#66AA00", "#B82E2E", "#316395", "#994499", "#22AA99",
                "#AAAA11", "#6633CC", "#E67300", "#8B0707", "#329262", "#5574A6", "#3B3EAC"};
        decodedColors = new int[colours.length];
        for (int i = 0; i < colours.length; i++) {
            int n = Color.parseColor(colours[i]);
            decodedColors[i] = n;
        }
    }

    public void displayToast(List l, Entry e, String first, String second, String currentSelectedDropDownItem) {
        if (currentSelectedDropDownItem.equals("1 Year")) {

            generateToast(l, e, first, second, "weeks ago");
        } else {
            generateToast(l, e, first, second, "days ago");
        }
    }


    public void generateToast(List l, Entry e, String first, String second, String third) {

        if (e.getY() > 1) {
            Toast.makeText(MainActivity.this, String.valueOf((int) e.getY() + " " + first + "\n" +
                    ((l.size() - 1) - (int) e.getX()) + " " + third), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, String.valueOf((int) e.getY() + " " + second + "\n" +
                    ((l.size() - 1) - (int) e.getX()) + " " + third), Toast.LENGTH_SHORT).show();
        }
    }

    public String getTextForToastMood(Entry e) {
        int moodId = 6 - Math.round(e.getY());
        return setAndSerializeMoodsForYAxisAndToast(moodId);

    }

    public void intialiseTextViews() {

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


    public void setJSONForSpinner(String dropDownList) throws JSONException {
        DataHandler dataHandler = new DataHandler();
        if (dropDownList.equals("1 Year")) {
            String root = dataHandler.oneYearData.getArray();
            retireveAndParseJSON(root);
        } else if (dropDownList.equals("2 Months")) {
            String root = dataHandler.twoMonthsData.getArray();
            retireveAndParseJSON(root);
        } else if (dropDownList.equals("4 Weeks")) {
            String root = dataHandler.fourWeeksData.getArray();
            retireveAndParseJSON(root);
        } else if (dropDownList.equals("7 Days")) {
            String root = dataHandler.sevenDaysData.getArray();
            retireveAndParseJSON(root);
        }
    }

    public void retireveAndParseJSON(String root) throws JSONException {

        JSONObject selectedChartJsonObject = new JSONObject(root);
        JSONObject entriesCount = (JSONObject) selectedChartJsonObject.get("entries_count");
        JSONObject dailyWords = (JSONObject) selectedChartJsonObject.get("daily_words");
        JSONObject wordsCountDuringLastXDays = (JSONObject) selectedChartJsonObject.get("words_count_during_last_x_days");
        JSONObject entriesPerDayOfWeek = (JSONObject) selectedChartJsonObject.get("entries_per_day_of_week");
        JSONObject averageDayOfWeekMood = (JSONObject) selectedChartJsonObject.get("average_day_of_week_mood");
        JSONObject averageMoodDuringLastXDays = (JSONObject) selectedChartJsonObject.get("average_mood_during_last_x_days");
        JSONObject moodCount = (JSONObject) selectedChartJsonObject.get("mood_count");


        lineChartEntriesCountList = addDataToLineChartListFromJson(entriesCount);

        lineChartWordCountList = addDataToLineChartListFromJson(wordsCountDuringLastXDays);

        lineChartMoodCountList = addDataToLineChartListFromJson(averageMoodDuringLastXDays);

        lineChartEntriesLabels = addDataToLineChartLabelListFromJson(entriesCount);

        lineChartWordLabels = addDataToLineChartLabelListFromJson(wordsCountDuringLastXDays);

        lineChartMoodLabels = addDataToLineChartLabelListFromJson(averageMoodDuringLastXDays);

        barChartEntriesCountList = addDataToBarChartListFromJson(entriesPerDayOfWeek);

        barChartEntriesWordCountList = addDataToBarChartListFromJson(dailyWords);

        barChartMoodCountList = addDataToBarChartListFromJson(averageDayOfWeekMood);

        pieEntries = addDataToPieChartFromJson(moodCount);


    }


    public List<Entry> addDataToLineChartListFromJson(JSONObject jsonObject) throws JSONException {
        JSONArray value = (JSONArray) jsonObject.get("data");
        List<Entry> list = new ArrayList<>();
        for (int i = 0; i < value.length(); i++) {
            list.add(new Entry(i, (float) value.getDouble(i)));
        }
        return list;
    }


    public List<BarEntry> addDataToBarChartListFromJson(JSONObject jsonObject) throws JSONException {
        JSONArray value = (JSONArray) jsonObject.get("data");
        List<BarEntry> list = new ArrayList<>();
        for (int i = 0; i < value.length(); i++) {
            list.add(new BarEntry(i, (float) value.getDouble(i)));
        }
        return list;
    }

    public List<Float> addDataToLineChartLabelListFromJson(JSONObject jsonObject) throws JSONException {
        JSONArray labels = (JSONArray) jsonObject.get("labels");
        List<Float> list = new ArrayList<>();
        for (int i = 0; i < labels.length(); i++) {
            list.add((float) labels.getDouble(i));
        }
        return list;

    }

    public List<PieEntry> addDataToPieChartFromJson(JSONObject jsonObject) throws JSONException {
        JSONArray value = (JSONArray) jsonObject.get("data");
        List<PieEntry> list = new ArrayList<>();
        for (int i = 0; i < value.length(); i++) {
            mood = new Mood(i + 1);
            testTextViewArray.get(i).setText(mood.getFontResId());
            testTextViewArray.get(i).setTypeface(mMoodsFont);
            list.add(new PieEntry(value.getInt(i), String.valueOf(i + 1)));
        }
        return list;
    }


    /**
     * all the methods here can be expanded as we introduce properties in the graph
     **/

    public void initialiseBarChart(BarChart barChart, BarDataSet barDataSet) {
        barDataSet.setColors(decodedColors);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        YAxis yAxis = barChart.getAxisRight();
        YAxis yAxisLeft = barChart.getAxisLeft();
        modifyYAxisForGraph(yAxis, false, 0, mTEXT_SIZE);
        modifyYAxisForGraph(yAxisLeft, true, 0, mTEXT_SIZE);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return weekDays.get((int) value);
            }
        };
        modifyXAxisForGraph(xAxis, false, -35, formatter);
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.invalidate();
    }

    public void initialiseLineChart(final LineChart lineChart, LineDataSet lineDataSet, final String firstToastString, final String secondToastString, final List<Float> labelsList) {
        lineDataSetproperties(lineDataSet);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(null);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setFadingEdgeLength(20);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxis = lineChart.getAxisRight();
        modifyYAxisForGraph(yAxisLeft, true, 0, mTEXT_SIZE);
        modifyYAxisForGraph(yAxis, false, 0, mTEXT_SIZE);
        ValueFormatter valueFormatter = new ValueFormatter() {

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (labelsList.size() <= (int) value) {
                    return "";

                } else {
                    return (String.valueOf(labelsList.get((int) value)));

                }
            }
        };
        Log.i("LabelsAfter", labelsList.toString());
        modifyXAxisForGraph(xAxis, false, 0, valueFormatter);
        lineChart.getDescription().setEnabled(false);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight h) {
                if (lineChart.equals(lineChartForMood)) {
                    moodTextForToast = getTextForToastMood(entry);

                    if (currentSelectedDropDownItem.equals("1 Year")) {

                        Toast.makeText(MainActivity.this, moodTextForToast + "\n" +
                                ((labelsList.size() - 1) - (int) entry.getX()) + " " + "Weeks ago", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, moodTextForToast + "\n" +
                                ((labelsList.size() - 1) - (int) entry.getX()) + " " + "Days ago", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    displayToast(labelsList, entry, firstToastString, secondToastString, currentSelectedDropDownItem);

                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
        lineChart.invalidate();
    }

    public void modifyXAxisForGraph(XAxis xAxis, Boolean gridlines, float labelRotationAngel, ValueFormatter formatter) {
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(mTEXT_SIZE);
        xAxis.setDrawGridLines(gridlines);
        xAxis.setDrawGridLinesBehindData(gridlines);
        xAxis.setValueFormatter(formatter);
        xAxis.setLabelRotationAngle(labelRotationAngel);
    }

    public void modifyYAxisForGraph(YAxis yAxis, Boolean enabled, int spaceBottom, int textSize) {
        yAxis.setEnabled(enabled);
        yAxis.setTextSize(textSize);
        yAxis.setSpaceBottom(spaceBottom);


    }

    public void modifYAxisForMoodsChart(YAxis yAxisLeft) {
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setAxisMaximum(5);
        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int moodId = 6 - (int) value;
                return setAndSerializeMoodsForYAxisAndToast(moodId);
            }
        };
        yAxisLeft.setValueFormatter(valueFormatter);
        yAxisLeft.setTextSize(10f);

    }

    public void lineDataSetproperties(LineDataSet lineDataSet) {
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

    public void setLegendProperties(Legend legend, boolean setEnabled, int mTextSize, int offset, Legend.LegendVerticalAlignment v, Legend.LegendHorizontalAlignment h, Legend.LegendOrientation o) {
        legend.setEnabled(setEnabled);
        legend.setFormSize(mTextSize);
        legend.setTextSize(mTextSize);
        legend.setVerticalAlignment(v);
        legend.setHorizontalAlignment(h);
        legend.setOrientation(o);
        legend.setYOffset(offset);

    }

}

