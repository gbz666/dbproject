package com.database.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.enums.CellDataTypeEnum;

/**
 * EasyExcel 读取时把 Excel 数字格转为 String，避免 "Converter not found, convert NUMBER to java.lang.String"。
 */
public class ExcelNumberToStringConverter implements Converter<String> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        if (cellData == null) return null;
        if (cellData.getNumberValue() != null) return cellData.getNumberValue().toString();
        // 部分 Excel/格式下数字格会被读成字符串或 number 为 null，尝试取字符串值
        if (cellData.getStringValue() != null && !cellData.getStringValue().trim().isEmpty())
            return cellData.getStringValue().trim();
        return null;
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty,
                                                GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value);
    }
}
