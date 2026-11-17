import java.sql.*;
public class TransactionSafety {
    static void transfer(Connection c,int from,int to,int amount) throws SQLException {
        if(amount<=0 || from==to)throw new IllegalArgumentException("Invalid transfer");
        boolean old=c.getAutoCommit(); c.setAutoCommit(false);
        try {
            try(PreparedStatement debit=c.prepareStatement(
                    "UPDATE wallets SET balance=balance-? WHERE id=? AND balance>=?")){
                debit.setInt(1,amount); debit.setInt(2,from); debit.setInt(3,amount);
                if(debit.executeUpdate()!=1)throw new SQLException("Insufficient funds or wallet missing");
            }
            try(PreparedStatement credit=c.prepareStatement(
                    "UPDATE wallets SET balance=balance+? WHERE id=?")){
                credit.setInt(1,amount); credit.setInt(2,to);
                if(credit.executeUpdate()!=1)throw new SQLException("Destination missing");
            }
            c.commit();
        } catch(SQLException | RuntimeException e){c.rollback();throw e;}
        finally {c.setAutoCommit(old);}
    }
    static boolean login(Connection c,String id,String password) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM users WHERE id=? AND pwd=?")){
            p.setString(1,id); p.setString(2,password);
            try(ResultSet r=p.executeQuery()){return r.next();}
        }
    }
    public static void main(String[] args){
        System.out.println("JDBC transaction and parameterized-login methods ready.");
        System.out.println("Supply a local test database Connection to call them.");
    }
}
