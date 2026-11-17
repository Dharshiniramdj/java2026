import java.io.*;
import java.util.*;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double balance = 10000;
        try {
            try (FileReader reader = new FileReader("card.txt")) { reader.read(); }
            System.out.print("Account number: "); int account = sc.nextInt();
            System.out.print("Withdrawal amount: "); double amount = sc.nextDouble();
            if (account <= 0 || amount <= 0) throw new IllegalArgumentException("Account and amount must be positive");
            if (amount > balance) throw new IllegalArgumentException("Insufficient balance");
            balance -= amount;
            System.out.println("Withdrawal successful. Balance: " + balance);
        } catch (FileNotFoundException e) { System.out.println("Card file missing"); }
        catch (InputMismatchException e) { System.out.println("Invalid numeric input"); }
        catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }
        catch (IOException | SecurityException e) { System.out.println("File access error: " + e.getMessage()); }
        finally { System.out.println("ATM session ended"); sc.close(); }
    }
}
