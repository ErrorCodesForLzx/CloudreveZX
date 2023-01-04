package com.ecsoft.cloudreve.time;

public class TimeUtils {
 
       /**
      * Constant that contains the amount of milliseconds in a second
      */
     static final long ONE_SECOND = 1000L;
 
     /**
      * Converts milliseconds to seconds
      * @param timeInMillis
      * @return The equivalent time in seconds
      */
     public static int toSecs(long timeInMillis) {
         // Rounding the result to the ceiling, otherwise a
         // System.currentTimeInMillis that happens right before a new Element
         // instantiation will be seen as 'later' than the actual creation time
         return (int)Math.ceil((double)timeInMillis / ONE_SECOND);
     }
 
     /**
      * Converts seconds to milliseconds, with a precision of 1 second
      * @param timeInSecs the time in seconds
      * @return The equivalent time in milliseconds
      */
     public static long toMillis(int timeInSecs) {
         return timeInSecs * ONE_SECOND;
     }
 
     /**
      * Converts a long seconds value to an int seconds value and takes into account overflow
      * from the downcast by switching to Integer.MAX_VALUE.
      * @param seconds Long value
      * @return Same int value unless long > Integer.MAX_VALUE in which case MAX_VALUE is returned
      */
     public static int convertTimeToInt(long seconds) {
         if (seconds > Integer.MAX_VALUE) {
             return Integer.MAX_VALUE;
         } else {
             return (int) seconds;
         }
     }
 
 }