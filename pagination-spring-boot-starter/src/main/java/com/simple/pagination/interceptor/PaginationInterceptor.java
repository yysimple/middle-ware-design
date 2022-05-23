package com.simple.pagination.interceptor;

import com.simple.pagination.annotation.*;
import com.simple.pagination.dialect.Dialect;
import com.simple.pagination.exception.PaginationException;
import com.simple.pagination.util.Page;
import com.simple.pagination.util.*;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/22 13:41
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})})
public class PaginationInterceptor implements Interceptor {

    private final Dialect dialect;
    private final PaginationSettings settings;
    private final ConcurrentMap<String, MappedStatement> countMsMap = new ConcurrentHashMap<>(64);
    private final ConcurrentMap<String, PaginationUnrefinedParam> unrefinedParamMap = new ConcurrentHashMap<>(64);

    public PaginationInterceptor(PaginationSettings settings) {
        settings.initialize();
        this.settings = settings;
        this.dialect = settings.getDialect();
    }

    /**
     * 实现mybatis提供的拦截器（其实就是插件的切入点，追到源码就可以看到 -- pluginAll）
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // @Intercepts 这里的参数顺序取值
        Object[] args = invocation.getArgs();
        // 可以拿到该方法之后的返回值类型，操作类型时什么（query-这是mybatis定义的查询key，然后返回的是List）/ returnType = interface java.util.List
        Method method = invocation.getMethod();
        // ms则是一些元数据信息，包括是哪个接口，在那个文件下，方法名，全限定名，包括xml里面的sql语句等，传递的参数这里面也会有
        MappedStatement ms = (MappedStatement) args[0];
        // 这里面就是传进来的参数
        Object parameter = args[1];
        Class<?> returnType = method.getReturnType();
        // 校验参数
        if (!isPageable(parameter, returnType)) {
            return invocation.proceed();
        }
        PaginationParam param = analyseParameter(parameter, ms.getId());
        // 校验是否传了分页参数，没有则不进拦截器
        if (!param.isPageable()) {
            return invocation.proceed();
        }
        Executor executor = (Executor) invocation.getTarget();
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        BoundSql boundSql;
        CacheKey cacheKey;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameter); // 这里可以拿到 sql 、参数 、等信息
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            boundSql = (BoundSql) args[5];
            cacheKey = (CacheKey) args[4];
        }
        Long total = null;
        if (isCounting(parameter)) {
            total = count(executor, ms, boundSql, parameter, resultHandler, param.getCountExpr(), param.getCountMethodName());
            if (total == 0 && settings.isSkipQueryIfCountEqualsZero()) {
                return PaginationCollectionFactory.build(returnType, total, param.getPage(), param.getSize());
            }
        }
        List<?> result = query(executor, ms, boundSql, parameter, resultHandler, cacheKey, param);
        return PaginationCollectionFactory.build(returnType, result, total, param.getPage(), param.getSize());
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * Returns count quantity.
     */
    private Long count(Executor executor, MappedStatement ms, BoundSql boundSql, Object parameter, ResultHandler resultHandler, String countExpr, String countMsId) throws SQLException {
        Object value;
        if (StringUtils.isEmpty(countMsId)) {
            value = countByGenerated(executor, ms, boundSql, parameter, resultHandler, countExpr);
        } else {
            value = countBySpecified(executor, ms, boundSql, parameter, resultHandler, countExpr, countMsId);
        }
        return value != null ? parseLong(((List) value).get(0)) : 0L;
    }

    /**
     * Returns count quantity by the generated SQL.
     */
    private Object countByGenerated(Executor executor, MappedStatement ms, BoundSql boundSql, Object parameter, ResultHandler resultHandler, String countExpr) throws SQLException {
        MappedStatement countMs = countMsMap.computeIfAbsent(buildCountMsKey(ms), id -> newCountMappedStatement(ms, id));
        CacheKey countKey = executor.createCacheKey(countMs, parameter, RowBounds.DEFAULT, boundSql);
        String originSql = boundSql.getSql();
        String countSql = dialect.getCountSql(originSql, countExpr);
        BoundSql countBoundSql = newBoundSql(ms, boundSql, countSql);
        return executor.query(countMs, parameter, RowBounds.DEFAULT, resultHandler, countKey, countBoundSql);
    }

