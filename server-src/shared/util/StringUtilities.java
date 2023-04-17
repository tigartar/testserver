/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.util;

public final class StringUtilities {
    public static final long SECOND_MILLIS = 1000L;
    public static final long MINUTE_MILLIS = 60000L;
    public static final long HOUR_MILLIS = 3600000L;
    public static final long DAY_MILLIS = 86400000L;
    private static final String UPPER_LOWER_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String VILLAGE_LEGAL_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- ";
    private static final String SENTENCE_LEGAL_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- 1234567890.,";
    private static final String EMPTY_STRING = "";
    private static final String SPACE_AND_SPACE = " and ";
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private StringUtilities() {
    }

    public static String raiseFirstLetter(String oldString) {
        if (oldString != null && !oldString.isEmpty()) {
            return oldString.substring(0, 1).toUpperCase() + oldString.substring(1).toLowerCase();
        }
        return oldString;
    }

    public static String raiseFirstLetterOnly(String oldString) {
        if (oldString != null && !oldString.isEmpty()) {
            return oldString.substring(0, 1).toUpperCase() + oldString.substring(1);
        }
        return oldString;
    }

    public static boolean containsIllegalPlayerNameCharacters(String name) {
        return StringUtilities.containsIllegalCharacters(name, UPPER_LOWER_CHARS);
    }

    public static boolean containsNonSentenceCharacters(String name) {
        return StringUtilities.containsIllegalCharacters(name, SENTENCE_LEGAL_CHARS);
    }

    public static boolean containsIllegalVillageCharacters(String name) {
        return StringUtilities.containsIllegalCharacters(name, VILLAGE_LEGAL_CHARS);
    }

    private static boolean containsIllegalCharacters(String name, String illegalCharacters) {
        char[] chars = name.toCharArray();
        for (int x = 0; x < chars.length; ++x) {
            if (illegalCharacters.indexOf(chars[x]) >= 0) continue;
            return true;
        }
        return false;
    }

    public static String getTimeString(long timeleft) {
        String times = EMPTY_STRING;
        if (timeleft < 60000L) {
            long secs = timeleft / 1000L;
            times = times + secs + " seconds";
        } else {
            String aft;
            long daysleft = timeleft / 86400000L;
            long hoursleft = (timeleft - daysleft * 86400000L) / 3600000L;
            long minutesleft = (timeleft - daysleft * 86400000L - hoursleft * 3600000L) / 60000L;
            if (daysleft > 0L) {
                times = times + daysleft + " days";
            }
            if (hoursleft > 0L) {
                aft = EMPTY_STRING;
                if (daysleft > 0L && minutesleft > 0L) {
                    times = times + ", ";
                    aft = aft + SPACE_AND_SPACE;
                } else if (daysleft > 0L) {
                    times = times + SPACE_AND_SPACE;
                } else if (minutesleft > 0L) {
                    aft = aft + SPACE_AND_SPACE;
                }
                times = times + hoursleft + " hours" + aft;
            }
            if (minutesleft > 0L) {
                aft = EMPTY_STRING;
                if (daysleft > 0L && hoursleft == 0L) {
                    aft = SPACE_AND_SPACE;
                }
                times = times + aft + minutesleft + " minutes";
            }
        }
        if (times.length() == 0) {
            times = "nothing";
        }
        return times;
    }

    public static String getWordForNumber(int number) {
        String toReturn;
        switch (number) {
            case 1: {
                toReturn = "one";
                break;
            }
            case 2: {
                toReturn = "two";
                break;
            }
            case 3: {
                toReturn = "three";
                break;
            }
            case 4: {
                toReturn = "four";
                break;
            }
            case 5: {
                toReturn = "five";
                break;
            }
            case 6: {
                toReturn = "six";
                break;
            }
            case 7: {
                toReturn = "seven";
                break;
            }
            case 8: {
                toReturn = "eight";
                break;
            }
            case 9: {
                toReturn = "nine";
                break;
            }
            case 10: {
                toReturn = "ten";
                break;
            }
            default: {
                toReturn = String.valueOf(number);
            }
        }
        return toReturn;
    }

    public static String replace(String target, String from, String to) {
        int start = target.indexOf(from);
        if (start == -1) {
            return target;
        }
        int lf = from.length();
        char[] targetChars = target.toCharArray();
        StringBuilder buffer = new StringBuilder();
        int copyFrom = 0;
        while (start != -1) {
            buffer.append(targetChars, copyFrom, start - copyFrom);
            buffer.append(to);
            copyFrom = start + lf;
            start = target.indexOf(from, copyFrom);
        }
        buffer.append(targetChars, copyFrom, targetChars.length - copyFrom);
        return buffer.toString();
    }

    public static boolean isVowel(char aLetter) {
        return "aeiouAEIOU".indexOf(aLetter) != -1;
    }

    public static boolean isConsonant(char aLetter) {
        return !StringUtilities.isVowel(aLetter);
    }

    public static String htmlify(String aLine) {
        String lLine = aLine.replaceAll("&", "&amp;");
        lLine = lLine.replaceAll("<", "&lt;");
        lLine = lLine.replaceAll(">", "&gt;");
        return lLine;
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int n : bytes) {
            sb.append(HEX_DIGITS[(n &= 0xFF) >> 4]);
            sb.append(HEX_DIGITS[n & 0xF]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String addGenus(String name) {
        char firstLetter = name.charAt(0);
        char lastLetter = name.charAt(name.length() - 1);
        StringBuilder builder2 = new StringBuilder(name.length() + 5);
        builder2.setLength(0);
        if (lastLetter == 's') {
            builder2.append("some ");
        } else if (StringUtilities.isVowel(firstLetter)) {
            builder2.append("an ");
        } else {
            builder2.append("a ");
        }
        builder2.append(name);
        return builder2.toString();
    }

    public static String addGenus(String name, boolean plural) {
        char firstLetter = name.charAt(0);
        StringBuilder builder2 = new StringBuilder(name.length() + 5);
        builder2.setLength(0);
        if (plural) {
            builder2.append("some ");
        } else if (StringUtilities.isVowel(firstLetter)) {
            builder2.append("an ");
        } else {
            builder2.append("a ");
        }
        builder2.append(name);
        return builder2.toString();
    }
}

