package com.energy.websocket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {
    private String type; // USER_MESSAGE, ADMIN_MESSAGE, BOT_RESPONSE, TYPING, READ
    private String senderId;
    private String senderName;
    private String recipientId;
    private String content;
    private String timestamp;
    
    @JsonProperty("isAdmin")
    private boolean admin;
    
    public boolean isAdmin() {
        return admin;
    }
    
    public void setIsAdmin(boolean isAdmin) {
        this.admin = isAdmin;
    }
}
