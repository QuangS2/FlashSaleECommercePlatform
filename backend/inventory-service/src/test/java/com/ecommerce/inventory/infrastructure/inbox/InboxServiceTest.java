package com.ecommerce.inventory.infrastructure.inbox;

import com.ecommerce.inventory.infrastructure.persistence.entity.InboxEventEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.SpringDataInboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboxServiceTest {

    @Mock
    private SpringDataInboxEventRepository inboxEventRepository;

    @InjectMocks
    private InboxService inboxService;

    @Test
    void testIsAlreadyProcessed_NullOrBlankMessageId() {
        assertFalse(inboxService.isAlreadyProcessed(null, "group-1"));
        assertFalse(inboxService.isAlreadyProcessed("   ", "group-1"));
        verifyNoInteractions(inboxEventRepository);
    }

    @Test
    void testIsAlreadyProcessed_ExistsById() {
        when(inboxEventRepository.existsById("msg-1")).thenReturn(true);

        boolean processed = inboxService.isAlreadyProcessed("msg-1", "group-1");

        assertTrue(processed);
        verify(inboxEventRepository, never()).save(any());
    }

    @Test
    void testIsAlreadyProcessed_NewMessage_Success() {
        when(inboxEventRepository.existsById("msg-new")).thenReturn(false);

        boolean processed = inboxService.isAlreadyProcessed("msg-new", "group-1");

        assertFalse(processed);
        verify(inboxEventRepository, times(1)).save(any(InboxEventEntity.class));
    }

    @Test
    void testIsAlreadyProcessed_DataIntegrityViolation_ReturnsTrue() {
        when(inboxEventRepository.existsById("msg-dup")).thenReturn(false);
        when(inboxEventRepository.save(any(InboxEventEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        boolean processed = inboxService.isAlreadyProcessed("msg-dup", "group-1");

        assertTrue(processed);
    }
}
