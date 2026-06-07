package com.fudan.lab5.user;

import java.time.LocalDate;

public record UserProfile(long id, String username, String displayName, String gender, LocalDate birthday, Integer age) {
}
