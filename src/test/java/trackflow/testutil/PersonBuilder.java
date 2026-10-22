package trackflow.testutil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import trackflow.model.person.Address;
import trackflow.model.person.AgeCategory;
import trackflow.model.person.Email;
import trackflow.model.person.Name;
import trackflow.model.person.Performance;
import trackflow.model.person.Person;
import trackflow.model.person.Phone;
import trackflow.model.person.Remark;
import trackflow.model.tag.Tag;
import trackflow.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";
    public static final String DEFAULT_REMARK = "She likes aardvarks.";

    private Name name;
    private AgeCategory ageCategory = new AgeCategory("Open");
    private Phone phone;
    private Email email;
    private Address address;
    private Remark remark;
    private Set<Tag> tags;
    private List<Performance> performances;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        remark = new Remark(DEFAULT_REMARK);
        tags = new HashSet<>();
        performances = new ArrayList<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        ageCategory = personToCopy.getAgeCategory();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        remark = personToCopy.getRemark();
        tags = new HashSet<>(personToCopy.getTags());
        performances = new ArrayList<>(personToCopy.getPerformances());
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the {@code Remark} of the {@code Person} that we are building.
     */
    public PersonBuilder withRemark(String remark) {
        this.remark = new Remark(remark);
        return this;
    }

    /**
     * Sets the athlete's age category.
     */
    public PersonBuilder withAgeCategory(String category) {
        ageCategory = new AgeCategory(category);
        return this;
    }

    /**
     * Sets the athlete's recorded performances. Each entry is {@code "EVENT|RESULT"} or
     * {@code "EVENT|RESULT|YYYY-MM-DD"}.
     */
    public PersonBuilder withPerformances(String... performances) {
        this.performances = new ArrayList<>();
        for (String entry : performances) {
            String[] parts = entry.split("\\|", -1);
            LocalDate date = parts.length > 2 && !parts[2].isEmpty() ? LocalDate.parse(parts[2]) : null;
            this.performances.add(new Performance(parts[0], parts[1], date));
        }
        return this;
    }

    /**
     * Builds the {@code Person} with the configured details.
     */
    public Person build() {
        return new Person(name, ageCategory, phone, email, address, remark, tags,
                new ArrayList<>(performances));
    }

}
