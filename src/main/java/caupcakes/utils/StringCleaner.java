package caupcakes.utils;

import com.ibm.icu.text.Normalizer;

import java.util.HashMap;
import java.util.Map;

public class StringCleaner {

    private static final Map<Integer, Replacement> charMap = buildReplacementMap();

    private static Map<Integer, Replacement> buildReplacementMap() {
        Map<Integer, Replacement> map = new HashMap<>();
        map.put(0xc6, new Replacement("AE", "Ae"));
        map.put(0xe6, new Replacement("ae"));
        map.put(0xd0, new Replacement("D"));
        map.put(0x111, new Replacement("d"));
        map.put(0xd8, new Replacement("O"));
        map.put(0xf8, new Replacement("o"));
        map.put(0x152, new Replacement("OE", "Oe"));
        map.put(0x153, new Replacement("oe"));
        map.put(0x166, new Replacement("T"));
        map.put(0x167, new Replacement("t"));
        return map;
    }

    public static String convertToAscii(String input) {
        /*
         * operating on char arrays because java.lang.String seems to perform an
         * automatic recomposition of decomposed characters.
         */
        String result = null;
        if (null != input) {
            char[] src = input.toCharArray();
            /* save space for exotic UTF characters */
            char[] target = new char[src.length * 3];
            int len = Normalizer.normalize(input.toCharArray(), target, Normalizer.NFKD, 0);
            result = processSpecialChars(target, len);
        }
        return result;
    }

    private static String processSpecialChars(char[] target, int len) {
        StringBuilder result = new StringBuilder();
        boolean skip = false;

        for (int i = 0; i < len; i++) {
            if (skip) {
                skip = false;
            } else {
                char c = target[i];
                if ((c > 0x20 && c < 0x40) || (c > 0x5a && c < 0x61) || (c > 0x79 && c < 0xc0) || c == 0xd7 || c == 0xf7) {
                    result.append(c);
                } else if (Character.isDigit(c) || Character.isISOControl(c)) {
                    result.append(c);
                } else if (Character.isWhitespace(c) || Character.isLetter(c)) {
                    boolean isUpper = false;

                    switch (c) {
                        case '\u00df':
                            result.append("ss");
                            break;
                        /* Handling of capital and lowercase umlauts */
                        case 'A':
                        case 'O':
                        case 'U':
                            isUpper = true;
                        case 'a':
                        case 'o':
                        case 'u':
                            result.append(c);
                            if (i + 1 < target.length && target[i + 1] == 0x308) {
                                result.append('e');
                                skip = true;
                            }
                            break;
                        default:
                            Replacement rep = charMap.get((int) c);
                            if (rep != null) {
                                result.append(rep.lower);
                            } else
                                result.append(c);
                    }
                }
            }
        }

        return result.toString();
    }

    private static class Replacement {

        private final String lower;

        Replacement(String ucReplacement, String lcReplacement) {
            this.lower = lcReplacement;
        }

        Replacement(String caseInsensitiveReplacement) {
            this(caseInsensitiveReplacement, caseInsensitiveReplacement);
        }

    }
}
