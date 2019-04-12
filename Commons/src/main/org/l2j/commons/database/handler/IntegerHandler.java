package org.l2j.commons.database.handler;

import org.l2j.commons.database.QueryDescriptor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerHandler implements TypeHandler<Integer> {

    @Override
    public Integer defaultValue() {
        return 0;
    }

    @Override
    public Integer handleResult(QueryDescriptor queryDescriptor) throws SQLException {
        if(queryDescriptor.isUpdate()) {
            return queryDescriptor.getStatement().getUpdateCount();
        }
        var resultSet = queryDescriptor.getStatement().getResultSet();
        if(resultSet.next()) {
            return handleColumn(resultSet, 1);
        }
        return defaultValue();
    }

    @Override
    public Integer handleType(ResultSet resultSet, Class<?> type) throws SQLException {
        return handleColumn(resultSet, 1);
    }

    @Override
    public Integer handleColumn(ResultSet resultSet, int column) throws SQLException {
        return resultSet.getInt(column);
    }

    @Override
    public String type() {
        return Integer.TYPE.getName() ;
    }
}
