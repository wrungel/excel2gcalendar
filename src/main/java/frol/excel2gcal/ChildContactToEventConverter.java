package frol.excel2gcal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.common.collect.Lists;

class ChildContactToEventConverter {
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private String summary(ChildContact c) {
        List<String> names = new ArrayList<>();
        if (c.getName().getSecondName() != null) {
            names.add(c.getName().getSecondName());
        }
        if (c.getName().getFirstName() != null) {
            names.add(c.getName().getFirstName());
        }
        if (c.getName().getMiddleName() != null) {
            names.add(c.getName().getMiddleName());
        }
        return names.stream().filter(Objects::nonNull).collect(Collectors.joining(" "));
    }

    Event convert(ChildContact c) {
        Event event = new Event();

        event.setSummary(summary(c));

        StringBuilder desc = new StringBuilder();
        desc.append("<table>");
        desc.append(String.format("<tr><td>Родители:</td><td>%s</td></tr>\n",
                c.getParentName() != null ? c.getParentName() : "-"));
        if (c.getPhoneNumber() != null) {
            desc.append(String.format("<tr><td>Tel:</td><td><a href=\"tel:%s\">%s</a></td></tr>\n",
                    String.format("+%s%s%s", c.getPhoneNumber().getCountry(), c.getPhoneNumber().getPrefix(), c.getPhoneNumber().getNumber()),
                    String.format("+%s %s %s", c.getPhoneNumber().getCountry(), c.getPhoneNumber().getPrefix(), c.getPhoneNumber().getNumber())));
        } else {
            desc.append("<tr><td>Tel:</td><td>-</td></tr>");
        }
        if (c.getEmail() != null) {
            desc.append(String.format("<tr><td>Email:</td><td><a href=\"mailto:%s\">%s</a></td></tr>\n", c.getEmail(), c.getEmail()));
        } else {
            desc.append(String.format("<tr><td>Email:</td><td>-</td></tr>\n", c.getEmail(), c.getEmail()));
        }
        desc.append(String.format("<tr><td>День рождения:</td><td>%s</td></tr>\n", LOCAL_DATE_FORMATTER.format(c.getBirthDate())));
        desc.append(String.format("<tr><td>Район:</td><td>%s</td></tr>\n",
                c.getDistrict() != null ? c.getDistrict() : "-"));
        desc.append(String.format("<tr><td>Ресторан:</td><td>%s</td></tr>\n", c.getRestaurant() != null ? c.getRestaurant() : "-"));
        if (c.getNote() != null) {
            desc.append(String.format("<tr><td>Комментарии:</td><td>%s</td></tr>\n", c.getName()));
        }
        if (c.getSource() != null) {
            desc.append(String.format("<tr><td>Источник:</td><td>%s</td></tr>\n", c.getSource()));
        }
        if (c.getLetter() != null) {
            desc.append(String.format("<tr><td>Письмо:</td><td>%s</td></tr>\n", c.getLetter()));
        }
        if (c.getNewsletter() != null) {
            desc.append(String.format("<tr><td>Рассылка:</td><td>%s</td></tr>\n", c.getNewsletter()));
        }
        desc.append(String.format("<tr><td>VIP:</td><td>%s</td></tr>\n", c.isVip() ? "да" : "нет"));
        desc.append("</table>");
        event.setDescription(desc.toString());
        LocalDate birthDate = c.getBirthDate();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        event.setStart(new EventDateTime().setDate(new DateTime(dateFormat.format(toDate(birthDate)))));
        event.setEnd(new EventDateTime().setDate(new DateTime(dateFormat.format(toDate(birthDate.plusDays(1))))));

        event.setTransparency("transparent");
        Event.Reminders reminders = new Event.Reminders()
                .setOverrides(Lists.newArrayList(
                        new EventReminder().setMethod("email").setMinutes(24*60-10*60),
                        new EventReminder().setMethod("email").setMinutes(28*24*60-10*60)))
                .setUseDefault(false);

        event.setReminders(reminders);
        String[] recurrence = new String[] {"RRULE:FREQ=YEARLY"};
        event.setRecurrence(Arrays.asList(recurrence));
        return event;
    }

    Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
