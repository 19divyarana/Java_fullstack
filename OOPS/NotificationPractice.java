/*
 * EXERCISE 3: Notification System (Strategy Pattern)
 * Concepts: Interfaces, Loose Coupling, Dependency Inversion, Composition
 *
 * TASK:
 * 1. Complete the NotificationSender interface with one method:
 *    send(String message, String recipient)
 * 2. Implement EmailSender, SmsSender, and PushSender - each should
 *    just print a message simulating that type of send, e.g.:
 *    "[EMAIL] to bob@mail.com: Your order shipped!"
 * 3. Complete the NotificationService class:
 *    - it should NOT know about Email/Sms/Push directly
 *    - it takes a NotificationSender in its constructor (dependency
 *      injection) and stores it
 *    - it has a method notifyUser(String message, String recipient)
 *      that delegates to the sender
 * 4. In main(), create a NotificationService with different senders
 *    and send a few notifications, showing you can swap strategies
 *    at runtime without changing NotificationService's code.
 *
 * BONUS:
 * - Add a MultiChannelSender that implements NotificationSender but
 *   internally holds a List<NotificationSender> and sends through all
 *   of them (Composite pattern).
 * - Discuss out loud: why is this better than an if/else chain checking
 *   a "type" string inside NotificationService?
 *
 * Compile:  javac NotificationPractice.java
 * Run:      java NotificationPractice
 */

import java.util.ArrayList;
import java.util.List;

public class NotificationPractice {

    interface NotificationSender {
        void send(String message, String recipient);
    }

    static class EmailSender implements NotificationSender {
        @Override
        public void send(String message, String recipient) {
            // TODO: implement
        }
    }

    static class SmsSender implements NotificationSender {
        @Override
        public void send(String message, String recipient) {
            // TODO: implement
        }
    }

    static class PushSender implements NotificationSender {
        @Override
        public void send(String message, String recipient) {
            // TODO: implement
        }
    }

    // BONUS class - implement after the three above work
    static class MultiChannelSender implements NotificationSender {
        private List<NotificationSender> senders = new ArrayList<>();

        public void addSender(NotificationSender sender) {
            senders.add(sender);
        }

        @Override
        public void send(String message, String recipient) {
            // TODO: loop through senders and call send() on each
        }
    }

    static class NotificationService {
        private NotificationSender sender;

        public NotificationService(NotificationSender sender) {
            this.sender = sender;
        }

        public void notifyUser(String message, String recipient) {
            // TODO: delegate to sender
        }

        // BONUS: allow swapping the strategy at runtime
        public void setSender(NotificationSender sender) {
            this.sender = sender;
        }
    }

    public static void main(String[] args) {
        NotificationService service = new NotificationService(new EmailSender());
        service.notifyUser("Your order shipped!", "bob@mail.com");

        service.setSender(new SmsSender());
        service.notifyUser("Your OTP is 4321", "+91-9999999999");

        // TODO: try MultiChannelSender once implemented
    }
}
