package trackflow.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import trackflow.model.AthleteRoster;
import trackflow.model.ReadOnlyRoster;
import trackflow.model.person.Address;
import trackflow.model.person.AgeCategory;
import trackflow.model.person.Email;
import trackflow.model.person.Name;
import trackflow.model.person.Person;
import trackflow.model.person.Phone;
import trackflow.model.person.Remark;
import trackflow.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AthleteRoster} with sample data.
 */
public class SampleDataUtil {

    public static final Remark EMPTY_REMARK = new Remark("");

    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Avery Tan"), new AgeCategory("Under 14"), new Phone("91234567"),
                new Email("avery.tan@example.com"), new Address("23 Bishan Street 12, #05-110"),
                new Remark("Sprints: 100m and 200m"), getTagSet()),
            new Person(new Name("Daniel Lim"), new AgeCategory("Under 14"), new Phone("92345678"),
                new Email("daniel.lim@example.com"), new Address("Blk 88 Tampines Avenue 4, #09-21"),
                EMPTY_REMARK, getTagSet()),
            new Person(new Name("Priya Nair"), new AgeCategory("Under 16"), new Phone("93456789"),
                new Email("priya.nair@example.com"), new Address("17 Clementi Road, #02-08"),
                new Remark("Hurdles; recovering from ankle sprain"), getTagSet()),
            new Person(new Name("Marcus Wong"), new AgeCategory("Under 16"), new Phone("94567890"),
                new Email("marcus.wong@example.com"), new Address("Blk 301 Jurong East Street 32, #12-45"),
                EMPTY_REMARK, getTagSet()),
            new Person(new Name("Sofia Rahman"), new AgeCategory("Under 18"), new Phone("95678901"),
                new Email("sofia.rahman@example.com"), new Address("9 Pasir Ris Drive 6, #03-14"),
                new Remark("Middle distance: 800m and 1500m"), getTagSet()),
            new Person(new Name("Ethan Goh"), new AgeCategory("Under 18"), new Phone("96789012"),
                new Email("ethan.goh@example.com"), new Address("52 Hougang Avenue 7, #08-33"),
                EMPTY_REMARK, getTagSet()),
            new Person(new Name("Aisyah Salleh"), new AgeCategory("Under 20"), new Phone("97890123"),
                new Email("aisyah.salleh@example.com"), new Address("Blk 120 Bukit Merah Lane 1, #14-02"),
                new Remark("Long jump and triple jump"), getTagSet()),
            new Person(new Name("Jun Wei Chua"), new AgeCategory("Under 20"), new Phone("98901234"),
                new Email("junwei.chua@example.com"), new Address("4 Woodlands Crescent, #06-19"),
                EMPTY_REMARK, getTagSet()),
            new Person(new Name("Rachel Ong"), new AgeCategory("Open"), new Phone("81234567"),
                new Email("rachel.ong@example.com"), new Address("31 Sengkang East Way, #10-27"),
                new Remark("Team captain; relay anchor leg"), getTagSet())
        };
    }

    public static ReadOnlyRoster getSampleRoster() {
        AthleteRoster sampleRoster = new AthleteRoster();
        for (Person samplePerson : getSamplePersons()) {
            sampleRoster.addPerson(samplePerson);
        }
        return sampleRoster;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
