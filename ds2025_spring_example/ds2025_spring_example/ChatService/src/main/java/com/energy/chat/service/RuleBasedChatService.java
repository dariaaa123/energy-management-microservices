package com.energy.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class RuleBasedChatService {

    private final List<ChatRule> rules = new ArrayList<>();

    public RuleBasedChatService() {
        initializeRules();
    }

    private void initializeRules() {
        // Rule 1: Greeting
        rules.add(new ChatRule(
            Arrays.asList("hello", "hi", "hey", "good morning", "good afternoon", "good evening"),
            "Hello! Welcome to Energy Management System support. How can I help you today?"
        ));

        // Rule 2: Account/Login issues
        rules.add(new ChatRule(
            Arrays.asList("login", "can't login", "cannot login", "password", "forgot password", "reset password", "account"),
            "For login issues, please try the following:\n1. Make sure your username and password are correct\n2. Clear your browser cache\n3. If you forgot your password, please contact the administrator to reset it.\nWould you like me to connect you with an admin?"
        ));

        // Rule 3: Device related
        rules.add(new ChatRule(
            Arrays.asList("device", "add device", "new device", "register device", "my devices"),
            "To manage your devices:\n- Admins can add new devices from the Devices Management section\n- Devices can be assigned to users by clicking 'Assign'\n- Each device has a maximum consumption threshold for alerts\nIs there something specific about devices you need help with?"
        ));

        // Rule 4: Energy consumption
        rules.add(new ChatRule(
            Arrays.asList("consumption", "energy", "usage", "electricity", "power", "kwh"),
            "You can view your energy consumption in the Energy Consumption Dashboard:\n1. Select your device from the dropdown\n2. Choose a date to view hourly consumption\n3. The chart shows consumption patterns throughout the day\nYou'll also receive alerts if consumption exceeds the device limit."
        ));

        // Rule 5: Alerts/Notifications
        rules.add(new ChatRule(
            Arrays.asList("alert", "notification", "warning", "overconsumption", "exceeded", "limit"),
            "Overconsumption alerts are triggered when your device exceeds its maximum consumption limit. You'll receive real-time notifications when this happens. To reduce alerts:\n- Check for faulty appliances\n- Optimize usage during peak hours\n- Contact admin to adjust the limit if needed"
        ));

        // Rule 6: Billing/Payment
        rules.add(new ChatRule(
            Arrays.asList("bill", "billing", "payment", "pay", "invoice", "cost", "price"),
            "For billing inquiries:\n- Your energy consumption is tracked hourly\n- Billing is calculated based on total kWh usage\n- For detailed billing information, please contact your energy provider\nWould you like to speak with an administrator about billing?"
        ));

        // Rule 7: Technical support
        rules.add(new ChatRule(
            Arrays.asList("error", "bug", "problem", "issue", "not working", "broken", "crash"),
            "I'm sorry you're experiencing technical issues. Please try:\n1. Refresh the page\n2. Clear browser cache and cookies\n3. Try a different browser\nIf the problem persists, I'll connect you with a technical administrator."
        ));

        // Rule 8: Contact/Human support
        rules.add(new ChatRule(
            Arrays.asList("human", "agent", "admin", "administrator", "speak to", "talk to", "contact", "real person"),
            "I'll connect you with an administrator right away. Please wait a moment while I notify them. In the meantime, feel free to describe your issue in detail."
        ));

        // Rule 9: Hours/Availability/Time
        rules.add(new ChatRule(
            Arrays.asList("hours", "available", "open", "when", "schedule", "time", "clock", "what time"),
            "Our support team is available:\n- Monday to Friday: 9:00 AM - 6:00 PM\n- Saturday: 10:00 AM - 2:00 PM\n- Sunday: Closed\nThe chatbot is available 24/7 for basic inquiries!"
        ));

        // Rule 10: Thank you/Goodbye
        rules.add(new ChatRule(
            Arrays.asList("thank", "thanks", "bye", "goodbye", "see you", "appreciate"),
            "You're welcome! Thank you for using Energy Management System. If you have any more questions, feel free to ask. Have a great day!"
        ));

        // Rule 11: How to use/Help
        rules.add(new ChatRule(
            Arrays.asList("how to", "help", "guide", "tutorial", "instructions", "how do i"),
            "Here's a quick guide:\n- Dashboard: View your assigned devices and energy data\n- Charts: Select a device and date to see consumption\n- Notifications: Real-time alerts for overconsumption\n- Chat: Get help from our support team\nWhat specific feature would you like help with?"
        ));

        // Rule 12: Security
        rules.add(new ChatRule(
            Arrays.asList("security", "secure", "safe", "privacy", "data", "protection"),
            "Your data security is our priority:\n- All connections are encrypted\n- Passwords are securely hashed\n- JWT tokens for authentication\n- Data is stored in secure databases\nFor security concerns, please contact an administrator."
        ));

        log.info("Initialized {} chat rules", rules.size());
    }

    public Optional<String> findResponse(String message) {
        String lowerMessage = message.toLowerCase().trim();
        
        for (ChatRule rule : rules) {
            for (String keyword : rule.keywords) {
                if (lowerMessage.contains(keyword)) {
                    log.info("Matched rule with keyword: {}", keyword);
                    return Optional.of(rule.response);
                }
            }
        }
        
        return Optional.empty();
    }

    private static class ChatRule {
        List<String> keywords;
        String response;

        ChatRule(List<String> keywords, String response) {
            this.keywords = keywords;
            this.response = response;
        }
    }
}
