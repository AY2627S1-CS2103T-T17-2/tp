package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches athletes with the specified competition category.
 */
public class AgeCategoryPredicate implements Predicate<Person> {
    private final AgeCategory ageCategory;

    /**
     * Creates a predicate that matches an athlete's recorded {@code ageCategory}.
     *
     * @throws NullPointerException If {@code ageCategory} is null.
     */
    public AgeCategoryPredicate(AgeCategory ageCategory) {
        this.ageCategory = requireNonNull(ageCategory);
    }

    @Override
    public boolean test(Person person) {
        return ageCategory.equals(person.getAgeCategory());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AgeCategoryPredicate predicate
                && ageCategory.equals(predicate.ageCategory);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("ageCategory", ageCategory).toString();
    }
}
