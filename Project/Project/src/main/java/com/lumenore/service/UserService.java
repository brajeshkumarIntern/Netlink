package com.lumenore.service;

import java.util.*;

import com.lumenore.globalexception.DatabaseEmptyException;
//import com.lumenore.model.Rating;
import com.lumenore.model1.User;
import com.lumenore.repository.UserRepository;
import com.lumenore2.model2.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Transactional
    public User addUser(User user) {
        if(repository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(("Email already exists: " + user.getEmail()));
        }
        repository.save(user);
        return user;
    }

    public List<User> getAllUser(){
        return repository.findAll();
    }

    public User getUserById(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new NullPointerException("User not found: " + id));
        Rating rating = restTemplate.getForObject("http://localhost:8081/rating/" + id, Rating.class);
        List<Rating> ratingList = List.of(rating);
        user.setRating(ratingList);
        return user;
    }



    public User updateUser(Long id, User user) {
        User u = repository.findById(id).orElseThrow(() -> new NullPointerException("User not found: " + id));
        u.setId(id);
        u.setName(user.getName());
        u.setEmail(user.getEmail());
        repository.save(user);
        return u;
    }

    public void deleteUser(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new NullPointerException("User not found: " + id));
        repository.delete(user);
    }

    public void deleteAll() {
        if (repository.count() == 0)
                throw new DatabaseEmptyException("No record in db to be deleted");
        repository.deleteAll();
    }
}
