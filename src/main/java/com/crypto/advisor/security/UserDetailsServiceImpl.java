package com.crypto.advisor.security;

import com.crypto.advisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(
            repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    String.format(
                        "User with username '%s' not found", username
                    )
                ))
        );
    }
}