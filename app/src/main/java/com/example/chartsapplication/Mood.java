package com.example.chartsapplication;

import com.example.chartsapplication.R;

public class Mood {

    public static final int MOOD_NONE = 0 ;
    public static final int MOOD_AWESOME_ID  = 1;
    public static final int MOOD_HAPPY_ID  = 2;
    public static final int MOOD_NEUTRAL_ID  = 3;
    public static final int MOODL_BAD_ID  = 4;
    public static final int MOODL_AWFUL_ID  = 5;

    public static final int MOOD_AWESOME_RES_ID  = R.string.mood_1happy;
    public static final int MOOD_HAPPY_RES_ID  = R.string.mood_2smile;
    public static final int MOOD_NEUTRAL_RES_ID  = R.string.mood_3neutral;
    public static final int MOODL_BAD_RES_ID  = R.string.mood_4unhappy;
    public static final int MOODL_AWFUL_RES_ID  = R.string.mood_5teardrop;

    public int mId ;

    public Mood(int id) {

        this.mId = id ;
    }

    public int getMoodTextResId(){
        int retVal = 0;

        if(mId==MOOD_NONE){
            retVal = R.string.mood_none;
        }

        if(mId==MOOD_AWESOME_ID){
            retVal = R.string.mood_1;
        }
        if(mId==MOOD_HAPPY_ID){
            retVal = R.string.mood_2;
        }
        if(mId==MOOD_NEUTRAL_ID){
            retVal = R.string.mood_3;
        }
        if(mId==MOODL_BAD_ID){
            retVal = R.string.mood_4;
        }
        if(mId==MOODL_AWFUL_ID){
            retVal = R.string.mood_5;
        }
        return retVal;
    }

    public int getFontResId() {
        int retVal = 0;

        if(mId==MOOD_AWESOME_ID){
            retVal = MOOD_AWESOME_RES_ID;
        }
        if(mId==MOOD_HAPPY_ID){
            retVal = MOOD_HAPPY_RES_ID;
        }
        if(mId==MOOD_NEUTRAL_ID){
            retVal = MOOD_NEUTRAL_RES_ID;
        }
        if(mId==MOODL_BAD_ID){
            retVal = MOODL_BAD_RES_ID;
        }
        if(mId==MOODL_AWFUL_ID){
            retVal = MOODL_AWFUL_RES_ID;
        }
        return retVal;
    }

    public int getId(){
        return mId;
    }


}
