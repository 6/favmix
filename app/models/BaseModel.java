/*
 * File: BaseModel.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 18
 */
package models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import play.i18n.Messages;
import siena.Model;

/**
 * Base class for all models with reusable methods.
 * 
 * @author Peter Graham
 * @author Devin Moore
 */
public class BaseModel extends Model {

    /**
     * Sort a given map by value in ascending order. Based by Devin Moore:
     * http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-
     *      the-values-in-java
     *
     * @param map the map to sort by value
     * @return the sorted map by value in ascending order
     */
    public Map sortByValue(Map map) {
         List list = new LinkedList(map.entrySet());
         Collections.sort(list, new Comparator() {
              public int compare(Object o1, Object o2) {
                   return ((Comparable) ((Map.Entry) (o1)).getValue())
                  .compareTo(((Map.Entry) (o2)).getValue());
              }
         });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Returns how long ago a given date is in a readable format. If less than
     * one minute ago, returns the message for "now". If 1 minute or less,
     * returns the singular form of "minute". If 2-59 minutes ago, returns the
     * plural form of "minute". Does the same so forth for hours and days if
     * applicable.
     *
     * Note: many Java programmers recommend the Joda-Time library for this,
     * since this library handles many date-related things automatically, such
     * as daylight savings time.
     *
     * @param date the date to see how long ago it was
     * @return a String of how long ago this update was posted
     */
    public String getHowLongAgo(Date date) {
        Date curDate = new Date(System.currentTimeMillis());
        long diffms = curDate.getTime() - date.getTime();
        long diffmin = diffms / (60*1000);
        if(diffmin <= 0) {
            return Messages.get("time.now");
        }
        if(diffmin <= 1) {
            return Messages.get("time", diffmin, Messages.get("time.min"));
        }
        if(diffmin <= 59) {
            return Messages.get("time", diffmin, Messages.get("time.mins"));
        }
        long diffhour = diffmin / 60;
        if(diffhour <= 1) {
            return Messages.get("time", diffhour, Messages.get("time.hour"));
        }
        if(diffhour <= 23) {
            return Messages.get("time", diffhour, Messages.get("time.hours"));
        }
        long diffday = diffhour / 24;
        if(diffday <= 1) {
            return Messages.get("time", diffday, Messages.get("time.day"));
        }
        return Messages.get("time", diffday, Messages.get("time.days"));
    }
}
