package com.example.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class Contacts {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Getting data from the database with specified limit and offset
     * @param limit - rows quantity
     * @param offset - rows offset
     * @return list with selected rows from the database
     */
    @Transactional(readOnly=true)
    public List<Contact> getContactsList(long limit, long offset) {
        String queryStatement = "SELECT id, name FROM contacts LIMIT " + limit +" OFFSET " + offset + ";";
        return jdbcTemplate.query(queryStatement,
                (ResultSet rs, int rowNum) -> new Contact(rs.getLong("id"), rs.getString("name")));
    }


}