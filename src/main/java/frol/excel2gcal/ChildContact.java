package frol.excel2gcal;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ChildContact {
    private final PersonName name;
    private final LocalDate birthDate;
    private final String parentName;
    private final PhoneNumber phoneNumber;
    private final String email;
    private final String newsletter;
    private final boolean vip;
    private final String restaurant;
    private final String district;
    private final String note;
    private final String source;
    private final String letter;

    public ChildContact(PersonName name, Date birthDate, String parentName, PhoneNumber phoneNumber, String email,
                        String newsletter, boolean vip, String restaurant, String district, String note, String source, String letter) {
        this.name = name;
        this.birthDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.parentName = parentName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.newsletter = newsletter;
        this.vip = vip;
        this.restaurant = restaurant;
        this.district = district;
        this.note = note;
        this.source = source;
        this.letter = letter;
    }

    public PersonName getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getParentName() {
        return parentName;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getNewsletter() {
        return newsletter;
    }

    public boolean isVip() {
        return vip;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public String getDistrict() {
        return district;
    }

    public String getNote() {
        return note;
    }

    public String getSource() {
        return source;
    }

    public String getLetter() {
        return letter;
    }
}
