package com.crypto.advisor.entity;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import com.crypto.advisor.model.Role;
import org.hibernate.validator.constraints.Length;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Length(min = 5, max = 15, message = "Username must be between 5 and 15 characters")
    private String username;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    @Length(min = 5, message = "Password must be greater than 5 characters")
    private String password;

    @Transient
    @JsonIgnore
    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirmation;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = Boolean.TRUE;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}