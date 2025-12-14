package com.castify.web.controller

import com.castify.usecase.dto.ConversationResponse
import com.castify.usecase.dto.CreateConversationRequest
import com.castify.usecase.dto.MessageResponse
import com.castify.usecase.dto.SendMessageRequest
import com.castify.usecase.messaging.CreateConversationUseCase
import com.castify.usecase.messaging.GetConversationsUseCase
import com.castify.usecase.messaging.GetMessagesUseCase
import com.castify.usecase.messaging.SendMessageUseCase
import com.castify.usecase.messaging.MarkMessageAsReadUseCase
import com.castify.web.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations", description = "Messaging endpoints")
class ConversationController(
    private val createConversationUseCase: CreateConversationUseCase,
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new conversation")
    fun createConversation(@Valid @RequestBody request: CreateConversationRequest): ConversationResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return createConversationUseCase.execute(userId, request)
    }

    @GetMapping
    @Operation(summary = "Get all conversations for current user")
    fun getConversations(): List<ConversationResponse> {
        val userId = SecurityUtils.getCurrentUserId()
        return getConversationsUseCase.execute(userId)
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get all messages in a conversation")
    fun getMessages(@PathVariable conversationId: UUID): List<MessageResponse> {
        val userId = SecurityUtils.getCurrentUserId()
        return getMessagesUseCase.execute(userId, conversationId)
    }

    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a message in a conversation")
    fun sendMessage(
        @PathVariable conversationId: UUID,
        @Valid @RequestBody request: SendMessageRequest
    ): MessageResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return sendMessageUseCase.execute(userId, conversationId, request)
    }

    @PutMapping("/{conversationId}/messages/{messageId}/read")
    @Operation(summary = "Mark a message as read")
    fun markMessageAsRead(
        @PathVariable conversationId: UUID,
        @PathVariable messageId: UUID
    ): MessageResponse {
        val userId = SecurityUtils.getCurrentUserId()
        return markMessageAsReadUseCase.execute(userId, conversationId, messageId)
    }
}
