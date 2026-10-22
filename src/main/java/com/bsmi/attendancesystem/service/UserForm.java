package com.bsmi.attendancesystem.service;

/**
 * What was typed into the Manage Users form.
 *
 * @param userId the user chosen in the table, or null when adding a new one
 */
public record UserForm(
        Integer userId, String username, String password, String confirmPassword, String role) {}
