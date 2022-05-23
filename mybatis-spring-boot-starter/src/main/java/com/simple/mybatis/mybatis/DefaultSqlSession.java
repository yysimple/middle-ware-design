package com.simple.mybatis.mybatis;

import com.simple.mybatis.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 13:40
 **/
public class DefaultSqlSession implements SqlSession {

    public static final String BOTTOM_LINE = "_";

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    public DefaultSqlSession() {
    }


    @Override
    public <T> T selectOne(String statement) {
        try {
            XNode xNode = configuration.getMapperElement().get(statement);
            PreparedStatement preparedStatement = configuration.getConnection().prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = resultSet2Obj(resultSet, xNode.getResultType());
            return objects.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        XNode xNode = configuration.getMapperElement().get(statement);
        // mapper.xml中拿到对应的参数字段
        Map<Integer, String> parameterMap = xNode.getParameter();
        try {
            PreparedStatement preparedStatement = configuration.getConnection().prepareStatement(xNode.getSql());
            buildParameter(preparedStatement, parameter, parameterMap);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = resultSet2Obj(resultSet, xNode.getResultType());
            return objects.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement) {
        XNode xNode = configuration.getMapperElement().get(statement);
        try {
            PreparedStatement preparedStatement = configuration.getConnection().prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, xNode.getResultType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        XNode xNode = configuration.getMapperElement().get(statement);
        Map<Integer, String> parameterMap = xNode.getParameter();
        try {
            PreparedStatement preparedStatement = configuration.getConnection().prepareStatement(xNode.getSql());
            buildParameter(preparedStatement, parameter, parameterMap);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, xNode.getResultType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        if (null == configuration.getConnection()) {
            return;
        }
        try {
            configuration.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buildParameter(PreparedStatement preparedStatement, Object parameter, Map<Integer, String> parameterMap) throws SQLException, IllegalAccessException {

        int size = parameterMap.size();
        // 单个参数
        if (parameter instanceof Long) {
            for (int i = 1; i <= size; i++) {
                preparedStatement.setLong(i, Long.parseLong(parameter.toString()));
            }
            return;
        }

        if (parameter instanceof Integer) {
            for (int i = 1; i <= size; i++) {
                preparedStatement.setInt(i, Integer.parseInt(parameter.toString()));
            }
            return;
        }

        if (parameter instanceof String) {
            for (int i = 1; i <= size; i++) {
                preparedStatement.setString(i, parameter.toString());
            }
            return;
        }

        Map<String, Object> fieldMap = new HashMap<>();
        // 对象参数解析成PreparedStatement需要的参数
        Field[] declaredFields = parameter.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            String name = field.getName();
            field.setAccessible(true);
            Object obj = field.get(parameter);
            field.setAccessible(false);
            fieldMap.put(name, obj);
        }

        for (int i = 1; i <= size; i++) {
            String parameterDefine = parameterMap.get(i);
            Object obj = fieldMap.get(parameterDefine);

            if (obj instanceof Short) {
                preparedStatement.setShort(i, Short.parseShort(obj.toString()));
                continue;
            }

            if (obj instanceof Integer) {
                preparedStatement.setInt(i, Integer.parseInt(obj.toString()));
                continue;
            }

            if (obj instanceof Long) {
                preparedStatement.setLong(i, Long.parseLong(obj.toString()));
                continue;
            }

            if (obj instanceof String) {
                preparedStatement.setString(i, obj.toString());
                continue;
            }

            if (obj instanceof Date) {
                preparedStatement.setDate(i, (java.sql.Date) obj);
            }

        }

    }

    /**
     * 转化成对应的JavaObject格式
     *
     * @param resultSet
     * @param className
     * @param <T>
     * @return
     */
    private <T> List<T> resultSet2Obj(ResultSet resultSet, String className) {
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Class<?> aliasClass = getAliasClass(className);
            T obj = (T) aliasClass.newInstance();
            // 每次遍历行值
            while (resultSet.next()) {
                // 处理列值
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    // 处理数据库字段名允许驼峰、非驼峰、下划线(这里默认开始)
                    columnName = mapperColumnToField(aliasClass, columnName);
                    // 这里是去获取每个字段对应的列信息
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    if (value instanceof Timestamp) {
                        method = aliasClass.getMethod(setMethod, Date.class);
                    } else {
                        method = aliasClass.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Class<?> getAliasClass(String className) throws Exception {
        Class<?> classNameClazz = Class.forName(className);
        if (className.contains(configuration.getTypeAliasesPackage())) {
            return classNameClazz;
        }
        // 拿到对应别名包下所有的类
        Set<Class<?>> classes = ObjectUtils.getClasses(configuration.getTypeAliasesPackage());
        List<Class<?>> collect = classes.stream()
                .filter(clazz -> !clazz.isAnonymousClass()).filter(clazz -> !clazz.isInterface())
                .filter(clazz -> !clazz.isMemberClass())
                .filter(clazz -> dealClazzName(clazz.getName()).equalsIgnoreCase(className)).collect(Collectors.toList());
        // mybatis中还使用了@Alias注解，同样不支持 同样的别名，我们这里简单处理
        if (collect.size() > 1) {
            throw new Exception("出现了重复的别名，自己看看哪里重复了");
        }
        Class<?> aClass = collect.get(0);
        return aClass;
    }

    private final static String DECIMAL_SEPARATOR = "\\.";

    private String dealClazzName(String clazzName) {
        if (StringUtils.isEmpty(clazzName)) {
            return "";
        }
        String[] split = clazzName.split(DECIMAL_SEPARATOR);
        String realClassName = split[split.length - 1];
        return realClassName;
    }

    /**
     * 这里有待优化，可以把字段信息放在缓存中，然后匹配到了就移除，无需每次都匹配所有字段，重复就抛出异常
     *
     * @param clazz
     * @param columnName
     * @return
     */
    public String mapperColumnToField(Class<?> clazz, String columnName) throws Exception {
        Field[] declaredFields = clazz.getDeclaredFields();
        Set<String> collect = Arrays.stream(declaredFields).map(Field::getName).collect(Collectors.toSet());
        if (configuration.getIsAllowedMapper()) {
            for (Field declaredField : declaredFields) {
                String fieldName = declaredField.getName();
                String lowerCase = fieldName.toLowerCase();
                String dealName = dealColumnName(columnName);
                if (lowerCase.equals(dealName)) {
                    return fieldName;
                }
            }
        } else {
            if (!collect.contains(columnName)) {
                throw new Exception("数据库字段无实体对象映射，赶紧去开启驼峰吧！！");
            }
            return columnName;
        }
        return columnName;
    }

    public String dealColumnName(String columnName) {
        String resColumnName = "";
        if (columnName.contains(BOTTOM_LINE)) {
            String[] split = columnName.split(BOTTOM_LINE);
            resColumnName = String.join("", split);
        } else {
            resColumnName = columnName;
        }

        return resColumnName.toLowerCase();
    }
}
