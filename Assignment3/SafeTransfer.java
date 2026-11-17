class Wallet {
    final int id; int balance;
    Wallet(int id, int balance) { this.id = id; this.balance = balance; }
}
public class SafeTransfer {
    static void transfer(Wallet from, Wallet to, int amount) {
        if (from == to) return;
        Wallet first = from.id < to.id ? from : to;
        Wallet second = from.id < to.id ? to : from;
        synchronized (first) {
            synchronized (second) {
                if (from.balance >= amount) { from.balance -= amount; to.balance += amount; }
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Wallet a = new Wallet(1, 1000), b = new Wallet(2, 1000);
        Thread t1 = new Thread(() -> transfer(a, b, 200));
        Thread t2 = new Thread(() -> transfer(b, a, 100));
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("Wallet A: " + a.balance);
        System.out.println("Wallet B: " + b.balance);
    }
}
