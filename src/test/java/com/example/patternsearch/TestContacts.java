package com.example.patternsearch;

import com.example.jdbc.Contact;
import com.example.jdbc.Contacts;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kateryna on 31.07.16.
 */
public class TestContacts extends Contacts {
    @Override
    public List<Contact> getContactsList(long limit, long offset) {
        InputStream inJson = Contact.class.getResourceAsStream("/contacts.json");
        ObjectMapper mapper = new ObjectMapper();
        List<Contact> contacts = null;
        try {
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Contact.class);
            contacts = mapper.readValue(inJson, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contacts;
    }
}
