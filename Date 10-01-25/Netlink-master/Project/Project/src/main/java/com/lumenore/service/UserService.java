package com.lumenore.service;

import java.util.*;

import com.lumenore.globalexception.DatabaseEmptyException;
import com.lumenore.model1.User;
import com.lumenore.repository.UserRepository;
import org.springframework.http.*;
import com.lumenore2.model2.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    //POST METHOD
    public User addUser(User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        repository.save(user);
        List<Rating> ratingList = user.getRatings();
        if (!ratingList.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            String token = "your_access_token";
            headers.set("Authorization", "Bearer " + token); // Example header
            headers.setContentType(MediaType.APPLICATION_JSON);

            List<Rating> userRating = user.getRatings();
            Rating rating = userRating.get(0);
            System.out.println(rating);

            HttpEntity<Rating> entity = new HttpEntity<>(rating, headers);
            ResponseEntity<Rating> response = restTemplate.exchange(
                    "http://localhost:8085/rating",
                    HttpMethod.POST,
                    entity,
                    Rating.class
            );
        }

        return user;
    }

//    GET METHOD FOR ALL USER
    public List<User> getAllUser() {
        List<User> userList = repository.findAll();

       for (User user: userList) {
           HttpHeaders headers = new HttpHeaders();
           String token = "your_access_token";
           headers.set("Authorization", "Bearer "+ token); // Example header
           headers.setContentType(MediaType.APPLICATION_JSON);

           // Create an HttpEntity with headers
           HttpEntity<String> entity = new HttpEntity<>(headers);

           // Call the external service using GET method
           ResponseEntity<?> response = restTemplate.exchange(
                   "http://localhost:8085/rating/email/" + user.getEmail(),
                   HttpMethod.GET,
                   entity,
                   ArrayList.class
           );

           // Extract the Rating object from the response
           List<Rating> rating = (List<Rating>) response.getBody();
           if (rating != null) {
               user.setRatings(rating);
           }
       }

        return userList;
    }

    // GET METHID FOR SINGLE USER
    public User getUserById(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new NullPointerException("User not found: " + id));

        // Prepare headers for external service call
        HttpHeaders headers = new HttpHeaders();
        String token = "your_access_token";
        headers.set("Authorization", "Bearer "+ token); // Example header
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create an HttpEntity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Call the external service using GET method
        ResponseEntity<?> response = restTemplate.exchange(
                "http://localhost:8085/rating/email/" + user.getEmail(),
                HttpMethod.GET,
                entity,
                ArrayList.class
        );

        // Extract the Rating object from the response
        List<Rating> rating = (List<Rating>) response.getBody();
        if (rating != null) {
            user.setRatings(rating);
        }
        return user;
    }

//  PUT METHOD
    public User updateUser(Long id, User user) {
        User existingUser = repository.findById(id).orElseThrow(() -> new NullPointerException("User not found: " + id));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setId(id);

        List<Rating> ratings  = user.getRatings();
        Rating userRating = ratings.get(0);
        if (userRating != null) {
            // Prepare headers for external service call
            HttpHeaders headers = new HttpHeaders();
            String token = "your_access_token";
            headers.set("Authorization", "Bearer " + token); // Example header
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create an HttpEntity with headers
            HttpEntity<Rating> entity = new HttpEntity<>(userRating, headers);

            // Call the external service using PUT method
            Rating updatedUserRating = restTemplate.exchange(
                    "http://localhost:8085/rating/" + id,
                    HttpMethod.PUT,
                    entity,
                    Rating.class).getBody();
            user.setRatings((List<Rating>) updatedUserRating);
        }
        repository.save(existingUser);
        return user;
    }

//    DELETE METHOD FOR SINGLE USER
    public void deleteUser(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new NullPointerException("User not found: " + id));

        // Prepare headers for external service call
        HttpHeaders headers = new HttpHeaders();
        String token = "your_access_token";
        headers.set("Authorization", "Bearer " + token); // Example header
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create an HttpEntity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Call the external service using DELETE method
        String response  = String.valueOf(restTemplate.exchange(
                "http://localhost:8085/rating/" + id,
                HttpMethod.DELETE,
                entity,
                String.class
        ));
        System.out.println(response);
        repository.delete(user);
    }

//    DELETE ALL METHOD
    public void deleteAll() {
        if (repository.count() == 0) {
            throw new DatabaseEmptyException("No record in db to be deleted");
        }
        repository.deleteAll();

        // Call external API to notify about deletion
        HttpHeaders headers = new HttpHeaders();
        String token = "your_access_token";
        headers.set("Authorization", "Bearer " + token); // Example header
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create an HttpEntity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Call the external service using DELETE method
        restTemplate.exchange(
                "http://localhost:8085/rating",
                HttpMethod.DELETE,
                entity,
                Void.class
        );
    }

    public String externalApi() {
        String Url = "";
        return "";
    }
}
