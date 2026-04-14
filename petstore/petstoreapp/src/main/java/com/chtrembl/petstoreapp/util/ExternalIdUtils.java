package com.chtrembl.petstoreapp.util;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Utility class for handling Microsoft Entra External ID specific operations
 */
@Component
public class ExternalIdUtils {

    /**
     * Extract display name from External ID user claims
     * Tries multiple claim names in order of preference
     */
    public static String getDisplayName(OidcUser user) {
        if (user == null) return "User";
        return getDisplayName(user.getClaims());
    }

    /**
     * Extract display name from claims map
     */
    public static String getDisplayName(Map<String, Object> claims) {
        if (claims == null) return "User";

        // Try different claim names for display name in order of preference
        String[] displayNameClaims = {"name", "display_name", "displayName"};
        for (String claimName : displayNameClaims) {
            String value = getStringClaim(claims, claimName);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        // Construct from given_name and family_name
        String givenName = getStringClaim(claims, "given_name");
        String familyName = getStringClaim(claims, "family_name");
        if (givenName != null && familyName != null) {
            return (givenName + " " + familyName).trim();
        }
        if (givenName != null) {
            return givenName.trim();
        }
        if (familyName != null) {
            return familyName.trim();
        }

        // Fallback to preferred_username or email
        String[] fallbackClaims = {"preferred_username", "email", "upn"};
        for (String claimName : fallbackClaims) {
            String value = getStringClaim(claims, claimName);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return "User";
    }

    /**
     * Extract email from External ID user claims
     * Enhanced to support External ID specific claims like identities
     */
    public static String getEmail(OidcUser user) {
        if (user == null) return null;
        return getEmail(user.getClaims());
    }

    /**
     * Extract email from claims map
     * Enhanced for Microsoft Entra External ID
     */
    public static String getEmail(Map<String, Object> claims) {
        if (claims == null) return null;

        // Try standard email claims first
        String[] emailClaims = {"email", "emails", "mail", "preferred_username", "upn"};
        for (String claimName : emailClaims) {
            String value = getStringClaim(claims, claimName);
            if (isValidEmail(value)) {
                return value.trim().toLowerCase();
            }
        }

        // Try External ID specific claims
        String emailFromVerified = getStringClaim(claims, "verified_primary_email");
        if (isValidEmail(emailFromVerified)) {
            return emailFromVerified.trim().toLowerCase();
        }

        // Try to extract from identities claim (External ID specific)
        String emailFromIdentities = extractEmailFromIdentities(claims.get("identities"));
        if (isValidEmail(emailFromIdentities)) {
            return emailFromIdentities.trim().toLowerCase();
        }

        return null;
    }

    /**
     * Get user ID from claims
     */
    public static String getUserId(OidcUser user) {
        if (user == null) return null;
        return getStringClaim(user.getClaims(), "sub");
    }

    /**
     * Get given name from claims
     */
    public static String getGivenName(Map<String, Object> claims) {
        return getStringClaim(claims, "given_name");
    }

    /**
     * Get family name from claims
     */
    public static String getFamilyName(Map<String, Object> claims) {
        return getStringClaim(claims, "family_name");
    }

    /**
     * Check if user is from External ID
     */
    public static boolean isExternalIdUser(OidcUser user) {
        if (user == null) return false;
        String issuer = getStringClaim(user.getClaims(), "iss");
        return issuer != null && issuer.contains(".ciamlogin.com");
    }

    /**
     * Extract email from identities claim (Microsoft Entra External ID specific)
     * identities format: [{"signInType":"emailAddress","issuer":"...","issuerAssignedId":"email@domain.com"}]
     */
    private static String extractEmailFromIdentities(Object identities) {
        if (identities == null) return null;

        try {
            // identities is usually a JSON array
            if (identities instanceof Iterable) {
                for (Object identity : (Iterable<?>) identities) {
                    if (identity instanceof Map<?, ?> identityMap) {
                        Object signInType = identityMap.get("signInType");
                        Object issuerAssignedId = identityMap.get("issuerAssignedId");

                        if ("emailAddress".equals(signInType) && issuerAssignedId != null) {
                            return issuerAssignedId.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }

        return null;
    }

    /**
     * Extract string value from claims, handling various data types
     */
    private static String getStringClaim(Map<String, Object> claims, String claimName) {
        if (claims == null || claimName == null) return null;

        Object value = claims.get(claimName);
        switch (value) {
            case null -> {
                return null;
            }
            case String s -> {
                return s;
            }

            // Handle arrays - take first non-empty string
            case Iterable iterable -> {
                for (Object item : iterable) {
                    if (item instanceof String && !((String) item).trim().isEmpty()) {
                        return (String) item;
                    }
                }
            }
            default -> {
                // Ignore other types
            }
        }

        return value.toString();
    }

    /**
     * Simple email validation
     */
    private static boolean isValidEmail(String email) {
        return email != null &&
                !email.trim().isEmpty() &&
                email.contains("@") &&
                email.contains(".");
    }

    /**
     * Format user information for display
     */
    public static String formatUserInfo(OidcUser user) {
        if (user == null) return "No user information";

        StringBuilder info = new StringBuilder();
        String displayName = getDisplayName(user);
        String email = getEmail(user);
        String userId = getUserId(user);

        info.append("Name: ").append(displayName);

        // Only add email if it's available
        if (email != null && !email.trim().isEmpty()) {
            info.append(", Email: ").append(email);
        }

        if (userId != null) {
            info.append(", ID: ").append(userId);
        }

        return info.toString();
    }
}