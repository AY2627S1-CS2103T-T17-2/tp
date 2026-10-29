// @@author qiyuannn
package trackflow.model.person;
// @@author

// @@author qiyuannn
import static trackflow.commons.util.CollectionUtil.requireAllNonNull;
// @@author

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// @@author qiyuannn
import trackflow.commons.util.ToStringBuilder;
import trackflow.model.tag.Tag;
// @@author

/**
 * Represents a Person in the athlete roster.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Athlete details; duplicate identity is defined by isSamePerson.
    private final Name name;
    private final AgeCategory ageCategory;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Remark remark;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Constructs an athlete in the Open age category with the supplied details.
     * All fields must be non-null; the address and remark may contain empty values.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags) {
        this(name, new AgeCategory("Open"), phone, email, address, remark, tags);
    }

    /**
     * Constructs an athlete with an explicit age category and a defensive copy of the supplied tags.
     * All fields must be non-null; the address and remark may contain empty values.
     */
    public Person(Name name, AgeCategory ageCategory, Phone phone, Email email,
            Address address, Remark remark, Set<Tag> tags) {
        requireAllNonNull(name, ageCategory, phone, email, address, remark, tags);
        this.ageCategory = ageCategory;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        this.tags.addAll(tags);
    }

    public Name getName() {
        return name;
    }

    public AgeCategory getAgeCategory() {
        return ageCategory;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Remark getRemark() {
        return remark;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if the normalized name and either phone or email match, or this is the same object.
     * Name comparison ignores case and repeated whitespace; contact comparison follows
     * {@link #hasSameContactDetails(Person)}. Age category and other stored details do not affect duplicate identity.
     * Returns false for null. Use {@link #equals(Object)} to compare all fields using their respective equality rules.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && normalizeName(name).equalsIgnoreCase(normalizeName(otherPerson.name))
                && hasSameContactDetails(otherPerson);
    }

    /**
     * Returns true if either the phone's digits or the email matches those of {@code otherPerson}.
     * Email comparison ignores case and surrounding whitespace. Names are not compared; null returns false.
     */
    public boolean hasSameContactDetails(Person otherPerson) {
        return otherPerson != null
                && (phone.value.replaceAll("\\D", "").equals(otherPerson.phone.value.replaceAll("\\D", ""))
                || email.value.trim().equalsIgnoreCase(otherPerson.email.value.trim()));
    }

    private static String normalizeName(Name name) {
        return name.fullName.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns true when every field compares equal using its own equality rules.
     * Names and email addresses are case-sensitive. Phone formatting and event-name case are handled by
     * {@link Phone#equals(Object)} and {@link Tag#equals(Object)}, respectively.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && ageCategory.equals(otherPerson.ageCategory)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && remark.equals(otherPerson.remark)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, ageCategory, phone, email, address, remark, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("ageCategory", ageCategory)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("remark", remark)
                .add("tags", tags)
                .toString();
    }

}
