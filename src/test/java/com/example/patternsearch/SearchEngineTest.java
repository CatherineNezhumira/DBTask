package com.example.patternsearch;

import com.example.jdbc.Contact;
import com.example.jdbc.Contacts;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

/**
 * Created by kateryna on 31.07.16.
 */
public class SearchEngineTest {

    @Test
    public void testSearch() throws Exception {
        List<Contact> contacts = getListFromJsonFile("/filtered_contacts.json");
        SearchEngine.getInstance(new TestContacts()).search("^B.*$").thenAccept(result -> {
            Assert.assertArrayEquals(contacts.toArray(), result.toArray());
        });
    }

    @Test(expected=PatternSyntaxException.class)
    public void testInValidPattern() throws PatternSyntaxException {
        SearchEngine.getInstance(new Contacts()).search("`>...*{}{}{}{}{{{{");
    }

    @Test
    public void testSearchInEmptyList() throws Exception {
        List<Contact> contacts = getListFromJsonFile("/empty_contacts.json");
        SearchEngine.getInstance(new TestContacts()).search("^A.*$").thenAccept(result -> {
            Assert.assertArrayEquals(contacts.toArray(), result.toArray());
        });
    }

    private List<Contact> getListFromJsonFile(String fileName) {
        InputStream inJson = Contact.class.getResourceAsStream(fileName);
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Contact.class);
        List<Contact> contacts = null;
        try {
            contacts = mapper.readValue(inJson, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contacts;
    }
}