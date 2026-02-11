package org.codeup.statiocore.security;

import org.codeup.statiocore.domain.User;

public interface TokenProvider {
    String generateToken(User user);
}