    /**
     * Returns count quantity by the specified SQL.
     */
    private Object countBySpecified(Executor executor, MappedStatement ms, BoundSql boundSql, Object parameter, ResultHandler resultHandler, String countExpr, String countMsId) throws SQLException {
        MappedStatement countMs = null;
        Throwable cause = null;
        try {
            countMs = ms.getConfiguration().getMappedStatement(countMsId, false);
        } catch (Throwable t) {
            cause = t;
        }
        if (countMs != null) {
            CacheKey countKey = executor.createCacheKey(countMs, parameter, RowBounds.DEFAULT, ms.getBoundSql(parameter));
            BoundSql countBoundSql = countMs.getBoundSql(parameter);
            return executor.query(countMs, parameter, RowBounds.DEFAULT, resultHandler, countKey, countBoundSql);
        }
        if (settings.isUseGeneratedIfCountMethodIsMissing()) {
            return countByGenerated(executor, ms, boundSql, parameter, resultHandler, countExpr);
        }
        throw new PaginationException("Counting method named \"" + countMsId + "\" is missing.", cause);
    }

    /**
     * Returns paging query results.
     */
    private <E> List<E> query(Executor executor, MappedStatement ms, BoundSql boundSql, Object parameter, ResultHandler resultHandler, CacheKey cacheKey, PaginationParam param) throws Exception {
        Object assembledParameter = dialect.assembleParameter(ms, parameter, boundSql, cacheKey, param);
        String originSql = boundSql.getSql();
        String paginationSql = dialect.getPagingSql(originSql, param);
        BoundSql paginationBoundSql = newBoundSql(ms, boundSql, paginationSql);
        return executor.query(ms, assembledParameter, RowBounds.DEFAULT, resultHandler, cacheKey, paginationBoundSql);
    }

