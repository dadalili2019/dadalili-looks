package com.dadalili.utils;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author caoqian
 * @since 2023/4/6 12:32
 */
public class CsvFileUtil {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileUtil.class);

    public static final String FILE_SUFFIX = ".csv";
    public static final String CSV_DELIMITER = ",";
    public static final String CSV_TAIL = "\r\n";
    protected static final String DATE_STR_FILE_NAME = "yyyyMMddHHmmssSSS";


    /**
     * 将字符串转成csv文件
     */
    public static void createCsvFile(String savePath, String contextStr) throws IOException {

        File file = new File(savePath);
        //创建文件
        file.createNewFile();
        //创建文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        //将指定字节写入此文件输出流
        fileOutputStream.write(contextStr.getBytes("gbk"));
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    /**
     * 写文件
     *
     * @param fileName
     * @param content
     */
    public static void writeFile(String fileName, String content) {
        FileOutputStream fos = null;
        OutputStreamWriter writer = null;
        try {
            fos = new FileOutputStream(fileName, true);
            writer = new OutputStreamWriter(fos, "GBK");
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            logger.error("写文件异常|{}", e);
        } finally {
            if (fos != null) {
                IOUtils.closeQuietly(fos);
            }
            if (writer != null) {
                IOUtils.closeQuietly(writer);
            }
        }
    }

    /**
     * 构建文件名称
     *
     * @param dataList
     * @return
     */
    public static String buildCsvFileFileName(List dataList) {
        return dataList.get(0).getClass().getSimpleName() + new SimpleDateFormat(DATE_STR_FILE_NAME).format(new Date()) + FILE_SUFFIX;
    }

    /**
     * 构建excel 标题行名
     *
     * @param dataList
     * @return
     */
    public static String buildCsvFileTableNames(List dataList) {
        Map<String, Object> map = toMap(dataList.get(0));
        StringBuilder tableNames = new StringBuilder();
        for (String key : map.keySet()) {
            tableNames.append(key).append(CsvFileUtil.CSV_DELIMITER);
        }
        return tableNames.append(CsvFileUtil.CSV_TAIL).toString();
    }

    /**
     * 构建excel内容
     *
     * @param dataLists
     * @return
     */
    public static String buildCsvFileBodyMap(List dataLists) {
        //不管你传什么玩意，我都给你反射一手，搞成Map
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object o : dataLists) {
            mapList.add(toMap(o));
        }
        //然后利用csv格式，对着map嘎嘎一顿拼接数据
        StringBuilder lineBuilder = new StringBuilder();
        for (Map<String, Object> rowData : mapList) {
            for (String key : rowData.keySet()) {
                Object value = rowData.get(key);
                if (Optional.ofNullable(value).isPresent()) {
                    lineBuilder.append(value).append(CsvFileUtil.CSV_DELIMITER);
                } else {
                    lineBuilder.append("--").append(CsvFileUtil.CSV_DELIMITER);
                }
            }
            lineBuilder.append(CsvFileUtil.CSV_TAIL);
        }
        return lineBuilder.toString();
    }

    /**
     * 类转map
     *
     * @param entity
     * @param <T>
     * @return
     */
    public static <T> Map<String, Object> toMap(T entity) {
        Class<? extends Object> bean = entity.getClass();
        Field[] fields = bean.getDeclaredFields();
        Map<String, Object> map = new HashMap<>(fields.length);
        for (Field field : fields) {
            try {
                if (!"serialVersionUID".equals(field.getName())) {
                    String methodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                    Method method = bean.getDeclaredMethod(methodName);
                    Object fieldValue = method.invoke(entity);
                    map.put(field.getName(), fieldValue);
                }
            } catch (Exception e) {
                logger.warn("toMap() Exception={}" + e.getMessage());
            }
        }
        return map;
    }
}
