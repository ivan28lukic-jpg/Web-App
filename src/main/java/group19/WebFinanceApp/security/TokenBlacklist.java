package group19.WebFinanceApp.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    // token -> expiration
    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        if (token != null && !token.isBlank()) {
            revoked.put(token, expiresAt);
        }
    }

    public boolean isRevoked(String token) {
        if (token == null) return false;
        Instant exp = revoked.get(token);
        if (exp == null) return false;
        if (exp.isBefore(Instant.now())) {
            revoked.remove(token); // istekao – očisti
            return false;
        }
        return true;
    }

    /** Očisti povremeno istekle zapise (možeš zvati iz schedulera ako želiš). */
    public void purgeExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> it = revoked.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isBefore(now)) it.remove();
        }
    }
}