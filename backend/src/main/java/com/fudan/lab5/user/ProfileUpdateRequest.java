package com.fudan.lab5.user;

import java.time.LocalDate;

public record ProfileUpdateRequest(String displayName, String gender, LocalDate birthday, Integer age) {
}
