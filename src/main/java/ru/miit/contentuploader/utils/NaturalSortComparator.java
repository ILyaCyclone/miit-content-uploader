package ru.miit.contentuploader.utils;

import java.util.*;

/**
 * http://fritzthecat-blog.blogspot.com/2015/10/natural-sort-order-reference.html
 */
public class NaturalSortComparator implements Comparator<String> {

    private final boolean caseSensitive = false;

    private final Map<String, List<String>> splitCache = new HashMap<>();

    /**
     * Splits the given strings and then compares them,
     * according to their nature, either as numbers or others.
     */
    @Override
    public int compare(String string1, String string2) {
        final Iterator<String> iterator1 = splitCached(string1).iterator();
        final Iterator<String> iterator2 = splitCached(string2).iterator();

        while (iterator1.hasNext() || iterator2.hasNext()) {
            // first has no more parts -> comes first
            if (!iterator1.hasNext())
                return -1;

            // second has no more parts -> comes first
            if (!iterator2.hasNext())
                return 1;

            // get parts to compare
            final String part1 = iterator1.next();
            final String part2 = iterator2.next();

            int result;
            // if part is a number
            if (Character.isDigit(part1.charAt(0)) && Character.isDigit(part2.charAt(0))) {
                result = Long.compare(Long.parseLong(part1), Long.parseLong(part2));

                // if numbers are equal, then shorter comes first
                if (result == 0)
                    result = Integer.compare(part1.length(), part2.length());
            } else {   // part is name or separator
                result = caseSensitive ? part1.compareTo(part2) : part1.compareToIgnoreCase(part2);
            }

            if (result != 0)    // found difference
                return result;
        }

        return 0;
    }

    private List<String> splitCached(String string) {
        return splitCache.computeIfAbsent(string, this::split);
    }

    /**
     * Splits given string into a list of names, numbers and separators (others).
     */
    private List<String> split(String string) {

        final List<String> list = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();

        boolean digits = false;
        boolean letters = false;
        boolean others = false;

        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);

            boolean isWhitespace = Character.isWhitespace(c);
            boolean isDigit = !isWhitespace && Character.isDigit(c);
            boolean isLetter = !isWhitespace && !isDigit && Character.isLetter(c);
            boolean isOther = !isWhitespace && !isDigit && !isLetter;

            if (isWhitespace || isDigit && !digits || isLetter && !letters || isOther && !others) {
                if (sb.length() > 0) { // close current string part
                    list.add(sb.toString());
                    sb.setLength(0);
                }
                digits = isDigit;
                letters = isLetter;
            }

            if (!isWhitespace)
                sb.append(c);
        }

        if (sb.length() > 0) // do not lose last part
            list.add(sb.toString());

        return list;
    }

}