public class Main {
    public static void main(String[] args) throws Exception {
        String host = "localhost";

        // 1. 
        MasterAntennaNode master = new MasterAntennaNode("master.antenna", host);

        // 2. Start antennas => ring: A → B → C → A
        // new AntennaNode(id, x, y, leftNeighbor, ringSize, masterQueue, host)
        AntennaNode a = new AntennaNode("A", 0,  0, "B", 3, "master.antenna", host);
        AntennaNode b = new AntennaNode("B", 5,  0, "C", 3, "master.antenna", host);
        AntennaNode c = new AntennaNode("C", 10, 0, "A", 3, "master.antenna", host);

        Thread.sleep(500); // let queues settle

        // 3. Start users
        UserNode alice = new UserNode("alice", 1, 0, 3.0, host);
        UserNode bob   = new UserNode("bob",   6, 0, 3.0, host);

        Thread.sleep(1000); // let them connect to an antenna

        // 4. Alice sends a message to Bob
        alice.sendMessage("bob", "Hey Bob!");
    }
}