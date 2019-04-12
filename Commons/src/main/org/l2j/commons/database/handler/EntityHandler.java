package org.l2j.commons.database.handler;

import org.l2j.commons.database.QueryDescriptor;
import org.l2j.commons.database.annotation.Column;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Objects.isNull;

public class EntityHandler implements TypeHandler<Object> {

    @Override
    public Object defaultValue() {
        return null;
    }

    @Override
    public Object handleResult(QueryDescriptor queryDescriptor) throws SQLException {
        var resultSet = queryDescriptor.getStatement().getResultSet();
        if(resultSet.next()) {
            return handleType(resultSet, queryDescriptor.getReturnType());
        }
        return defaultValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handleType(ResultSet resultSet, Class<?> type) throws SQLException {
        try {
            var instance = type.getDeclaredConstructor().newInstance();
            var fields = type.getDeclaredFields();

            var metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                var columnName = metaData.getColumnName(i);

                Field f = findField(fields, columnName);
                if(isNull(f)) {
                    throw  new SQLException("There is no field with name " +  columnName + " on Type " + type.getName());
                }
                if(f.trySetAccessible()) {
                    var handler = TypeHandler.MAP.getOrDefault(f.getType().getName(), TypeHandler.MAP.get(Object.class.getName()));
                    f.set(instance, handler.handleColumn(resultSet, i));
                } else {
                    throw  new SQLException("No accessible field " + f.getName() + " On type " + type );
                }
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public Object handleColumn(ResultSet resultSet, int column) {
        return null;
    }

    private Field findField(Field[] fields, String columnName) {
        for (Field field : fields) {
            if(field.isAnnotationPresent(Column.class)) {
                if(field.getAnnotation(Column.class).value().equalsIgnoreCase(columnName)) {
                    return field;
                }
            } else if(field.getName().equalsIgnoreCase(columnName)) {
                return field;
            }
        }
        return  null;
    }

    @Override
    public String type() {
        return Object.class.getName();
    }
}
