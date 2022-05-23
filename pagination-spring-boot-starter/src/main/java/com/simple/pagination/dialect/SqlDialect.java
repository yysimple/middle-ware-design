package com.simple.pagination.dialect;

import com.simple.pagination.exception.SqlDialectException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 17:45
 */
public enum SqlDialect {
    /**
     * 支持的多种数据源
     */
    DB2("DB2", DB2Dialect.class),
    DERBY("Derby", DerbyDialect.class),
    H2("H2", PostgreSQLDialect.class),
    HIVE("Apache Hive", DB2Dialect.class),
    HSQLDB("HSQLDB", PostgreSQLDialect.class),
    INFORMIX("Informix", InformixDialect.class),
    MARIADB("MariaDB", MySQLDialect.class),
    MYSQL("MySQL", MySQLDialect.class),
    ORACLE("Oracle", OracleDialect.class),
    PHOENIX("Phoenix", PostgreSQLDialect.class),
    POSTGRESQL("PostgreSQL", PostgreSQLDialect.class),
    SQLITE("SQLite", MySQLDialect.class),
    SQLSERVER("SQL Server", DerbyDialect.class),
    TIDB("TiDB", MySQLDialect.class),
    ;

    private final String name;
    private final Class<? extends AbstractDialect> dialectClass;

    SqlDialect(String name, Class<? extends AbstractDialect> dialectClass) {
        this.name = name;
        this.dialectClass = dialectClass;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Dialect> getDialectClass() {
        return dialectClass;
    }

    /**
     * 只要是 Dialect 的子类（mysql方言等）传进来，实例化出一个方言类并赋值
     *
     * @return
     */
    public Dialect newDialect() {
        try {
            // 获取到传入方言的构造器，并返回
            AbstractDialect dialect = dialectClass.getConstructor().newInstance();
            dialect.setSqlDialect(this);
            return dialect;
        } catch (Exception e) {
            throw new SqlDialectException(e);
        }
    }

    private static final Map<String, SqlDialect> map;

    /**
     * 将枚举中所有的方言初始化到map中，以name忽略大小写为key
     */
    static {
        Map<String, SqlDialect> store = new HashMap<>(32);
        for (SqlDialect e : values()) {
            String lowerCaseDialectName = e.getName().toLowerCase();
            String lowerCaseEnumName = e.name().toLowerCase();
            store.put(lowerCaseDialectName, e);
            if (!lowerCaseDialectName.equals(lowerCaseEnumName)) {
                store.putIfAbsent(e.name().toLowerCase(), e);
            }
        }
        map = Collections.unmodifiableMap(store);
    }

    /**
     * 根据传进来的值取出对应的 dialect
     *
     * @param name
     * @return
     */
    public static SqlDialect get(String name) {
        if (name == null) {
            return null;
        }
        return map.get(name);
    }
}
