package com.ecommerce.inventory.infrastructure.inbox;

import com.ecommerce.inventory.infrastructure.persistence.entity.InboxEventEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.SpringDataInboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service managing Idempotent Consumer pattern via inbox_events table (Bảng 15 & Mục 4.2.3).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboxService {

    private final SpringDataInboxEventRepository inboxEventRepository;

    /**
     * Checks whether the given messageId has already been processed by consumerGroup.
     * If not, records it into inbox_events atomically.
     *
     * @return true if already processed (duplicate message, should be skipped); false if newly recorded.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean isAlreadyProcessed(String messageId, String consumerGroup) {
        if (messageId == null || messageId.isBlank()) {
            return false;
        }

        if (inboxEventRepository.existsById(messageId)) {
            log.warn("[IDEMPOTENT CONSUMER] Message [{}] đã được xử lý bởi nhóm [{}]. Bỏ qua trùng lặp.",
                    messageId, consumerGroup);
            return true;
        }

        try {
            inboxEventRepository.save(InboxEventEntity.builder()
                    .messageId(messageId)
                    .consumerGroup(consumerGroup)
                    .processedAt(Instant.now())
                    .build());
            return false;
        } catch (DataIntegrityViolationException ex) {
            log.warn("[IDEMPOTENT CONSUMER] Duplicate key on message [{}], bỏ qua.", messageId);
            return true;
        }
    }
}
