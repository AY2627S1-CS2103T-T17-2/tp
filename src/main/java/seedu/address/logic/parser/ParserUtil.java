package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.AgeCategory;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    /**
     * Matches the CLI's reserved parameter-prefix shapes: a single letter followed by {@code /}, or {@code addr/}.
     * Longer words followed by a slash are treated as field content, such as {@code Mon/Tue} in a remark.
     */
    private static final Pattern PARAMETER_PREFIX_PATTERN =
            Pattern.compile("(?<!\\S)((?i:addr/)|[A-Za-z]/)");

    /**
     * Parses an age category using its canonical display form.
     */
    public static AgeCategory parseAgeCategory(String category) throws ParseException {
        requireNonNull(category);
        if (!AgeCategory.isValidAgeCategory(category)) {
            throw new ParseException(AgeCategory.MESSAGE_CONSTRAINTS);
        }
        return new AgeCategory(category);
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        BigInteger parsedIndex = new BigInteger(trimmedIndex);
        BigInteger maximumIndex = BigInteger.valueOf(Integer.MAX_VALUE);
        int boundedIndex = parsedIndex.min(maximumIndex).intValue();
        return Index.fromOneBased(boundedIndex);
    }

    /**
     * Rejects the first parameter-like prefix that is not supported by the command.
     */
    public static void verifyNoUnknownPrefixes(String arguments, String commandUsage,
            Prefix... allowedPrefixes) throws ParseException {
        requireNonNull(arguments);
        requireNonNull(commandUsage);
        Set<String> allowed = new HashSet<>();
        for (Prefix prefix : allowedPrefixes) {
            allowed.add(prefix.getPrefix().toLowerCase(Locale.ROOT));
        }

        Matcher matcher = PARAMETER_PREFIX_PATTERN.matcher(arguments);
        while (matcher.find()) {
            String suppliedPrefix = matcher.group(1);
            if (!allowed.contains(suppliedPrefix.toLowerCase(Locale.ROOT))) {
                throw new ParseException(String.format(Messages.MESSAGE_UNKNOWN_PARAMETER,
                        suppliedPrefix, commandUsage));
            }
        }
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (trimmedAddress.isEmpty() || !Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
