/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Constants;
import com.wurmonline.server.EasterCalculator;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.StringUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WurmCalendar
implements TimeConstants {
    private static final DateFormat gmtDateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
    private static final String[] day_names;
    private static final Logger logger;
    static final int STARFALL_DIAMONDS = 0;
    static final int STARFALL_SAW = 1;
    static final int STARFALL_DIGGING = 2;
    static final int STARFALL_LEAF = 3;
    static final int STARFALL_BEAR = 4;
    static final int STARFALL_SNAKE = 5;
    static final int STARFALL_SHARK = 6;
    static final int STARFALL_FIRES = 7;
    static final int STARFALL_RAVEN = 8;
    static final int STARFALL_DANCERS = 9;
    static final int STARFALL_OMENS = 10;
    static final int STARFALL_SILENCE = 11;
    private static boolean isSpring;
    private static boolean isTestChristmas;
    public static boolean wasTestChristmas;
    private static boolean isTestEaster;
    private static boolean isTestWurm;
    private static boolean isTestHalloween;
    private static boolean personalGoalsActive;
    private static final boolean ENABLE_CHECK_SPRING = false;
    private static final int startYear = 980;
    public static long currentTime;
    public static long lastHarvestableCheck;
    private static final String[] starfall_names;

    private WurmCalendar() {
    }

    public static String getTime() {
        long year = 980L + currentTime / 29030400L;
        int starfall = (int)(currentTime % 29030400L / 2419200L);
        int day = (int)(currentTime % 2419200L / 86400L);
        int dayOfWeek = day % 7;
        long week = day / 7 + 1;
        int hour = (int)(currentTime % 86400L / 3600L);
        int minute = WurmCalendar.getMinute();
        int second = WurmCalendar.getSecond();
        String toReturn = StringUtil.format("It is %02d:%02d:%02d", hour, minute, second);
        toReturn = toReturn + " on " + day_names[dayOfWeek] + " in week " + week + " of " + starfall_names[starfall] + " in the year of " + year + ".";
        return toReturn;
    }

    public static final boolean mayDestroyHugeAltars() {
        int day = (int)(currentTime % 2419200L / 86400L);
        long week = day / 7 + 1;
        return !(WurmCalendar.getDay() != 3 && WurmCalendar.getDay() != 6 || week != 1L && week != 3L);
    }

    public static final String getTimeFor(long wurmtime) {
        long year = 980L + wurmtime / 29030400L;
        int starfall = (int)Math.max(0L, wurmtime % 29030400L / 2419200L);
        int day = (int)(wurmtime % 2419200L / 86400L);
        int dayOfWeek = Math.max(0, day % 7);
        long week = day / 7 + 1;
        int hour = (int)(wurmtime % 86400L / 3600L);
        int minute = (int)(wurmtime % 3600L / 60L);
        int second = (int)(wurmtime % 60L);
        String toReturn = StringUtil.format("%02d:%02d:%02d", hour, minute, second);
        toReturn = toReturn + " on " + day_names[dayOfWeek] + " in week " + week + " of " + starfall_names[starfall] + " in the year of " + year + ".";
        return toReturn;
    }

    public static final String getDateFor(long wurmtime) {
        long year = 980L + wurmtime / 29030400L;
        int starfall = (int)(wurmtime % 29030400L / 2419200L);
        int day = (int)(wurmtime % 2419200L / 86400L);
        int dayOfWeek = day % 7;
        long week = day / 7 + 1;
        String toReturn = "";
        toReturn = toReturn + day_names[dayOfWeek] + ", week " + week + " of " + starfall_names[starfall] + ", " + year + ".";
        return toReturn;
    }

    public static final String getDaysFrom(long wurmtime) {
        boolean inPast = currentTime > wurmtime;
        long diff = Math.abs(currentTime - wurmtime);
        long diffYear = diff / 29030400L;
        int diffMonth = (int)(diff % 29030400L / 2419200L);
        int diffWeek = (int)(diff % 2419200L / 604800L);
        int diffDay = (int)(diff % 604800L / 86400L);
        StringBuilder buf = new StringBuilder();
        if (diffYear > 0L) {
            if (diffYear == 1L) {
                buf.append(diffYear + " year, ");
            } else {
                buf.append(diffYear + " years, ");
            }
        }
        if (diffYear > 0L || diffMonth > 0) {
            if (diffMonth == 1) {
                buf.append(diffMonth + " month, ");
            } else {
                buf.append(diffMonth + " months, ");
            }
        }
        if (diffYear > 0L || diffMonth > 0 || diffWeek > 0) {
            if (diffWeek == 1) {
                buf.append(diffWeek + " week, ");
            } else {
                buf.append(diffWeek + " weeks, ");
            }
        }
        if (diffDay == 1) {
            buf.append(diffDay + " day");
        } else {
            buf.append(diffDay + " days");
        }
        if (inPast) {
            buf.append(" ago.");
        } else {
            buf.append(".");
        }
        return buf.toString();
    }

    public static void tickSecond() {
        if (++currentTime < WurmHarvestables.getLastHarvestableCheck() + 3600L) {
            return;
        }
        WurmHarvestables.checkHarvestables(currentTime);
        if (personalGoalsActive && !WurmCalendar.nowIsBefore(0, 1, 1, 1, 2019)) {
            personalGoalsActive = false;
            Server.getInstance().broadCastAlert("Alert: Personal Goals are now disabled", true);
            for (Player p : Players.getInstance().getPlayers()) {
                p.getCommunicator().sendCloseWindow((short)27);
            }
        }
    }

    public static int getYear() {
        return 980 + WurmCalendar.getYearOffset();
    }

    public static int getYearOffset() {
        return (int)(currentTime / 29030400L);
    }

    public static int getStarfallWeek() {
        return (int)(currentTime % 29030400L / 604800L);
    }

    public static int getStarfall() {
        return (int)(currentTime % 29030400L / 2419200L);
    }

    public static int getDay() {
        int day = (int)(currentTime % 29030400L / 86400L);
        int dayOfWeek = day % 7;
        return dayOfWeek;
    }

    public static int getHour() {
        return (int)(currentTime % 86400L / 3600L);
    }

    public static int getMinute() {
        return (int)(currentTime % 3600L / 60L);
    }

    public static int getSecond() {
        return (int)(currentTime % 60L);
    }

    public static void incrementHour() {
        WurmCalendar.setTime(currentTime + 3600L);
    }

    protected static void setTime(long time) {
        currentTime = time;
    }

    public static boolean isNight() {
        int h = WurmCalendar.getHour();
        return h > 20 || h < 6;
    }

    public static boolean isMorning() {
        int h = WurmCalendar.getHour();
        return h <= 8 && h >= 2;
    }

    public static boolean isNewYear1() {
        return WurmCalendar.nowIsBetween(0, 1, 1, 0, Year.now().getValue(), 0, 5, 1, 0, Year.now().getValue());
    }

    public static boolean isAfterNewYear1() {
        return WurmCalendar.nowIsAfter(0, 5, 1, 0, Year.now().getValue());
    }

    public static boolean toggleSpecial(String special) {
        wasTestChristmas = isTestChristmas;
        isTestChristmas = false;
        isTestEaster = false;
        isTestWurm = false;
        isTestHalloween = false;
        switch (special) {
            case "xmas": {
                isTestChristmas = true;
                return true;
            }
            case "easter": {
                isTestEaster = true;
                return true;
            }
            case "wurm": {
                isTestWurm = true;
                return true;
            }
            case "halloween": {
                isTestHalloween = true;
                return true;
            }
        }
        return false;
    }

    public static boolean isTestChristmas() {
        return isTestChristmas;
    }

    public static boolean isChristmas() {
        if (isTestChristmas) {
            return true;
        }
        return WurmCalendar.nowIsBetween(15, 0, 23, 11, Year.now().getValue(), 12, 0, 31, 11, Year.now().getValue());
    }

    public static boolean isBeforeChristmas() {
        return WurmCalendar.nowIsBefore(17, 0, 23, 11, Year.now().getValue());
    }

    public static boolean isAfterChristmas() {
        return WurmCalendar.nowIsAfter(12, 0, 31, 11, Year.now().getValue());
    }

    public static boolean isAfterEaster() {
        Calendar c = EasterCalculator.findHolyDay(Year.now().getValue());
        return WurmCalendar.nowIsAfter(10, 0, c.get(5) + 2, c.get(2), c.get(1));
    }

    public static boolean isEaster() {
        if (isTestEaster) {
            return true;
        }
        Calendar c = EasterCalculator.findHolyDay(Year.now().getValue());
        return WurmCalendar.nowIsAfter(10, 0, c.get(5), c.get(2), c.get(1)) && WurmCalendar.nowIsBefore(10, 0, c.get(5) + 2, c.get(2), c.get(1));
    }

    public static boolean isHalloween() {
        if (isTestHalloween) {
            return true;
        }
        return WurmCalendar.nowIsBetween(0, 1, 28, 9, Year.now().getValue(), 23, 59, 5, 10, Year.now().getValue());
    }

    public static boolean isAnniversary() {
        if (isTestWurm) {
            return true;
        }
        return WurmCalendar.nowIsBetween(0, 1, 6, 5, Year.now().getValue(), 23, 59, 12, 5, Year.now().getValue());
    }

    public static String getSpecialMapping(boolean predot) {
        if (WurmCalendar.isChristmas()) {
            return predot ? ".xmas" : "xmas.";
        }
        if (WurmCalendar.isEaster()) {
            return predot ? ".easter" : "easter.";
        }
        if (WurmCalendar.isHalloween()) {
            return predot ? ".halloween" : "halloween.";
        }
        if (WurmCalendar.isAnniversary()) {
            return predot ? ".wurm" : "wurm.";
        }
        return "";
    }

    public static void checkSpring() {
    }

    public static boolean isSpring() {
        int starfall = WurmCalendar.getStarfall();
        return starfall > 2 && starfall < 6;
    }

    public static boolean isSummer() {
        int starfall = WurmCalendar.getStarfall();
        return starfall > 5 && starfall < 9;
    }

    public static boolean isAutumn() {
        int starfall = WurmCalendar.getStarfall();
        return starfall > 8 && starfall < 12;
    }

    public static boolean isWinter() {
        int starfall = WurmCalendar.getStarfall();
        return starfall > 11 || starfall < 3;
    }

    public static boolean isAutumnWinter() {
        int starfall = WurmCalendar.getStarfall();
        return starfall > 8 || starfall < 3;
    }

    public static boolean isSeasonSpring() {
        int starfallWeek = WurmCalendar.getStarfallWeek();
        return starfallWeek >= 2 && starfallWeek < 12;
    }

    public static boolean isSeasonSummer() {
        int starfallWeek = WurmCalendar.getStarfallWeek();
        return starfallWeek >= 12 && starfallWeek < 35;
    }

    public static boolean isSeasonAutumn() {
        int starfallWeek = WurmCalendar.getStarfallWeek();
        return starfallWeek >= 35 && starfallWeek < 45;
    }

    public static boolean isSeasonWinter() {
        int starfallWeek = WurmCalendar.getStarfallWeek();
        return starfallWeek >= 46 || starfallWeek < 2;
    }

    public static int getSeasonNumber() {
        int season = 0;
        if (WurmCalendar.isWinter()) {
            season = 4;
        }
        if (WurmCalendar.isSpring()) {
            season = 0;
        }
        if (WurmCalendar.isSummer()) {
            season = 2;
        }
        if (WurmCalendar.isAutumn()) {
            season = 3;
        }
        return season;
    }

    public static boolean nowIsBetween(int shour, int sminute, int sday, int smonth, int syear, int ehour, int eminute, int eday, int emonth, int eyear) {
        Calendar start = Calendar.getInstance();
        start.set(syear, smonth, sday, shour, sminute);
        long startTime = start.getTimeInMillis();
        Calendar end = Calendar.getInstance();
        end.set(eyear, emonth, eday, ehour, eminute);
        long endTime = end.getTimeInMillis();
        long now = System.currentTimeMillis();
        return now >= startTime && now <= endTime;
    }

    public static boolean nowIsBefore(int shour, int sminute, int sday, int smonth, int syear) {
        Calendar start = Calendar.getInstance();
        start.set(syear, smonth, sday, shour, sminute);
        return System.currentTimeMillis() < start.getTimeInMillis();
    }

    public static boolean nowIsAfter(int hour, int minute, int day, int month, int year) {
        Calendar cnow = Calendar.getInstance();
        cnow.set(year, month, day, hour, minute);
        return System.currentTimeMillis() > cnow.getTimeInMillis();
    }

    public static String formatGmt(long time) {
        return gmtDateFormat.format(new Date(time)) + " GMT";
    }

    public static long getCurrentTime() {
        return currentTime;
    }

    static {
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        day_names = new String[]{"day of the Ant", "Luck day", "day of the Wurm", "Wrath day", "day of Tears", "day of Sleep", "day of Awakening"};
        logger = Logger.getLogger(WurmCalendar.class.getName());
        isSpring = false;
        isTestChristmas = false;
        wasTestChristmas = false;
        isTestEaster = false;
        isTestWurm = false;
        isTestHalloween = false;
        personalGoalsActive = WurmCalendar.nowIsBefore(0, 1, 1, 1, 2019);
        currentTime = 0L;
        lastHarvestableCheck = 0L;
        starfall_names = new String[]{"the starfall of Diamonds", "the starfall of the Saw", "the starfall of the Digging", "the starfall of the Leaf", "the Bear's starfall", "the Snake's starfall", "the White Shark starfall", "the starfall of Fires", "the Raven's starfall", "the starfall of Dancers", "the starfall of Omens", "the starfall of Silence"};
    }

    static final class Ticker
    implements Runnable {
        Ticker() {
        }

        @Override
        public void run() {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Running newSingleThreadScheduledExecutor for calling WurmCalendar.tickSecond()");
            }
            try {
                long now = System.nanoTime();
                try {
                    WurmCalendar.tickSecond();
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, "Exception in WurmCalendar.tickSecond");
                    e.printStackTrace();
                }
                float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f;
                if (lElapsedTime > (float)Constants.lagThreshold) {
                    logger.info("Finished calling WurmCalendar.tickSecond(), which took " + lElapsedTime + " millis.");
                }
            }
            catch (RuntimeException e) {
                logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling WurmCalendar.tickSecond()", e);
                throw e;
            }
        }
    }
}

