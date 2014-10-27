package com.p.p.server.util;

import com.p.p.server.model.bean.Picture;
import com.p.p.server.model.bean.Posting;
import com.p.p.server.model.bean.Role;
import com.p.p.server.model.bean.User;
import com.p.p.server.model.repository.PictureRepository;
import com.p.p.server.model.repository.PostingRepository;
import com.p.p.server.model.repository.RoleRepository;
import com.p.p.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DBUtils {

    private static Map<Integer, Class<?>> typesMap;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PostingRepository postingRepository;

    @Autowired
    PictureRepository pictureRepository;

    @Autowired
    DataSource dataSource;

    public DBUtils() throws IllegalAccessException, IOException, ClassNotFoundException {
        typesMap = getSQLToJavaTypesMap();
    }

    public void dumpNativeQuery(String query) throws SQLException, IOException {
        Statement stmt = dataSource.getConnection().createStatement();
        ResultSet results = stmt.executeQuery(query);
        dumpStatementQuery(results);
    }

    public static void dumpStatementQuery(ResultSet results) throws SQLException {
        ResultSetMetaData metaData = results.getMetaData();
        String[] columns = extractColumnNames(metaData);
        List<Object[]> tableData = new ArrayList<>();
        while (results.next()) {
            Object[] values = new Object[columns.length];
            for (int i = 1; i <= columns.length; i++) {
                if (metaData.getColumnType(i) == -3) {
                    byte[] bytes = results.getBytes(i);
                    values[i - 1] = new String(bytes);
                } else {
                    values[i - 1] = results.getObject(i, typesMap.get(metaData.getColumnType(i)));
                }
            }
            tableData.add(values);
        }
        dumpTable(columns, tableData);
    }

    private static String[] extractColumnNames(ResultSetMetaData metaData) throws SQLException {
        String[] names = new String[metaData.getColumnCount()];
        for (int i = 1; i <= names.length; i++) {
            names[i - 1] = metaData.getColumnName(i);
        }
        return names;
    }

    private static void dumpTable(String[] header, List<Object[]> values) {
        int[] lengths = calculateLengths(header, values);

        String headerLine = createValueLine(lengths, header);

        StringBuilder newBuilder = new StringBuilder(fullLine(headerLine.length() - 2));
        newBuilder.append('\n').append(headerLine).append('\n').append(middleLine(lengths)).append('\n');

        for (Object[] objects : values) {
            newBuilder.append(createValueLine(lengths, objects)).append('\n').append(middleLine(lengths)).append('\n');
        }
        System.out.println(newBuilder);
    }

    private static String createValueLine(int[] lengths, Object[] values) {
        StringBuilder builder = new StringBuilder("|");
        for (int i = 0; i < lengths.length; i++) {
            String h = values[i] != null ? values[i].toString() : "";
            appendValue(builder, h, lengths[i]);
        }
        return builder.toString();
    }

    private static void appendValue(StringBuilder builder, String h, int length) {
        builder.append(h);
        for (int j = 0; j < (length - h.length()); j++) {
            builder.append(" ");
        }
        builder.append("|");
    }

    private static String fullLine(int length) {
        String result = "+";
        for (int i = 0; i < length; i++) {
            result += "-";
        }
        return result += "+";
    }

    private static String middleLine(int[] lengths) {
        String result = "+";
        for (int i = 0; i < lengths.length; i++) {
            for (int j = 0; j < lengths[i]; j++) {
                result += "-";
            }
            result += "+";
        }
        return result;
    }

    private static int[] calculateLengths(String[] header, List<Object[]> values) {
        int[] columnLengths = new int[header.length];
        for (int i = 0; i < header.length; i++) {
            int l = header[i].length();
            for (Object[] vs : values) {
                l = (vs[i] != null && l < vs[i].toString().length()) ? vs[i].toString().length() : l;
            }
            columnLengths[i] = l;
        }
        return columnLengths;
    }

    private static Map<String, String> readCsvAsMap(String resource) throws IOException {
        assert resource != null;

        String[] lines = FileCopyUtils.copyToString(
                new InputStreamReader(DBUtils.class.getResourceAsStream(resource))).split("\\n");

        Map<String, String> map = new HashMap<>(lines.length);

        for (String l : lines) {
            String[] values = l.split(",");
            map.put(values[2], values[1]);
        }

        return map;
    }

    private static Map<Integer, String> getTypeCodes() throws IllegalAccessException {
        Field[] fields = java.sql.Types.class.getFields();
        Map<Integer, String> map = new HashMap<>(fields.length);

        for (Field f : fields) {
            map.put(new Integer(f.getInt(null)), f.getName());
        }

        return map;
    }

    private static Map<Integer, Class<?>> getSQLToJavaTypesMap() throws IllegalAccessException, IOException, ClassNotFoundException {
        Map<String, String> dataTypesMap = readCsvAsMap("/data-mapping.csv");
        Map<Integer, String> typeCodes = getTypeCodes();

        assert dataTypesMap != null && typeCodes != null;

        Map<Integer, Class<?>> map = new HashMap<>(typeCodes.size());

        for (Map.Entry<Integer, String> entry : typeCodes.entrySet()) {
            Integer code = entry.getKey();
            String typeName = entry.getValue();

            if (dataTypesMap.keySet().contains(typeName)) {
                map.put(code, Class.forName(dataTypesMap.get(typeName)));
            }
        }

        return map;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
