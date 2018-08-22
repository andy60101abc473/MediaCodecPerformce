package com.example.lai.decodetest;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.HashMap;

public class Debug {
    public static class TimeTravel {
        private static boolean enable = true;
        private static boolean enableAvg = true;

        private static String tag = "TimeTravel";
        private static int assignedIndex = 0;
        public int index;
        public long timeTravel;
        public long record;

        private static class TT_AVG {
            private static int sampleNum = 500;
            private int mSum = 0;
            private int count = 0;

            public void addTimeTravel(String name, long timeTravel) {
                mSum += timeTravel;
                count++;
                if(count == sampleNum) {
                    int avg = getAverage();
                    if(TimeTravel.enableAvg) {
                        Log.d(tag, name + " average costs: " + avg + " msec");
                    }
                }
            }
            private int getAverage() {
                int avg = (int)((float)mSum / (float)count + 0.5f);
                mSum = 0;
                count = 0;
                return avg;
            }
        }
        @SuppressLint("UseSparseArrays")
        private static HashMap<String, TT_AVG> mMap = new HashMap<>();

        public TimeTravel() {
            index = assignedIndex++;
            timeTravel = 0;
            record = System.currentTimeMillis() % 1000000000L;
        }

        public TimeTravel(boolean autoAssignIndex) {
            if(autoAssignIndex) index = assignedIndex++;
            else index = -1;
            timeTravel = 0;
            record = System.currentTimeMillis() % 1000000000L;
        }

        public void stamp(String name) {
            long now = System.currentTimeMillis() % 1000000000L;
            long dif = now - record;
            if(enable) Log.d(tag, "ID: " + index + ", To " + name + " costs: " + dif + " msec");
            timeTravel += dif;
            record = now;

            TT_AVG avg = mMap.get(name);
            if(avg == null) {
                avg = new TT_AVG();
                mMap.put(name, avg);
            }
            avg.addTimeTravel(name, dif);
        }

        public void showTotalTime() {
            if(enable) Log.d(tag, "ID: " + index + ", total travel costs " + timeTravel + " msec");
        }

        public TimeTravel clone() {
            TimeTravel travel = new TimeTravel(false);
            travel.index = index;
            travel.timeTravel = timeTravel;
            travel.record = record;
            return travel;
        }
    }
}
