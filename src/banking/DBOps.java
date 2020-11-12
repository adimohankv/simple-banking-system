package banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBOps {
    private String filename;

    private String transferCardNumber;
    private int id;

    final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS card (\n"
        + "    id INTEGER PRIMARY KEY,\n"
        + "    number TEXT,\n"
        + "    pin TEXT,\n"
        + "    balance INTEGER DEFAULT 0\n"
        + "    );";

    public DBOps(String filename) {
        this.filename = filename;
    }

    private void setId(int id) {
        this.id = id;
    }

    private void setTransferCardNumber(String transferCardNumber) {
        this.transferCardNumber = transferCardNumber;
    }

    private Connection connect() {
        String url = "jdbc:sqlite:" + this.filename;

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    public void createTable() {
        try(Connection con = connect();
            Statement smt = con.createStatement()) {

            smt.execute(CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCard(String cardNumber, String pin) {
        String sqlInsert = "INSERT INTO card(number,pin) VALUES(?,?)";

        try(Connection conn = connect();
            PreparedStatement preparedStatement = conn.prepareStatement(sqlInsert);) {

            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pin);

            preparedStatement.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean login(String cardNumber, String pin) {
        String sqlSelect = "SELECT id, number, pin, balance FROM card WHERE number = ? AND pin = ?";

        try (Connection conn = connect();
            PreparedStatement preparedStatement = conn.prepareStatement(sqlSelect);) {

            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pin);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                if(rs.getString(2).equals(cardNumber) && rs.getString(3).equals(pin)) {
                    this.setId(rs.getInt(1));
                    System.out.println(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getBalance() {
        String sqlBalance = "SELECT balance FROM card WHERE id = ?";

        try (Connection conn = connect();
            PreparedStatement pStatement = conn.prepareStatement(sqlBalance);) {

            pStatement.setInt(1, getId());

            ResultSet rs = pStatement.executeQuery();

            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getId() {
        return this.id;
    }

    public String getTransferCardNumber() {
        return this.transferCardNumber;
    }

    public Boolean addIncome(int income) {
        String sqlIncome = "UPDATE card SET balance = balance + ? WHERE id = ?";

        try (Connection conn = connect();
            PreparedStatement preparedStatement = conn.prepareStatement(sqlIncome);) {

            preparedStatement.setInt(1, income);
            preparedStatement.setInt(2, this.getId());

            preparedStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean checkIfCardExists(String cardNumber) {
        String sqlSelect = "SELECT id, number FROM card WHERE number = ?";

        try (Connection conn = connect();
            PreparedStatement preparedStatement = conn.prepareStatement(sqlSelect);) {

            preparedStatement.setString(1, cardNumber);

            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                if(rs.getString(2).equals(cardNumber)) {
                    this.setTransferCardNumber(cardNumber);

                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean transferMoney(int transferAmmt) {
        String updateOwnerCard = "UPDATE card SET balance = balance - ? WHERE id = ?;";
        String updateTransfreeCard = "UPDATE card SET balance = balance + ? WHERE number = ?;";

        try (Connection conn = connect();
            PreparedStatement pStatement1 = conn.prepareStatement(updateOwnerCard);
            PreparedStatement pStatement2 = conn.prepareStatement(updateTransfreeCard);) {

            conn.setAutoCommit(false);

            pStatement1.setInt(1, transferAmmt);
            pStatement1.setInt(2, this.getId());

            pStatement2.setInt(1, transferAmmt);
            pStatement2.setString(2, this.getTransferCardNumber());

            int affectedRows1 = pStatement1.executeUpdate();
            int affectedRows2 = pStatement2.executeUpdate();

            if (affectedRows1 != 1 || affectedRows2 != 1) {
                conn.rollback();
            }

            conn.commit();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean deleteAccount() {
        String sqlDelete = "DELETE FROM card WHERE id = ?;";

        try (Connection conn = connect();
            PreparedStatement pStatement = conn.prepareStatement(sqlDelete)) {
            
            pStatement.setInt(1, getId());

            pStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}

