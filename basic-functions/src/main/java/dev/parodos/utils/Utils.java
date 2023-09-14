package dev.parodos.utils;


import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Utils {

    public void isValidUser(int userID) {
        System.out.println("Checking user ID: " + userID);
        if (userID == 0) {
            System.out.println("Failed with the userID" + userID);
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
}
