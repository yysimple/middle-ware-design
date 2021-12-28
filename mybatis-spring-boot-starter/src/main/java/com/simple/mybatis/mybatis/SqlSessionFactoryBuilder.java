package com.simple.mybatis.mybatis;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述: 用于构建出一个工厂，通过加载资源文件，然后将其信息解析成工厂构造器需要的参数
 * 最后有工厂生成一个sql会话
 *
 * @author: WuChengXing
 * @create: 2021-12-28 13:52
 **/
public class SqlSessionFactoryBuilder {

    /**
     * 这里的配置和连接操作，都不放在这里做了，引入starter后可以放在自动装配里面去完成
     *
     * @param connection
     * @param properties
     * @return
     * @throws Exception
     */
    public DefaultSqlSessionFactory build(Connection connection, Properties properties) throws Exception {
        Configuration configuration = setConfiguration(connection, properties);
        // 读取配置
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources(properties.get("mapperLocations").toString());
        List<Element> list = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            Document document = new SAXReader().read(new InputSource(new InputStreamReader(resource.getInputStream())));
            list.add(document.getRootElement());
        }
        configuration.setMapperElement(mapperElement(list));
        return new DefaultSqlSessionFactory(configuration);
    }

    private Configuration setConfiguration(Connection connection, Properties properties) {
        Configuration configuration = new Configuration();
        configuration.setConnection(connection);
        configuration.setBaseMapperPackage(properties.get("baseMapperPackage").toString());
        // == mapUnderscoreToCamelCase
        configuration.setIsAllowedMapper(dealIsAllowedMapper((String) properties.get("isAllowedMapper")));
        configuration.setTypeAliasesPackage(properties.get("typeAliasesPackage").toString());
        return configuration;
    }

    private Boolean dealIsAllowedMapper(String value) {
        return !StringUtils.isEmpty(value) && "true".equalsIgnoreCase(value);
    }

    /**
     * 获取SQL语句信息
     *
     * @param list
     * @return
     */
    private Map<String, XNode> mapperElement(List<Element> list) {
        Map<String, XNode> map = new HashMap<>();
        for (Element root : list) {
            //命名空间
            String namespace = root.attributeValue("namespace");
            // SELECT
            List<Element> selectNodes = root.selectNodes("select");
            for (Element node : selectNodes) {
                String id = node.attributeValue("id");
                String parameterType = node.attributeValue("parameterType");
                String resultType = node.attributeValue("resultType");
                String sql = node.getText();

                // ? 匹配
                Map<Integer, String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile("(#\\{(.*?)})");
                Matcher matcher = pattern.matcher(sql);
                for (int i = 1; matcher.find(); i++) {
                    String g1 = matcher.group(1);
                    String g2 = matcher.group(2);
                    parameter.put(i, g2);
                    sql = sql.replace(g1, "?");
                }

                XNode xNode = new XNode();
                xNode.setNamespace(namespace);
                xNode.setId(id);
                xNode.setParameterType(parameterType);
                xNode.setResultType(resultType);
                xNode.setSql(sql);
                xNode.setParameter(parameter);

                map.put(namespace + "." + id, xNode);
            }
        }
        return map;
    }
}
