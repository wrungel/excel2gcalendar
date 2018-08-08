package frol.excel2gcal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class NamesDictionaryCSVReader {

    private static final String[] FIRST_NAME_TYPOS = {"Михоил", "Стефамия", "Мадат", "Критина", "Ян", "Кейси", "Критина", "Алекснадр",
            "Адрини", "Анастпсия", "Яросла", "Александдра", "ИМИК", "Беаттриса", "Герамн", "Свавелий", "Тимофей1", "Маргорита" };

    static Set<String> readFromResource(String resourceName) {

            Set<String> result = new HashSet<>();
            InputStream inputStream = ExcelReader.class.getResourceAsStream(resourceName);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            try {
                while((line = in.readLine()) != null) {
                    int s1 = line.indexOf(';');
                    String name = line.substring(0, s1);
                    result.add(name.toLowerCase());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (String firstNameTypo : FIRST_NAME_TYPOS) {
                result.add(firstNameTypo.toLowerCase());
            }
            return result;

    }

}
