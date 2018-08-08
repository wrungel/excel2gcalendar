package frol.excel2gcal;

import static org.apache.poi.ss.usermodel.CellType.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

class ExcelReader {
    private static final Logger LOG = Logger.getLogger(ExcelReader.class.getSimpleName());

    private PersonNameDetector personNameDetector;

    static class Stats {
        private int childNameMissing = 0;
        private int childNameInvalid = 0;
        private int phoneNumberMissing = 0;
        private int birthdayFormatUnknown = 0;
        private int birthdayMissing = 0;
        private int birthdayFormatOK = 0;
    }

    private Stats stats;

    ExcelReader(PersonNameDetector personNameDetector) {
        this.personNameDetector = personNameDetector;
    }

    List<ChildContact> parse(String fileName) {
        stats = new Stats();
        List<ChildContact> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File(fileName))) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row cells : sheet) {
                if (cells.getRowNum() == 0) {
                    continue; // skip header row
                }
                ChildContact childContact = parseRow(cells);
                if (childContact != null) {
                    result.add(childContact);
                }
            }

            LOG.info(() -> "birthdayFormatUnknown: " + stats.birthdayFormatUnknown);
            LOG.info(() -> "birthdayMissing: " + stats.birthdayMissing);
            LOG.info(() -> "birthdayFormatOK: " + stats.birthdayFormatOK);
            LOG.info(() -> "childNameMissing: " + stats.childNameMissing);
            LOG.info(() -> "childNameInvalid: " + stats.childNameInvalid);
            LOG.info(() -> "phoneNumberMissing: " + stats.phoneNumberMissing);
        } catch (InvalidFormatException | IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private ChildContact parseRow(Row cells) {
        Date birthDate = parseBirthDate(cells.getCell(3));
        if (birthDate == null) {
            return null;
        }
        stats.birthdayFormatOK++;

        Cell childNameCell = cells.getCell(1);
        if (isChildNameCellBlankOrMissing(childNameCell)) {
            stats.childNameMissing++;
            return null;
        }

        PersonName childName;
        try {
            childName = personNameDetector.detect(childNameCell);
        } catch (InvalidChildNameException e) {
            stats.childNameInvalid++;
            warn(childNameCell, "childNameInvalid: %s", e.getMessage());
            return null;
        }

        String parentName = getStringCellValue(cells.getCell(0));
        PhoneNumber phoneNumber = parsePhoneNumber(cells.getCell(4));
        if (phoneNumber == null) {
            stats.phoneNumberMissing++;
        }

        String email = getStringCellValue(cells.getCell(6));
        String newsletter = getStringCellValue(cells.getCell(7));
        boolean vip = parseVip(cells.getCell(8));
        String restaurant = getStringCellValue(cells.getCell(9));
        String district = getStringCellValue(cells.getCell(10));
        String note = getStringCellValue(cells.getCell(11));
        String source = getStringCellValue(cells.getCell(12));
        String letter = getStringCellValue(cells.getCell(13));

        return new ChildContact(childName, birthDate, parentName, phoneNumber, email,
                newsletter, vip, restaurant, district, note, source, letter);
    }

    private String getStringCellValue(Cell cell) {
        if (cell.getCellTypeEnum() == CellType.BLANK) {
            return null;
        }
        return cell.getStringCellValue().trim();
    }

    private boolean parseVip(Cell cell) {
        if (cell.getCellTypeEnum() == CellType.BLANK) {
            return false;
        }
        if (cell.getCellTypeEnum() != STRING) {
            warn(cell, "vip cell has wrong type, expected: STRING, actual: %s", cell.getCellTypeEnum().name());
            return false;
        }
        String value = cell.getStringCellValue().trim();
        if (value.equalsIgnoreCase("нет")) {
            return false;
        } else if (value.equalsIgnoreCase("Да") || value.equalsIgnoreCase("ВИП")) {
            return true;
        } else {
            warn(cell, "vip cell: invalid value: expected one of: {нет, Да, ВИП}, actual: '%s'");
            return false;
        }
    }

    private Date parseBirthDate(Cell cell) {
        CellType cellType = cell.getCellTypeEnum();
        if (cellType == CellType.BLANK) {
            stats.birthdayMissing++;
            return null;
        }

        if (cellType != CellType.NUMERIC) {
            warn(cell, "birthdayFormatUnknown");
            stats.birthdayFormatUnknown++;
            return null;
        }
        return cell.getDateCellValue();
    }

    private PhoneNumber parsePhoneNumber(Cell cell) {
        if (cell.getCellTypeEnum() == CellType.BLANK) {
            return null;
        }

        String value;
        if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            value = Long.toString(BigDecimal.valueOf(cell.getNumericCellValue()).longValue());
        } else {
            value = cell.getStringCellValue().replace(" ", "").replace("-", "");
            if (value.isEmpty()) {
                return null;
            }
        }

        PhoneNumber phoneNumber;
        if ((value.startsWith("7") || value.startsWith("8")) && value.length() == 11) {
            phoneNumber = new PhoneNumber("7", value.substring(1, 4), value.substring(4));
        } else if (value.length() == 7) {
            phoneNumber = new PhoneNumber("7", "812", value);
        } else if (value.length() == 10) {
            phoneNumber = new PhoneNumber("7", value.substring(0, 3), value.substring(3));
        } else {
            throw new RuntimeException(format(cell, "invalid phone number: '%s'", value));
        }
        return phoneNumber;
    }

    private boolean isChildNameCellBlankOrMissing(Cell childNameCell) {
        return childNameCell.getCellTypeEnum() == CellType.BLANK ||
                (childNameCell.getCellTypeEnum() == STRING && childNameCell.getStringCellValue().equals("?"));
    }

    private void warn(Cell cell, String format, String... arguments) {
        LOG.log(Level.WARNING, () -> format(cell, format, (Object[]) arguments));
    }

    private String format(Cell cell, String format, Object... arguments) {
        return String.format("at %d:%d: '%s': ", cell.getRowIndex(), cell.getColumnIndex(), cellValue(cell))
                + String.format(format, arguments);
    }

    private String cellValue(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case BLANK: return "";
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return Double.toString(cell.getNumericCellValue());
            case BOOLEAN: return Boolean.toString(cell.getBooleanCellValue());
            case ERROR: return Byte.toString(cell.getErrorCellValue());
            case FORMULA: return cell.getCellFormula();
            case _NONE: return null;
            default:
                throw new IllegalStateException(cell.getCellTypeEnum().name());
        }
    }

}
