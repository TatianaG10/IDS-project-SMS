import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.awt.Point;

public class Main {

    public static void main(String[] args) throws Exception {

        // ── 1. Connect to RabbitMQ ─────────────────────────────────
        // Everyone shares the same Connection object (one TCP connection to the broker)
        // but each class creates its own Channel from it
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // change this if your broker is elsewhere
        Connection connection = factory.newConnection();

        // ── 2. Start MasterAntenna FIRST ──────────────────────────
        // It declares the fanout exchange — antennas need it to exist before they start
        MasterAntenna master = new MasterAntenna(connection);
        master.start();
        System.out.println("Master started");

        // ── 3. Start Antennas ─────────────────────────────────────
        // Ring layout: A → B → C → A  (each one's rightNeighbor is the next)
        // new Antenna(id, x, y, connection, rightNeighborId, ringSize)
        //
        //   [A] ──► [B] ──► [C]
        //    ▲                │
        //    └────────────────┘
        //
        int ringSize = 3;
        Antenna antennaA = new Antenna("A", 0,   0, connection, "B", ringSize);
        Antenna antennaB = new Antenna("B", 150, 0, connection, "C", ringSize);
        Antenna antennaC = new Antenna("C", 300, 0, connection, "A", ringSize);

        Thread.sleep(500); // give RabbitMQ a moment to finish binding all queues

        // ── 4. Create Users ───────────────────────────────────────
        // Users start near an antenna so they are in range
        User alice = new User("alice", 10,  0);  // near antenna A (at x=0)
        User bob   = new User("bob",  160,  0);  // near antenna B (at x=150)

        // ── 5. Connect users to their closest antenna ─────────────
        // In a real version the User broadcasts a ping and picks the closest one.
        // Here we do it manually to keep things simple and visible.
        antennaA.connectUser(alice);
        antennaB.connectUser(bob);

        Thread.sleep(200);

        // ── 6. Alice sends a message to Bob ───────────────────────
        // Alice is on antenna A. Bob is on antenna B.
        // A doesn't know Bob, so it forwards to B, which does know Bob → delivered.
        System.out.println("\n--- Alice sends to Bob ---");
        Message msg1 = new Message(
            "alice",        // senderId
            "bob",          // receiverId
            "Hey Bob!",     // content
            alice.getPosition(),
            "A"             // originalAntennaId — where the message enters the ring
        );
        antennaA.sendRight(msg1); // inject into the ring from antenna A

        Thread.sleep(500);

        // ── 7. Bob sends a message to Alice ───────────────────────
        System.out.println("\n--- Bob sends to Alice ---");
        Message msg2 = new Message(
            "bob",
            "alice",
            "Hey Alice!",
            bob.getPosition(),
            "B"
        );
        antennaB.sendRight(msg2);

        Thread.sleep(500);

        // ── 8. Test offline delivery ──────────────────────────────
        // Charlie is offline. Alice tries to message him.
        // The message will travel the full ring (3 hops) then go to master for storage.
        System.out.println("\n--- Alice sends to Charlie (offline) ---");
        Message msg3 = new Message(
            "alice",
            "charlie",
            "Where are you Charlie?",
            alice.getPosition(),
            "A"
        );
        antennaA.sendRight(msg3);

        Thread.sleep(500);

        // ── 9. Charlie comes online ───────────────────────────────
        // He appears near antenna C. Master should flush the stored message to him.
        System.out.println("\n--- Charlie comes online near antenna C ---");
        User charlie = new User("charlie", 310, 0); // near antenna C (at x=300)
        antennaC.connectUser(charlie);

        // In a real version, connectUser() would notify master automatically.
        // Here we call flushMessages() manually so you can see what happens.
        master.flushMessages("charlie", "C", antennaC.getChannel());

        Thread.sleep(500);

        // ── 10. Test movement ─────────────────────────────────────
        // Alice moves away from antenna A. She is now out of range.
        System.out.println("\n--- Alice moves far away ---");
        alice.updatePosition(400, 400); // far from every antenna
        if (!antennaA.isInRange(alice.getPosition())) {
            antennaA.disconnectUser(alice.getId());
            System.out.println("Alice disconnected from antenna A (out of range)");
        }

        // ── Cleanup ───────────────────────────────────────────────
        System.out.println("\n--- Done. Press Enter to shut down ---");
        System.in.read();
        connection.close();
    }
}