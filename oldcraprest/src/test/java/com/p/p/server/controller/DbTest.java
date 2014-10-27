package com.p.p.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p.p.server.model.bean.*;
import com.p.p.server.model.repository.PictureRepository;
import com.p.p.server.model.repository.PostingRepository;
import com.p.p.server.model.repository.RoleRepository;
import com.p.p.server.model.repository.UserRepository;
import com.p.p.server.util.DBUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/applicationContext.xml")
public class DbTest {

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

    @Autowired
    DBUtils dbUtils;

    @Before
    @Transactional(readOnly=false)
    public void setup() throws IllegalAccessException, IOException, ClassNotFoundException {
        //typesMap = getSQLToJavaTypesMap();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        postingRepository.deleteAll();
        pictureRepository.deleteAll();
        fillData();
    }

    @Transactional(readOnly=false)
    private void fillData() {
        List<User> userList = userRepository.findAll();

        int initialCount = userList.size();

        User user1 = new User("Mihail Nenov", "mihail.n@gmail.com", "pass");
        User user2 = new User("Stancho Nenov", "stancho.n@gmail.com", "pass");
        userRepository.save(user1);
        userRepository.save(user2);

        Role role = new Role("ADMIN");
        role.getUsers().add(user1);
        role.getUsers().add(user2);
        roleRepository.save(role);

        Posting posting1 = new Posting(user2, "TV for present - like brand new one!");
        postingRepository.save(posting1);
        Picture picture1 = new Picture(posting1, "Front view - really nice!");
        pictureRepository.save(picture1);

        userList = userRepository.findAll();
        assertEquals(2, userList.size() - initialCount);
    }

