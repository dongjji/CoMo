package com.dongjji.como.user.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserDto {
    private String username;
    private String password;
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;
}
