package com.example.controllers;

import com.example.jdbc.Contact;
import com.example.jdbc.Contacts;
import com.example.patternsearch.SearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.PatternSyntaxException;

@Controller
public class ContactController {
    @Autowired
    private Contacts contacts;

    @RequestMapping("/hello/contacts")
    @Async
    public
    @ResponseBody
    CompletableFuture<List<Contact>> getContacts(@RequestParam(value = "nameFilter", required = true) String nameFilter) {
        return SearchEngine.getInstance(contacts).search(nameFilter);
    }

    @ExceptionHandler
    private void handlePatternSyntaxException(PatternSyntaxException exception,
                                              HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }


}
