package com.castify.domain.messaging

import java.util.*

interface ConversationRepository {
    fun saveConversation(conversation: Conversation): Conversation
    fun findConversationById(conversationId: UUID): Conversation?
    fun findConversationsByUser(userId: UUID): List<Conversation>
    fun findConversationBetweenUsers(talentUserId: UUID, clientUserId: UUID): Conversation?

    fun saveMessage(message: Message): Message
    fun findMessageById(messageId: UUID): Message?
    fun findMessagesByConversation(conversationId: UUID): List<Message>
}
