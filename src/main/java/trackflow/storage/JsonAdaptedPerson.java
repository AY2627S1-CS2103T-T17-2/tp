// @@author qiyuannn
package trackflow.storage;
// @@author

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// @@author qiyuannn
import trackflow.commons.exceptions.IllegalValueException;
import trackflow.model.person.Address;
import trackflow.model.person.AgeCategory;
import trackflow.model.person.Email;
import trackflow.model.person.Name;
import trackflow.model.person.Person;
import trackflow.model.person.Phone;
import trackflow.model.person.Remark;
import trackflow.model.tag.Tag;
// @@author

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Athlete's %s field is missing!";

    private final String name;
    private final String ageCategory;
    private final String phone;
    private final String email;
    private final String address;
    private final String remark;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            String remark, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, remark, tags, null);
    }

    /**
     * Constructs a saved athlete, with missing categories defaulting to Open on load.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("remark") String remark, @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("ageCategory") String ageCategory) {
        this.ageCategory = ageCategory;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        ageCategory = source.getAgeCategory().value;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        remark = source.getRemark().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException("Athlete's tags contain a null tag!");
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);
        if (ageCategory != null && !AgeCategory.isValidAgeCategory(ageCategory)) {
            throw new IllegalValueException(AgeCategory.MESSAGE_CONSTRAINTS);
        }
        final AgeCategory modelAgeCategory = new AgeCategory(ageCategory == null ? "Open" : ageCategory);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        // Address and remark are optional data: a saved athlete that is missing either (for example, from an
        // older file) loads with an empty value rather than making the whole file unreadable.
        final String addressValue = address == null ? "" : address;
        if (!Address.isValidAddress(addressValue)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(addressValue);

        final Remark modelRemark = new Remark(remark == null ? "" : remark);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        return new Person(modelName, modelAgeCategory, modelPhone, modelEmail, modelAddress, modelRemark, modelTags);
    }

}
