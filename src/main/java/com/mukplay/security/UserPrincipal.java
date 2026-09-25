package com.mukplay.security;

import com.mukplay.domain.user.entity.User;
import com.mukplay.domain.user.entity.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String loginId;
    private final String password;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String loginId, String password, UserRole role) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.authorities = List.of(new SimpleGrantedAuthority(role.name()));
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getLoginId(),
                user.getPassword(),
                user.getRole()
        );
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
