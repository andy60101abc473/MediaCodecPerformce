package com.example.lai.decodetest;

import android.util.Log;

public class Debug {
    public static class TimeTravel {
        private static String tag = "TimeTravel";
        private static boolean enable = true;
        private static int assignedIndex = 0;
        public int index;
        public long timeTravel;
        public long record;

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
            if(enable) Log.d(tag, "ID: " + index + ", " + name + " costs: " + dif + " msec");
            timeTravel += dif;
            record = now;
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
