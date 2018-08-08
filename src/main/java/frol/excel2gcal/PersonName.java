package frol.excel2gcal;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PersonName {
    private final String secondName;
    private final String firstName;
    private final String middleName;

    PersonName(String secondName, String firstName, String middleName) {
        this.secondName = secondName;
        this.firstName = firstName;
        this.middleName = middleName;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    @Override
    public String toString() {
        return Stream.of(secondName, firstName, middleName).filter(Objects::nonNull).collect(Collectors.joining(" "));
    }
}
