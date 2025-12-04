package fr.github.vera.services;

import fr.github.vera.model.BlacklistedToken;
import fr.github.vera.repository.BlacklistedTokenRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenBlacklistService {
    private static final Logger logger = LogManager.getLogger(TokenBlacklistService.class);
    private static final long INITIAL_DELAY = 2; // 2 minutes
    private static final long PURGE_INTERVAL = 60; // 60 minutes
    private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final ScheduledExecutorService scheduler;

    public TokenBlacklistService(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "blacklist-purge-thread");
            t.setDaemon(true);
            return t;
        });

        this.startPurgeTask();
    }

    private void startPurgeTask() {
        scheduler.scheduleAtFixedRate(
                this::purgeExpiredTokens,
                INITIAL_DELAY,
                PURGE_INTERVAL,
                TIME_UNIT
        );
    }

    public void shutdown() {
        try {
            if (!scheduler.isShutdown()) {
                logger.info("Stopping token purge service...");
                scheduler.shutdown();

                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                logger.info("Token purge service stopped");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void purgeExpiredTokens() {
        try {
            int deletedCount = blacklistedTokenRepository.cleanupExpiredTokens();
            if (deletedCount > 0) {
                logger.info("Purged {} expired tokens from blacklist", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during tokens purge", e);
        }
    }

    public void blacklistToken(String token, Date expiresAt, Integer userId) {
        BlacklistedToken blacklistedToken = new BlacklistedToken(
                token,
                expiresAt.toInstant(),
                userId,
                "logout"
        );
        blacklistedTokenRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }
}