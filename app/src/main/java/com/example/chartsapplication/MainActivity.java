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
import com.pixelcrater.Diaro.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StatsActivity extends AppCompatActivity {

    // Labels
    private List<Float> lineChartEntryLabels = new ArrayList<>();
    private List<Float> lineChartWordLabels = new ArrayList<>();
    private List<Float> lineChartMoodLabels = new ArrayList<>();

    //Dataholders
    private List<BarEntry> listEntryCountByWeekday = new ArrayList<>();
    private List<BarEntry> barChartMoodCountList = new ArrayList<>();
    private List<BarEntry> listWordCountPerDay = new ArrayList<>();
    private List<Entry> lineChartWordCountList = new ArrayList<>();
    private List<Entry> lineChartEntriesCountList = new ArrayList<>();
    private List<Entry> lineChartMoodCountList = new ArrayList<>();
    private List<PieEntry> pieChartMoodsList = new ArrayList<>();

    // The chartviews
    private BarChart chartEntryCountByWeekday;
    private BarChart barChartMoodCount;
    private BarChart chartWordCountByWeekday;
    private LineChart lineChartEntryCount;
    private LineChart lineChartWordCount;
    private LineChart lineChartForMood;
    private PieChart pieChartMoodCount;


    private List<String> weekDays = new ArrayList<>();
    private List<TextView> testTextViewArray = new ArrayList<>();

    private int mTEXT_SIZE = 12;
    private int[] decodedColors;
    private Typeface mMoodsFont;
    private Mood mood;
    private String currentSelectedDropDownItem;
    private String moodTextForToast;

    private Spinner spinner;

    DataHandler dataHandler = new DataHandler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       setContentView(R.layout.activity_main);
        
        mMoodsFont = ResourcesCompat.getFont(this, R.font.diaro_moods);
        weekDays = Arrays.asList(new DateFormatSymbols(getApplicationContext().getResources().getConfiguration().locale).getShortWeekdays()).subList(1, 8);

        initViews();

        try {
            fetchData(dataHandler.oneYearData.getArray());
            intChartData();
        } catch (Exception e) {

        }

    }

    public void initViews() {

        spinner = findViewById(R.id.spinner_select_days);

        chartEntryCountByWeekday = findViewById(R.id.bar_chart_entry_count);

        barChartMoodCount = findViewById(R.id.bar_chart_mood_per_day);
        chartWordCountByWeekday = findViewById(R.id.bar_chart_word_count);
        lineChartWordCount = findViewById(R.id.line_chart_word_count);
        lineChartEntryCount = findViewById(R.id.line_chart_entry_count);
        pieChartMoodCount = findViewById(R.id.pie_chart_mood_count);
        lineChartForMood = findViewById(R.id.line_chart_average_mood);

        testTextViewArray = new ArrayList<>();

        testTextViewArray.add(findViewById(R.id.testTextView0));
        testTextViewArray.add(findViewById(R.id.testTextView1));
        testTextViewArray.add(findViewById(R.id.testTextView2));
        testTextViewArray.add(findViewById(R.id.testTextView3));
        testTextViewArray.add(findViewById(R.id.testTextView4));

        final String[] colours = new String[]{"#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
                "#3B3EAC", "#0099C6", "#DD4477", "#66AA00", "#B82E2E", "#316395", "#994499", "#22AA99",
                "#AAAA11", "#6633CC", "#E67300", "#8B0707", "#329262", "#5574A6", "#3B3EAC"};
        decodedColors = new int[colours.length];
        for (int i = 0; i < colours.length; i++) {
            int n = Color.parseColor(colours[i]);
            decodedColors[i] = n;
        }

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.select_array_spinner, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                try {

                    currentSelectedDropDownItem = String.valueOf(spinner.getSelectedItem());
                    fetchData(currentSelectedDropDownItem);

                    intChartData();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void intChartData() {

        setDataToBarChart(chartEntryCountByWeekday, listEntryCountByWeekday);
        setDataToBarChart(chartWordCountByWeekday, listWordCountPerDay);
        setDataToBarChart(barChartMoodCount, barChartMoodCountList);

        setDataToLineChart(lineChartWordCount, lineChartWordCountList, "words", "word", lineChartWordLabels);
        setDataToLineChart(lineChartEntryCount, lineChartEntriesCountList, "entries", "entry", lineChartEntryLabels);
        setDataToLineChart(lineChartForMood, lineChartMoodCountList, "moods", "mood", lineChartMoodLabels);

        modifYAxisForMoodsChart(barChartMoodCount.getAxisLeft());
        modifYAxisForMoodsChart(lineChartForMood.getAxisLeft());

        setDataToPieChart(pieChartMoodsList);
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
            Toast.makeText(this, String.valueOf((int) e.getY() + " " + first + "\n" + ((l.size() - 1) - (int) e.getX()) + " " + third), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, String.valueOf((int) e.getY() + " " + second + "\n" + ((l.size() - 1) - (int) e.getX()) + " " + third), Toast.LENGTH_SHORT).show();
        }
    }

    public String getTextForToastMood(Entry e) {
        int moodId = 6 - Math.round(e.getY());
        return setAndSerializeMoodsForYAxisAndToast(moodId);

    }

    public void fetchData(String dropDownList) throws JSONException {

        String root = "";
        if (dropDownList.equals("1 Year")) {
            root = dataHandler.oneYearData.getArray();

        } else if (dropDownList.equals("2 Months")) {
            root = dataHandler.twoMonthsData.getArray();

        } else if (dropDownList.equals("4 Weeks")) {
            root = dataHandler.fourWeeksData.getArray();

        } else if (dropDownList.equals("7 Days")) {
            root = dataHandler.sevenDaysData.getArray();

        }

        JSONObject selectedChartJsonObject = new JSONObject(root);

        JSONObject entriesCount = (JSONObject) selectedChartJsonObject.get("entries_count");
        JSONObject dailyWords = (JSONObject) selectedChartJsonObject.get("daily_words");
        JSONObject wordsCountDuringLastXDays = (JSONObject) selectedChartJsonObject.get("words_count_during_last_x_days");
        JSONObject entriesPerDayOfWeek = (JSONObject) selectedChartJsonObject.get("entries_per_day_of_week");
        JSONObject averageDayOfWeekMood = (JSONObject) selectedChartJsonObject.get("average_day_of_week_mood");
        JSONObject averageMoodDuringLastXDays = (JSONObject) selectedChartJsonObject.get("average_mood_during_last_x_days");
        JSONObject moodCount = (JSONObject) selectedChartJsonObject.get("mood_count");

        listEntryCountByWeekday = DataHandler.getEntryListFromJson(entriesCount);
        listWordCountPerDay = DataHandler.getEntryListFromJson(wordsCountDuringLastXDays);
        lineChartEntriesCountList = DataHandler.getEntryListFromJson(entriesCount);
        lineChartWordCountList = DataHandler.getEntryListFromJson(wordsCountDuringLastXDays);
        lineChartMoodCountList = DataHandler.getEntryListFromJson(averageMoodDuringLastXDays);
        barChartMoodCountList = DataHandler.getBarEntryListFromJson(averageDayOfWeekMood);

        pieChartMoodsList = addDataToPieChartFromJson(moodCount);

        lineChartEntryLabels = DataHandler.getFloatListFromJson(entriesCount);
        lineChartWordLabels = DataHandler.getFloatListFromJson(wordsCountDuringLastXDays);
        lineChartMoodLabels = DataHandler.getFloatListFromJson(averageMoodDuringLastXDays);
    }


    public void setDataToBarChart(BarChart barChart, List<BarEntry> barEntry) {

        BarDataSet barDataSet = new BarDataSet(barEntry, "");
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


    public void setDataToLineChart(final LineChart lineChart, List<Entry> listEntry, final String firstToastString, final String secondToastString, final List<Float> labelsList) {

        LineDataSet lineDataSet = new LineDataSet(listEntry, firstToastString);
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setLineWidth(1);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(Color.LTGRAY);

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
                        Toast.makeText(StatsActivity.this, moodTextForToast + "\n" + ((labelsList.size() - 1) - (int) entry.getX()) + " " + "Weeks ago", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(StatsActivity.this, moodTextForToast + "\n" + ((labelsList.size() - 1) - (int) entry.getX()) + " " + "Days ago", Toast.LENGTH_SHORT).show();
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

    public void setDataToPieChart(List<PieEntry> pieEntry) {

        PieDataSet pieChartMoodsList = new PieDataSet(pieEntry, "");
        pieChartMoodsList.setDrawValues(false);
        pieChartMoodsList.setColors(decodedColors);
        PieData pieData = new PieData(pieChartMoodsList);
        pieChartMoodCount.setData(pieData);
        pieChartMoodCount.setDrawSliceText(false);
        pieChartMoodCount.setDrawHoleEnabled(false);
        Legend legend = pieChartMoodCount.getLegend();

        //-- add a lineDataProperties() method here for each chart as required ---
        legend.setEnabled(true);
        legend.setFormSize(mTEXT_SIZE);
        legend.setTextSize(mTEXT_SIZE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setYOffset(10);


        pieChartMoodCount.getDescription().setEnabled(false);
        pieChartMoodCount.invalidate();
    }

    public String setAndSerializeMoodsForYAxisAndToast(int moodId) {
        Mood mood = new Mood(moodId);
        if (mood.getMoodTextResId() == 0) {
            return "";
        } else {
            return getString(mood.getMoodTextResId());
        }
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

}
