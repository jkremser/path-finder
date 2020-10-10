package cz.muni.fi.mobileIDS.src;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Jiri Kremser
 * 
 * Utils and constants for the whole application
 */
public class CommonUtils {
    //prefix of path to the resources
    public static final String RESOURCE_PREFIX_PATH = "_data/";
    //path to icons
    public static final String IMAGE_TRAM = "/cz/muni/fi/mobileIDS/_images/salina.gif";
    public static final String IMAGE_TROLLEY_BUS = "/cz/muni/fi/mobileIDS/_images/trol.gif";
    public static final String IMAGE_BUS = "/cz/muni/fi/mobileIDS/_images/bus.gif";
    public static final String IMAGE_TRAIN = "/cz/muni/fi/mobileIDS/_images/train.gif";
    //type constants
    public static final int ID_TRAM = 1;
    public static final int ID_TROLLEY_BUS = 2;
    public static final int ID_BUS1 = 3;
    public static final int ID_TRAIN1 = 4;
    public static final int ID_TRAIN2 = 5;

    /**
     * returns the proper path prefix to package with data
     * 
     * @param date
     * @return String Proper path prefix to package with data
     */
    public static String getDay(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(date);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        if ((d == 1 && m == Calendar.JANUARY) ||  // Novy rok
          (d == 1 && m == Calendar.MAY) ||        // Svatek prace
          (d == 8 && m == Calendar.MAY) ||        // Den vitezstvi
          (d == 5 && m == Calendar.JULY) ||       // Den slovanských věrozvěstů Cyrila a Metoděje
          (d == 6 && m == Calendar.JULY) ||       // Den upálení mistra Jana Husa, 
          (d == 28 && m == Calendar.SEPTEMBER) || // Den české státnosti
          (d == 28 && m == Calendar.OCTOBER) ||   // Den vzniku samostatného československého státu
          (d == 17 && m == Calendar.NOVEMBER) ||  // Den boje za svobodu a demokracii
          (d == 24 && m == Calendar.DECEMBER) ||  // Štědrý den
          (d == 25 && m == Calendar.DECEMBER) ||  // 1. svátek vánoční
          (d == 26 && m == Calendar.DECEMBER)) {  // 2. svátek vánoční
            return LineUtils.SUNDAY;
        } else {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    return LineUtils.SUNDAY;
                case Calendar.MONDAY:
                    return LineUtils.MONDAY;
                case Calendar.TUESDAY:
                    return LineUtils.TUESDAY;
                case Calendar.WEDNESDAY:
                    return LineUtils.WEDNESDAY;
                case Calendar.THURSDAY:
                    return LineUtils.THURSDAY;
                case Calendar.FRIDAY:
                    return LineUtils.FRIDAY;
                case Calendar.SATURDAY:
                    return LineUtils.SATURDAY;
            }
        }
        return null;
    }

    /**
     * returns actual time in minutes
     * 
     * @param date
     * @return int Number of minutes of day
     */
    public static int getTimeInMin(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(date);
        return 60 * calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE);
    }

    /**
     * transtales time from minutes to HH:MM format
     * 
     * @param min Number of minutes of day
     * @return String Time in format HH:MM
     */
    public static String getTimeFromMin(int min) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(new Date(60000L * min));
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        return (hours > 9 ? "" + hours : " " + hours) + ":" + (minutes > 9 ? "" + minutes : "0" + minutes);
    }

    /**
     * Sorts the specified array of shorts into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted
     */
    public static void sort(short[] a) {
        sort1(a, 0, a.length);
    }

    /**
     * Sorts the specified sub-array of shorts into ascending order.
     */
    private static void sort1(short x[], int off, int len) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i = off; i < len + off; i++) {
                for (int j = i; j > off && x[j - 1] > x[j]; j--) {
                    swap(x, j, j - 1);
                }
            }
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        short v = x[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) {
                    swap(x, a++, b);
                }
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) {
                    swap(x, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(x, b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            sort1(x, off, s);
        }
        if ((s = d - c) > 1) {
            sort1(x, n - s, s);
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(short x[], int a, int b) {
        short t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(short x[], int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(x, a, b);
        }
    }

    /**
     * Returns the index of the median of the three indexed shorts.
     */
    private static int med3(short x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }
}
