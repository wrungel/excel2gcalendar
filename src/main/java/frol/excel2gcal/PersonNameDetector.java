package frol.excel2gcal;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;

import com.google.common.base.Splitter;

class PersonNameDetector {
    private final Set<String> firstNamesDict;
    private static final Splitter LIST_OF_FIRST_NAMES_SPLITTER = Splitter.on(Pattern.compile("[,/](\\s)*"));

    PersonNameDetector(Set<String> firstNamesDict) {
        this.firstNamesDict = firstNamesDict;
    }

    PersonName detect(Cell cell) {
        if (isListOfFirstNames(cell.getStringCellValue())) {
            return new PersonName(null, cell.getStringCellValue(), null);
        }
        String[] names = cell.getStringCellValue().split("\\s+");
        switch (names.length) {
            case 1: // only firstName
                if (!isFirstName(names[0])) {
                    throw new InvalidChildNameException("unknown first name");
                }
                return new PersonName(names[0], null, null);
            case 2: // <firstName> <secondName> or <secondName> <firstName>
                if (!isFirstName(names[0]) && !isFirstName(names[1])) {
                    throw new InvalidChildNameException("neither first nor second part is a known first name");
                }
                if (isFirstName(names[0]) && isFirstName(names[1])) {
                    throw new InvalidChildNameException("ambiguous first name");
                }
                String firstName;
                String secondName;
                if (isFirstName(names[0])) {
                    firstName = names[0];
                    secondName = names[1];
                } else {
                    firstName = names[1];
                    secondName = names[0];
                }
                return new PersonName(secondName, firstName, null);
            case 3:  // <secondName> <firstName> <middleName>
                if (!isFirstName(names[1])) {
                    throw new InvalidChildNameException("first name '" + names[1] + "' is unknown");
                }
                return new PersonName(names[0], names[1], names[2]);
            default:
                throw new InvalidChildNameException("more than three parts");
        }
    }

    private boolean isListOfFirstNames(String s) {
        for (String s1 : LIST_OF_FIRST_NAMES_SPLITTER.split(s)) {
            if (!firstNamesDict.contains(s1.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private boolean isFirstName(String s) {
        return firstNamesDict.contains(s.toLowerCase()) || isListOfKnownFirstNames(s);
    }


    private boolean isListOfKnownFirstNames(String s) {
        for (String s1 : s.split("[,/]")) {
            if (!firstNamesDict.contains(s1.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
