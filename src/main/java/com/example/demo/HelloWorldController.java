package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Person;
import com.example.demo.repository.PersonRepository;

@RestController
public class HelloWorldController {

    @Autowired
    private PersonRepository personRepository;

    @RequestMapping("/hello")
    public String sayHello(@RequestParam String firstName, @RequestParam String lastName) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        personRepository.save(person);
        return "Hello, " + firstName + " " + lastName + "!";
    }
}