    /**
     * Returns a new instanced object of {@link MappedStatement}.
     *
     * @param ms   The origin {@link MappedStatement}
     * @param msId New id
     * @return a new instanced object of {@link MappedStatement}
     */
    private MappedStatement newCountMappedStatement(MappedStatement ms, String msId) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), msId, ms.getSqlSource(), ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class, Collections.emptyList()).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        return builder.build();
    }

    /**
     * Returns a new instanced object of {@link BoundSql}.
     *
     * @param ms       The origin {@link MappedStatement}
     * @param boundSql The origin {@link BoundSql}
     * @param sql      SQL string
     * @return a new instanced object of {@link BoundSql}
     */
    private BoundSql newBoundSql(MappedStatement ms, BoundSql boundSql, String sql) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    /**
     * 校验参数是否合法（是否存在page、size等字段，或者别名字段）
     *  - 配置文件配置 page-field: xxx
     *  - 继承 Pageable
     *  - 使用 `@Pagination`、`@Page`、`@Size`、`@Offset`、`@Rows` 注解.
     * @param parameter  The parameter
     * @param returnType Return type
     */
    private boolean isPageable(Object parameter, Class<?> returnType) {
        if (parameter == null
                || !Collection.class.isAssignableFrom(returnType)
                || parameter.getClass().getAnnotation(DisablePagination.class) != null) {
            return false;
        }
        return settings.isObtainValuesFromFields()
                || parameter instanceof Pageable
                || parameter.getClass().getAnnotation(Pagination.class) != null;
    }

    /**
     * Returns <code>true</code> when {@link DisableCount} is absent
     * and enable count equals <code>true</code>.
     *
     * @param parameter The parameter
     */
    private boolean isCounting(Object parameter) {
        if (parameter.getClass().getAnnotation(DisableCount.class) != null) {
            return false;
        }
        if (StringUtils.isEmpty(settings.getEnableCountFieldName())) {
            return true;
        }
        try {
            Object value = BeanUtils.read(parameter, settings.getEnableCountFieldName());
            return parseBoolean(value, false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns {@link PaginationParam} from parameter.
     * 解析参数
     *
     * @param parameter 对应实体对象传入的值
     * @param msId 对应的是mapper接口层的 权限定名 + 方法名 com.simple.page.mapper.StudentMapper.listStudent
     * @return {@link PaginationParam}
     */
    private PaginationParam analyseParameter(Object parameter, String msId) {
        String key = buildUnrefinedParamKey(parameter, msId); // com.simple.page.mapper.StudentMapper.listStudent#com.simple.page.ibo.StudentIbo
        PaginationUnrefinedParam unrefinedParam = unrefinedParamMap.get(key);
        if (unrefinedParam == null) {
            unrefinedParam = getPaginationUnrefinedParam(parameter);
            if (unrefinedParam.isInvalid()) {
                return PaginationParam.nonPagination();
            }
            unrefinedParamMap.putIfAbsent(key, unrefinedParam);
        }
        int defaultPage = unrefinedParam.getDefaultPage();
        int defaultSize = unrefinedParam.getDefaultSize();
        int maxPage = unrefinedParam.getMaxPage();
        int maxSize = unrefinedParam.getMaxSize();
        String countExpr;
        String countMethodName;
        Integer page;
        Integer size;
        Integer offset;
        Integer rows;
        boolean usePageSize;
        boolean useOffsetRows;
        if (parameter instanceof Pageable) {
            Pageable pageable = (Pageable) parameter;
            page = pageable.getPage();
            size = pageable.getSize();
            offset = pageable.getOffset();
            rows = pageable.getRows();
            countExpr = pageable.getCountExpr();
            countMethodName = pageable.getCountMethodName();
        } else if (unrefinedParam.isMapType()) {
            Map parameterMap = (Map) parameter;
            page = parseInteger(getMapValue(parameterMap, settings.getPageFieldName()));
            size = parseInteger(getMapValue(parameterMap, settings.getSizeFieldName()));
            offset = parseInteger(getMapValue(parameterMap, settings.getOffsetFieldName()));
            rows = parseInteger(getMapValue(parameterMap, settings.getRowsFieldName()));
            countExpr = parseString(getMapValue(parameterMap, settings.getCountExprFieldName()));
            countMethodName = parseString(getMapValue(parameterMap, settings.getCountMethodNameFieldName()));
        } else {
            page = invokeInteger(parameter, unrefinedParam.getPageReadMethod());
            size = invokeInteger(parameter, unrefinedParam.getSizeReadMethod());
            offset = invokeInteger(parameter, unrefinedParam.getOffsetReadMethod());
            rows = invokeInteger(parameter, unrefinedParam.getRowsReadMethod());
            countExpr = invokeString(parameter, unrefinedParam.getCountExprReadMethod());
            countMethodName = invokeString(parameter, unrefinedParam.getCountMethodNameReadMethod());
        }
        if (StringUtils.isEmpty(countExpr)
                && StringUtils.isNotEmpty(unrefinedParam.getCountExpr())) {
            countExpr = unrefinedParam.getCountExpr();
        }
        usePageSize = page != null && size != null;
        useOffsetRows = offset != null && rows != null;
        if (usePageSize) {
            if (page <= 0 && defaultPage > 0) {
                page = defaultPage;
            }
            if (size <= 0 && defaultSize > 0) {
                size = defaultSize;
            }
            if (page > maxPage && maxPage > 0) {
                page = maxPage;
            }
            if (size > maxSize && maxSize > 0) {
                size = maxSize;
            }
            offset = (page - 1) * size;
            rows = size;
        } else if (useOffsetRows) {
            if (rows <= 0 && defaultSize > 0) {
                rows = defaultSize;
            }
            if (offset <= 0 && defaultPage > 0) {
                offset = (defaultPage - 1) * rows;
            }
            if (rows > maxSize && maxSize > 0) {
                rows = maxSize;
            }
            if (offset / rows + 1 > maxPage && maxPage > 0) {
                offset = (maxPage - 1) * rows;
            }
            // Converts [offset, rows] to [page, size] if rows can be divided by offset.
            if (rows > 0 && offset % rows == 0) {
                page = offset / rows + 1;
                size = rows;
            } else {
                page = null;
                size = null;
            }
        } else {
            return PaginationParam.nonPagination();
        }
        return new PaginationParam(page, size, offset, rows, countExpr, countMethodName, true);
    }

    /**
     * Returns {@link PaginationUnrefinedParam} from parameter.
     *
     * @param parameter The parameter
     * @return {@link PaginationUnrefinedParam}
     */
    private PaginationUnrefinedParam getPaginationUnrefinedParam(Object parameter) {
        // There may be an object that extends Pageable.
        if (parameter instanceof Pageable) {
            return buildPaginationUnrefinedParam(parameter, false);
        }
        // There may be a map.
        if (parameter instanceof Map) {
            return buildPaginationUnrefinedParam(parameter, true);
        }
        Method pageReadMethod = null;
        Method sizeReadMethod = null;
        Method offsetReadMethod = null;
        Method rowsReadMethod = null;
        Method countExprReadMethod = null;
        Method countMethodNameReadMethod = null;
        // Obtains read-method from configuration.
        if (settings.isObtainValuesFromFields()) {
            PropertyDescriptor[] pds;
            try {
                pds = BeanUtils.getPropertyDescriptors(parameter.getClass());
            } catch (IntrospectionException ignored) {
                pds = null;
            }
            if (pds != null && pds.length > 0) {
                for (PropertyDescriptor pd : pds) {
                    if (Objects.equals(settings.getPageFieldName(), pd.getName())) {
                        pageReadMethod = pd.getReadMethod();
                    } else if (Objects.equals(settings.getSizeFieldName(), pd.getName())) {
                        sizeReadMethod = pd.getReadMethod();
                    } else if (Objects.equals(settings.getOffsetFieldName(), pd.getName())) {
                        offsetReadMethod = pd.getReadMethod();
                    } else if (Objects.equals(settings.getRowsFieldName(), pd.getName())) {
                        rowsReadMethod = pd.getReadMethod();
                    } else if (Objects.equals(settings.getCountExprFieldName(), pd.getName())) {
                        countExprReadMethod = pd.getReadMethod();
                    } else if (Objects.equals(settings.getCountMethodNameFieldName(), pd.getName())) {
                        countMethodNameReadMethod = pd.getReadMethod();
                    }
                }
            }
        }
        // Obtains read-method by field annotations.
        Field[] fields = ReflectUtils.getDeepFields(parameter, true);
        for (Field field : fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            if (annotations.length == 0) {
                continue;
            }
            for (Annotation annotation : annotations) {
                try {
                    if (annotation instanceof Page) {
                        pageReadMethod = BeanUtils.getReadMethod(field);
                    } else if (annotation instanceof Size) {
                        sizeReadMethod = BeanUtils.getReadMethod(field);
                    } else if (annotation instanceof Offset) {
                        offsetReadMethod = BeanUtils.getReadMethod(field);
                    } else if (annotation instanceof Rows) {
                        rowsReadMethod = BeanUtils.getReadMethod(field);
                    } else if (annotation instanceof CountExpr) {
                        countExprReadMethod = BeanUtils.getReadMethod(field);
                    } else if (annotation instanceof CountMethodName) {
                        countMethodNameReadMethod = BeanUtils.getReadMethod(field);
                    }
                } catch (IntrospectionException ignored) {
                    // ignored
                }
            }
        }
        // Obtains read-method by method annotations.
        Method[] methods = ReflectUtils.getDeepMethods(parameter, true);
        for (Method method : methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            if (annotations.length == 0) {
                continue;
            }
            for (Annotation annotation : annotations) {
                if (annotation instanceof Page) {
                    pageReadMethod = method;
                } else if (annotation instanceof Size) {
                    sizeReadMethod = method;
                } else if (annotation instanceof Offset) {
                    offsetReadMethod = method;
                } else if (annotation instanceof Rows) {
                    rowsReadMethod = method;
                } else if (annotation instanceof CountExpr) {
                    countExprReadMethod = method;
                } else if (annotation instanceof CountMethodName) {
                    countMethodNameReadMethod = method;
                }
            }
        }
        if (pageReadMethod == null
                && sizeReadMethod == null
                && offsetReadMethod == null
                && rowsReadMethod == null) {
            return PaginationUnrefinedParam.invalidation();
        }
        PaginationUnrefinedParam unrefinedParam = buildPaginationUnrefinedParam(parameter, false);
        unrefinedParam.setMethods(
                pageReadMethod,
                sizeReadMethod,
                offsetReadMethod,
                rowsReadMethod,
                countExprReadMethod,
                countMethodNameReadMethod);
        return unrefinedParam;
    }

    /**
     * Builds {@link PaginationUnrefinedParam} from parameter.
     *
     * @param parameter The parameter
     * @param mapType   Whether it is a map type
     * @return {@link PaginationUnrefinedParam}
     */
    private PaginationUnrefinedParam buildPaginationUnrefinedParam(Object parameter, boolean mapType) {
        Pagination pagination = parameter.getClass().getAnnotation(Pagination.class);
        if (pagination != null) {
            return new PaginationUnrefinedParam(
                    pagination.defaultPage() > 0 ? pagination.defaultPage() : settings.getDefaultPage(),
                    pagination.defaultSize() > 0 ? pagination.defaultSize() : settings.getDefaultSize(),
                    pagination.maxPage() > 0 ? pagination.maxPage() : settings.getMaxPage(),
                    pagination.maxSize() > 0 ? pagination.maxSize() : settings.getMaxSize(),
                    StringUtils.isNotEmpty(pagination.countExpr()) ? pagination.countExpr() : settings.getCountExpr(),
                    mapType,
                    true);
        } else {
            return new PaginationUnrefinedParam(
                    settings.getDefaultPage(),
                    settings.getDefaultSize(),
                    settings.getMaxPage(),
                    settings.getMaxSize(),
                    settings.getCountExpr(),
                    mapType,
                    true);
        }
    }

    /**
     * Returns {@link Integer} value by invoking method.
     * And try to convert to {@link Integer} value
     * if it is a {@link String} value.
     *
     * @param o      target
     * @param method {@link Method}
     * @return {@link Integer} value
     */
    private Integer invokeInteger(Object o, Method method) {
        Object value = invoke(o, method);
        return parseInteger(value);
    }

    /**
     * Returns {@link String} value by invoking method.
     *
     * @param o      target
     * @param method {@link Method}
     * @return {@link String} value
     */
    private String invokeString(Object o, Method method) {
        Object value = invoke(o, method);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Invokes method.
     *
     * @param o      target
     * @param method {@link Method}
     * @return {@link Boolean} value
     */
    private Object invoke(Object o, Method method) {
        if (o == null || method == null) {
            return null;
        }
        try {
            return BeanUtils.read(o, method);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param map It may be an object of {@link org.apache.ibatis.binding.MapperMethod.ParamMap}
     * @param key The key whose associated value is to be returned
     * @return The value to which the specified key is mapped
     */
    private Object getMapValue(Map map, Object key) {
        if (map == null || key == null) {
            return null;
        }
        try {
            // The map may be an object of {@link MapperMethod.ParamMap}
            return map.get(key);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Parses the object argument as a string.
     */
    private String parseString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    /**
     * Parses the object argument as a boolean.
     */
    private boolean parseBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        } else if (value instanceof Number) {
            // Returns false if the value equals 0.
            return ((Number) value).intValue() != 0;
        } else if (value instanceof String) {
            String s = (String) value;
            // Returns true if the text equals "true".
            if ("true".equalsIgnoreCase(s)) {
                return true;
            }
            // Returns false if the text equals "false".
            if ("false".equalsIgnoreCase(s)) {
                return false;
            }
        }
        return defaultValue;
    }

    /**
     * Parses the object argument as a signed decimal integer.
     */
    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return null;
    }

    /**
     * Parses the object argument as a signed decimal long.
     */
    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return null;
    }

    /**
     * Returns count MappedStatement key.
     */
    private String buildCountMsKey(MappedStatement ms) {
        return ms.getId() + "__COUNT";
    }

    /**
     * Returns unrefined param key.
     */
    private String buildUnrefinedParamKey(Object parameter, String msId) {
        return msId + "#" + parameter.getClass().getName();
    }

    public Dialect getDialect() {
        return dialect;
    }

    public PaginationSettings getSettings() {
        return settings;
    }
}
