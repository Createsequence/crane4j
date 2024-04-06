package cn.crane4j.core.util;

import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link CharSequence} or {@link String} utils.
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class StringUtils {

    /**
     * Join the {@code array} with {@code separator}.
     *
     * @param mapper mapper
     * @param separator separator
     * @param array array
     * @return joined string
     */
    @SafeVarargs
    public static <T> String join(Function<T, CharSequence> mapper, String separator, T... array) {
        if (ArrayUtils.isEmpty(array)) {
            return "";
        }
        return join(Arrays.asList(array), mapper, separator);
    }

    /**
     * Join the {@code coll} with {@code separator}.
     *
     * @param coll collection
     * @param mapper mapper
     * @param separator separator
     * @return joined string
     */
    public static <T> String join(
        Collection<T> coll, Function<T, CharSequence> mapper, String separator) {
        if (CollectionUtils.isEmpty(coll)) {
            return "";
        }
        return coll.stream()
            .map(mapper)
            .collect(Collectors.joining(separator));
    }

    /**
     * <p>Whether the {@code searchStr} is in the {@code str}.<br />
     * eg:
     * <ul>
     *     <li>{@code "abc", "abc"} will return {@code true}</li>
     *     <li>{@code "abc", "b"} will return {@code true}</li>
     *     <li>{@code "abc", "d"} will return {@code false}</li>
     *     <li>{@code null, "a"} will return {@code false}</li>
     *     <li>{@code "a", null} will return {@code false}</li>
     *     <li>{@code null, null} will return {@code false}</li>
     * </ul>
     *
     * @param str the CharSequence to check, may be null
     * @param searchStr the CharSequence to find, may be null
     * @return true if the search CharSequence is in the CharSequence
     */
    public static boolean contains(CharSequence str, CharSequence searchStr) {
        // if all null, return false
        if (Objects.isNull(str) && Objects.isNull(searchStr)) {
            return false;
        }
        if (Objects.equals(str, searchStr)) {
            return true;
        }
        if (Objects.isNull(str) || Objects.isNull(searchStr)) {
            return false;
        }
        return str.toString().contains(searchStr);
    }

    /**
     * <p>Format string with placeholder {}.<br />
     * eg: {@code "a{}c", "b"} will return {@code "abc"}
     *
     * @param template template
     * @param args     args
     * @return formatted string
     */
    public static String format(String template, Object... args) {
        if (isEmpty(template) || ArrayUtils.isEmpty(args)) {
            return template;
        }
        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        int index = 0;
        while (cursor < template.length()) {
            int placeholderIndex = template.indexOf("{}", cursor);
            if (placeholderIndex == -1) {
                sb.append(template.substring(cursor));
                break;
            }
            sb.append(template, cursor, placeholderIndex);
            if (index < args.length) {
                sb.append(args[index++]);
            } else {
                sb.append("{}");
            }
            cursor = placeholderIndex + 2;
        }
        return sb.toString();
    }

    /**
     * <p>Whether the given {@link CharSequence} is empty.<br />
     * eg: {@code null, ""} will return true, {@code "a", " "} will return false.
     *
     * @param cs char sequence
     * @return is empty
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * <p>Whether the given {@link CharSequence} is not empty.
     * eg: {@code null, ""} will return false, {@code "a", " "} will return true.
     *
     * @param cs char sequence
     * @return is not empty
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * <p>return the {@code str} if {@code str} is not empty, otherwise return {@code defaultStr}.
     *
     * @param str str
     * @param defaultStr default str
     * @return str or default str
     */
    public static String emptyToDefault(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * <p>return the {@code str} if {@code str} is not empty, otherwise return {@code null}.
     *
     * @param str str
     * @return str or null
     */
    public static String emptyToNull(String str) {
        return isEmpty(str) ? null : str;
    }

    /**
     * <p>Make the first character uppercase and add prefix.<br />
     * eg: {@code "bc", "a"} will return {@code "aBc"}, {@code null, "a"} will return {@code "anull"}.
     *
     * @param str str
     * @param prefix prefix
     * @return str with first character uppercase and prefix
     */
    public static String upperFirstAndAddPrefix(String str, String prefix) {
        return prefix + upperFirst(str);
    }

    /**
     * <p>Make the first character uppercase.<br />
     * eg: {@code "abc"} will return {@code "Abc"}.
     *
     * @param str str
     * @return str with first character uppercase
     */
    public static String upperFirst(String str) {
        if (isEmpty(str)) {
            return str;
        }
        char[] charArray = str.toCharArray();
        charArray[0] = Character.toUpperCase(charArray[0]);
        return new String(charArray);
    }

    /**
     * <p>Whether the given {@link CharSequence} is blank.
     * eg: {@code null, "", " "} will return true, {@code "a"} will return false.
     *
     * @param cs char sequence
     * @return is blank
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Whether the given {@link CharSequence} is not blank.
     * eg: {@code null, "", " "} will return false, {@code "a"} will return true.
     *
     * @param cs char sequence
     * @return is not blank
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Md5 digest as hex.
     *
     * @param str str
     * @return md5 hex
     */
    public static String md5DigestAsHex(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] digest = md5.digest(str.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return str;
        }
    }

    /**
     * Split string by given splitter.
     *
     * @param str str
     * @param splitter splitter
     * @return string list
     */
    public static Collection<String> split(String str, String splitter) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }
        String[] split = str.split(splitter);
        return split.length > 1 ?
            Arrays.stream(split).map(String::trim).collect(Collectors.toList()) :
            Collections.singletonList(str);
    }
}