    @Transactional(readOnly = true)
    private void dumpDbObject(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(DateFormat.getDateTimeInstance());
        System.out.println("Found object: \n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
    }

    @Test
    public void testNativeQuery() throws IOException, SQLException {
        dbUtils.dumpNativeQuery("select * from users");
    }

    @Test
    public void testContent() throws IOException, SQLException, SystemException, NotSupportedException {

        User foundUser = userRepository.getByMail("mihail.n@gmail.com");
        assertNotNull(foundUser);
        assertEquals(1, foundUser.getRoles().size());

        Role foundRole = foundUser.getRoles().iterator().next();
        assertEquals(2, foundRole.getUsers().size());

        dumpDbObject(foundUser);

        dumpDbObject(roleRepository.findAll());

        foundUser.setPhone("(359) 887 376 552");

        userRepository.save(foundUser);

        dbUtils.dumpNativeQuery("select * from users");

        dbUtils.dumpNativeQuery("select * from roles");

        dbUtils.dumpNativeQuery("select mail as username, password, enabled from users where mail='mihail.n@gmail.com'");

        dbUtils.dumpNativeQuery("select u.mail as username, r.name as authority from USERS u, ROLES r, USER_ROLES ur "
                + "where ur.role_id = r.id and ur.user_id = u.id and u.mail='mihail.n@gmail.com'");
    }

    @Test
    @Ignore
    public void testPreparedStatement() throws SQLException {
        PreparedStatement stmt = dataSource.getConnection().prepareStatement("select u.mail as username, r.name as authority from USERS u, ROLES r, USER_ROLES ur "
                + "where ur.role_id = r.id and ur.user_id = u.id and u.mail=?");

        stmt.setString(1, "mihail.n@gmail.com");
        ResultSet results = stmt.executeQuery();
        dbUtils.dumpStatementQuery(results);
    }
//
//    protected void dumpNativeQuery(String query) throws SQLException, IOException {
//        Statement stmt = dataSource.getConnection().createStatement();
//        ResultSet results = stmt.executeQuery(query);
//        dumpStatementQuery(results);
//    }
//
//    protected static void dumpStatementQuery(ResultSet results) throws SQLException {
//        ResultSetMetaData metaData = results.getMetaData();
//        String[] columns = extractColumnNames(metaData);
//        List<Object[]> tableData = new ArrayList<>();
//        while (results.next()) {
//            Object[] values = new Object[columns.length];
//            for (int i = 1; i <= columns.length; i++) {
//                if (metaData.getColumnType(i) == -3) {
//                    byte[] bytes = results.getBytes(i);
//                    values[i - 1] = new String(bytes);
//                } else {
//                    System.out.println("SQL Type: " + metaData.getColumnType(i) + "; Java type: " + typesMap.get(metaData.getColumnType(i)));
//                    values[i - 1] = results.getObject(i, typesMap.get(metaData.getColumnType(i)));
//                }
//            }
//            tableData.add(values);
//        }
//        dumpTable(columns, tableData);
//    }
//
//    private static String[] extractColumnNames(ResultSetMetaData metaData) throws SQLException {
//        String[] names = new String[metaData.getColumnCount()];
//        for (int i = 1; i <= names.length; i++) {
//            names[i - 1] = metaData.getColumnName(i);
//        }
//        return names;
//    }
//
//    private static void dumpTable(String[] header, List<Object[]> values) {
//        int[] lengths = calculateLengths(header, values);
//
//        String headerLine = createValueLine(lengths, header);
//
//        StringBuilder newBuilder = new StringBuilder(fullLine(headerLine.length() - 2));
//        newBuilder.append('\n').append(headerLine).append('\n').append(middleLine(lengths)).append('\n');
//
//        for (Object[] objects : values) {
//            newBuilder.append(createValueLine(lengths, objects)).append('\n').append(middleLine(lengths)).append('\n');
//        }
//        System.out.println(newBuilder);
//    }
//
//    private static String createValueLine(int[] lengths, Object[] values) {
//        StringBuilder builder = new StringBuilder("|");
//        for (int i = 0; i < lengths.length; i++) {
//            String h = values[i] != null ? values[i].toString() : "";
//            appendValue(builder, h, lengths[i]);
//        }
//        return builder.toString();
//    }
//
//    private static void appendValue(StringBuilder builder, String h, int length) {
//        builder.append(h);
//        for (int j = 0; j < (length - h.length()); j++) {
//            builder.append(" ");
//        }
//        builder.append("|");
//    }
//
//    private static String fullLine(int length) {
//        String result = "+";
//        for (int i = 0; i < length; i++) {
//            result += "-";
//        }
//        return result += "+";
//    }
//
//    private static String middleLine(int[] lengths) {
//        String result = "+";
//        for (int i = 0; i < lengths.length; i++) {
//            for (int j = 0; j < lengths[i]; j++) {
//                result += "-";
//            }
//            result += "+";
//        }
//        return result;
//    }
//
//    private static int[] calculateLengths(String[] header, List<Object[]> values) {
//        int[] columnLengths = new int[header.length];
//        for (int i = 0; i < header.length; i++) {
//            int l = header[i].length();
//            for (Object[] vs : values) {
//                l = (vs[i] != null && l < vs[i].toString().length()) ? vs[i].toString().length() : l;
//            }
//            columnLengths[i] = l;
//        }
//        return columnLengths;
//    }
//
//    private static Map<String, String> readCsvAsMap(String resource) throws IOException {
//        assert resource != null;
//
//        String[] lines = FileCopyUtils.copyToString(
//                new InputStreamReader(DbTest.class.getResourceAsStream(resource))).split("\\n");
//
//        Map<String, String> map = new HashMap<>(lines.length);
//
//        for (String l : lines) {
//            String[] values = l.split(",");
//            map.put(values[2], values[1]);
//        }
//
//        return map;
//    }
//
//    private static Map<Integer, String> getTypeCodes() throws IllegalAccessException {
//        Field[] fields = java.sql.Types.class.getFields();
//        Map<Integer, String> map = new HashMap<>(fields.length);
//
//        for (Field f : fields) {
//            map.put(new Integer(f.getInt(null)), f.getName());
//        }
//
//        return map;
//    }
//
//    private static Map<Integer, Class<?>> getSQLToJavaTypesMap() throws IllegalAccessException, IOException, ClassNotFoundException {
//        Map<String, String> dataTypesMap = readCsvAsMap("/data-mapping.csv");
//        Map<Integer, String> typeCodes = getTypeCodes();
//
//        assert dataTypesMap != null && typeCodes != null;
//
//        Map<Integer, Class<?>> map = new HashMap<>(typeCodes.size());
//
//        for (Map.Entry<Integer, String> entry : typeCodes.entrySet()) {
//            Integer code = entry.getKey();
//            String typeName = entry.getValue();
//
//            if (dataTypesMap.keySet().contains(typeName)) {
//                map.put(code, Class.forName(dataTypesMap.get(typeName)));
//            }
//        }
//
//        return map;
//    }
}
