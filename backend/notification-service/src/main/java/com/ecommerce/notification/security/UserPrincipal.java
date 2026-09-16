package com.ecommerce.notification.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Principal {
    private final String name;
    private final List<String> roles;

    @Override
    public String getName() {
        return name;
    }
}
