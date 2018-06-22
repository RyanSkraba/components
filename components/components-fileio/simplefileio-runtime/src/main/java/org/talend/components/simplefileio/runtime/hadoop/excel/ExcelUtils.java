// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.simplefileio.runtime.hadoop.excel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * some utils methods for excel files
 */
public class ExcelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     *
     * @param cell
     * @param formulaEvaluator
     * @return return the cell value as String (if needed evaluate the existing formula)
     */
    public static String getCellValueAsString(Cell cell, FormulaEvaluator formulaEvaluator) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        switch (cell.getCellTypeEnum()) {
        case BLANK:
            return "";
        case BOOLEAN:
            return cell.getBooleanCellValue() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
        case ERROR:
            return "Cell Error type";
        case FORMULA:
            try {
                return getCellValueAsString(cell, formulaEvaluator.evaluate(cell));
            } catch (Exception e) {
                // log error message and the formula
                LOGGER.warn("Unable to evaluate cell (line: {}, col: {}) with formula '{}': {}", cell.getRowIndex(),
                        cell.getColumnIndex(), cell.getCellFormula(), e.getMessage(), e);
                return StringUtils.EMPTY;
            }
        case NUMERIC:
            return getNumericValue(cell, null, false);
        case STRING:
            //TODO which is better? StringUtils.trim(cell.getStringCellValue())
            return cell.getRichStringCellValue().getString();
        default:
            return "Unknown Cell Type: " + cell.getCellTypeEnum();
        }
    }

    /**
     *
     * @param cell
     * @param cellValue
     * @return internal method which switch on the formula result value type then return a String value
     */
    private static String getCellValueAsString(Cell cell, CellValue cellValue) {
        if (cellValue == null) {
            return StringUtils.EMPTY;
        }
        switch (cellValue.getCellTypeEnum()) {
        case BLANK:
            return "";
        case BOOLEAN:
            return cellValue.getBooleanValue() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
        case ERROR:
            return "Cell Error type";
        case NUMERIC:
            return getNumericValue(cell, cellValue, cellValue != null);
        case STRING:
            //TODO which is better? StringUtils.trim(cell.getStringCellValue())
            return cell.getRichStringCellValue().getString();
        default:
            return "Unknown Cell Type: " + cell.getCellTypeEnum();
        }
    }

    //TODO configurable?
    //private static final DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);//this is the one dataprep use for excel 97
    private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    
    //TODO use this for number?
//    private static DecimalFormat df = new DecimalFormat("#.####################################", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    
    //Numeric type (use data formatter to get number format right)
    private static final DataFormatter formatter = new DataFormatter(Locale.ENGLISH);//this is the one dataprep use for excel 97
    
    /**
     * Return the numeric value.
     *
     * @param cell the cell to extract the value from.
     * @return the numeric value from the cell.
     */
    private static String getNumericValue(Cell cell, CellValue cellValue, boolean fromFormula) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return sdf.format(cell.getDateCellValue());
        }
        
        if (cellValue == null) {
            return formatter.formatCellValue(cell);
        }

        return fromFormula ? cellValue.formatAsString() : formatter.formatCellValue(cell);
    }
    
    public static boolean isEmptyRow(Row row) {
      if (row == null) {
          return true;
      }
      
      if (row.getLastCellNum() < 1) {
          return true;
      }
      
      for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
          Cell cell = row.getCell(cellNum);
          if (cell != null && cell.getCellTypeEnum() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())) {
              return false;
          }
      }
      return true;
    }
    
    public static boolean isEmptyRow4Stream(Row row) {
      if (row == null) {
          return true;
      }
      
      for (Cell cell : row) {
          if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK  && StringUtils.isNotBlank(cell.toString())) {
              return false;
          }
      }
      return true;
    }

}