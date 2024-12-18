package com.ul.vrs.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ul.vrs.entity.account.Account;
import com.ul.vrs.entity.account.Customer;
import com.ul.vrs.entity.account.Manager;
import com.ul.vrs.repository.AccountRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AccountManagerService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Account signUp(String username, String password, String accountType) {
        Optional<Account> account = accountRepository.findById(username);
        String hashedPassword = passwordEncoder.encode(password);

        if(account.isPresent()) {
            System.out.println("Username already exists: " + username);
            return null;
        }

        Account newAccount = null;

        switch (accountType.toLowerCase()) {
            case "customer" -> newAccount = new Customer(username, hashedPassword);
            case "manager" -> newAccount = new Manager(username,  hashedPassword);
            default -> System.err.println("Unkown account type");
        }

        if (newAccount != null) {
            accountRepository.save(newAccount);
            System.out.println("Account created for username: " + username + " as " + accountType);
        }

        return newAccount;
    }

    public Account logIn(String username, String password) {
        Optional<Account> account = accountRepository.findById(username);

        if (account.isPresent() && passwordEncoder.matches(password, account.get().getPassword())) {
            System.out.println("Login successful for user: " + username);
            return account.get();
        }

        System.out.println("Login failed for user: " + username);
        return null;
    }

    public Account getAccount(String username) {
        return accountRepository.findById(username).get();
    }
}