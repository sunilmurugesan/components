package uk.gov.hmrc.eos.util.excel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * {@code ExcelMapper} component provides one way mapping from Excel sheets to Java classes. It
 * converts each row of the specified Excel data into Java objects. It uses Apache Poi (the Java API
 * for Microsoft Documents) under the hood to perform the mapping process.
 * <p>
 * To use this component simply Autowire the {@code ExcelMapper} and call the {@code process(...)}
 * passing in either the {@link FileSystemResource} or the absolute path of the file including the
 * file name and the Class of the correct Bean defined as parameter type T.
 * <p>
 * It maps each row of the provided Excel data to the parameter type T defined in the bean class.
 */
@Component
@Slf4j
public class ExcelMapper {

  public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

  public <T> List<T> process(Resource resource, Class<T> beanClass) {
    Workbook workbook = openSpreadsheet(resource);
    List<T> mappedSpreadsheetData;
    try {
      Sheet sheet = workbook.getSheetAt(0); // Supports just one sheet ATM.
      mappedSpreadsheetData = mapSpreadsheetData(sheet, beanClass);
    } finally {
      closeWorkbook(workbook);
    }
    return mappedSpreadsheetData;
  }

  public <T> List<T> process(String filePath, Class<T> beanClass) {
    return this.process(new FileSystemResource(filePath), beanClass);
  }

  private <T> List<T> mapSpreadsheetData(Sheet sheet, Class<T> beanClass) {
    log.info("Mapping Spreadsheet data to bean {}", beanClass.getName());
    List<String> columnHeaders = getColumnHeaders(sheet.getRow(sheet.getFirstRowNum()));
    Map<String, Field> beanFields = beanFieldsByColumnName(beanClass);

    validateColumnHeaders(columnHeaders, beanFields);

    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                sheet.iterator(),
                Spliterator.ORDERED),
            false)
        .skip(1)
        .filter(row -> Objects.nonNull(row.getCell(0)))
        .map(row -> mapRow(row, beanClass, beanFields, columnHeaders))
        .collect(Collectors.toList());
  }

  private void validateColumnHeaders(List<String> columnHeaders, Map<String, Field> beanFields) {
    long duplicateCount = columnHeaders.stream()
        .filter(header -> Collections.frequency(columnHeaders, header) > 1)
        .count();
    log.info("Column headers in the Excel sheet {}", columnHeaders);
    log.info("Bean field mapped to Excel column {}", beanFields.keySet());
    if (duplicateCount > 0) {
      throw new ExcelMapperException("Duplicate column headers found");
    }
    if (columnHeaders.size() != beanFields.size()) {
      throw new ExcelMapperException("Invalid column header");
    }
    long invalidHeaders = columnHeaders.stream()
        .filter(header -> !beanFields.containsKey(header))
        .count();
    if (invalidHeaders > 0) {
      throw new ExcelMapperException("Invalid column header");
    }
  }

  private <T> T mapRow(Row row, Class<T> clazz, Map<String, Field> beanFields,
      List<String> columnHeaders) {
    T bean = instanceOf(clazz);
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                row.iterator(),
                Spliterator.ORDERED),
            false)
        .map(cell -> mapCellDataToField(cell, bean, beanFields, columnHeaders))
        .collect(Collectors.toSet())
        .stream().findFirst().orElse(null);
  }

  private <T> T mapCellDataToField(Cell cell, T bean, Map<String, Field> beanFields,
      List<String> columnHeaders) {
    if (cell.getColumnIndex() >= columnHeaders.size()) {
      throw new ExcelMapperException("Cell data not mapped correctly to a header");
    }
    Field field = beanFields.get(columnHeaders.get(cell.getColumnIndex()));
    setFieldData(bean, field, getCellData(cell));
    return bean;
  }

  private <T> T instanceOf(Class<T> clazz) {
    T bean;
    try {
      bean = clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to create a instance of the bean for the supplied class", e);
    }
    return bean;
  }

  private Object getCellData(Cell cell) {
    Object data = null;
    switch (cell.getCellType()) {
      case STRING:
        data = cell.getStringCellValue();
        break;
      case NUMERIC:
        data = DateUtil.isCellDateFormatted(cell) ? formatDate(cell.getDateCellValue())
            : String.valueOf(cell.getNumericCellValue());
        break;
    }
    return data;
  }

  private String formatDate(Object date) {
    return new SimpleDateFormat(ISO_DATE_FORMAT).format(date);
  }

  private <T> void setFieldData(T bean, Field field, Object data) {
    try {
      field.setAccessible(true);
      field.set(bean, data);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to set the data to the field. Fix the bean mapping");
    }
  }

  private static <T> Map<String, Field> beanFieldsByColumnName(Class<T> beanClass) {
    Map<String, Field> beanFields = new HashMap<>();
    Arrays.stream(beanClass.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(ExcelColumn.class))
        .forEach(field -> {
          ExcelColumn ec = field.getAnnotation(ExcelColumn.class);
          beanFields.put(ec.value(), field);
        });
    return beanFields;
  }

  private List<String> getColumnHeaders(Row headerRow) {
    return IntStream.range(headerRow.getFirstCellNum(), headerRow.getLastCellNum())
        .mapToObj(index -> getColumnHeaderName(headerRow.getCell(index)))
        .collect(Collectors.toList());
  }

  private String getColumnHeaderName(Cell cell) {
    if (cell == null) {
      throw new ExcelMapperException("Header cannot be empty");
    }
    return cell.getStringCellValue();
  }

  private Workbook openSpreadsheet(Resource resource) {
    Workbook workbook;
    try {
      log.info("Reading from the spreadsheet ...");
      workbook = WorkbookFactory.create(resource.getFile());
    } catch (IOException e) {
      throw new ExcelMapperException("Unable to find or open spreadsheet from the given location",
          e);
    }
    return workbook;
  }

  private void closeWorkbook(Workbook workbook) {
    try {
      log.info("Closing the spreadsheet ...");
      workbook.close();
    } catch (IOException e) {
      throw new RuntimeException("Unable to close the Workbook", e);
    }
  }
}
