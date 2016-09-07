import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/mydb?serverTimezone=UTC&useSSL=false";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "qwerty";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in);
        ) {
            Connection conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
           // initDB(conn);

            while (true) {
                System.out.println("Input '1' to add Product");
                System.out.println("Input '2' to add Customer");
                System.out.println("Input '3' to add Order");
                System.out.println("Input '4' to view Order");

                String s = sc.nextLine();
                switch (s) {
                    case "1":
                        addProduct(sc, conn);
                        break;
                    case "2":
                        addCustomer(sc, conn);
                        break;
                    case "3":
                        addOrder(sc, conn);
                        break;
                    case "4":
                        viewOrder(sc, conn);
                        break;
                    default:
                        return;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewOrder(Scanner sc, Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        System.out.println("Input order id");
        String sId = sc.nextLine();
        ResultSet rs = st.executeQuery("SELECT Customer FROM Orders WHERE id=" + sId);
        rs.next();
        System.out.println("Order number - " + sId + ", made by - " + rs.getString(1));
        rs.close();
        st.close();
        String statement = "SELECT * FROM Orders WHERE id=" + sId;

        try (PreparedStatement ps = conn.prepareStatement(statement);
             ResultSet res = ps.executeQuery()) {

            ResultSetMetaData md = res.getMetaData();

            for (int i = 3; i <= md.getColumnCount(); i++)
                System.out.print(md.getColumnName(i) + "\t\t\t");
            System.out.println();

            while (res.next()) {
                for (int i = 3; i <= md.getColumnCount(); i++) {
                    System.out.print(res.getString(i) + "\t\t\t");
                }
                System.out.println();
            }
        }

    }

    private static void addOrder(Scanner sc, Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT MAX(id) AS id FROM Orders");
        rs.next();
        Integer orderId = rs.getInt(1) + 1;
        rs.close();
        st.close();

        String statement = "INSERT INTO Orders (id, Customer, Product, Amount, price)" +
                " VALUES(" +
                " ?," +
                "?,?,?," +
                "((SELECT price FROM Products WHERE name=?)*?)" +
                ")";

        System.out.println("Enter Customer name :");
        String customName = sc.nextLine();
        System.out.println("Enter number of different products in order");
        int n = Integer.parseInt(sc.nextLine());
        for (int i = 0; i < n; i++) {
            System.out.println("Enter Product name :");
            String prodName = sc.nextLine();
            System.out.println("Enter amount of product");
            Integer amount = Integer.parseInt(sc.nextLine());
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, orderId);
                ps.setString(2, customName);
                ps.setString(3, prodName);
                ps.setInt(4, amount);
                ps.setString(5, prodName);
                ps.setInt(6, amount);
                ps.executeUpdate();
            }

        }
    }

    private static void addCustomer(Scanner sc, Connection conn) throws SQLException {
        String statement = "INSERT INTO Customers (name)" +
                " VALUES(?)";

        System.out.println("Enter Customer name :");
        String name = sc.nextLine();

        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    private static void addProduct(Scanner sc, Connection conn) throws SQLException {
        String statement = "INSERT INTO Products (name, price)" +
                " VALUES(?, ?)";

        System.out.println("Enter product name :");
        String name = sc.nextLine();
        System.out.println("Enter product price :");
        String sPrice = sc.nextLine();
        double price = Double.parseDouble(sPrice);

        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.executeUpdate();
        }
    }

    private static void initDB(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
//            Товары
            st.execute("DROP TABLE IF EXISTS Products");
            st.execute("CREATE TABLE Products (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    " name VARCHAR(255) NOT NULL," +
                    " price DEC(15,2) NOT NULL)");
//            Заказчики
            st.execute("DROP TABLE IF EXISTS Customers");
            st.execute("CREATE TABLE Customers (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    " name VARCHAR(255) NOT NULL)");
//            Заказы
            st.execute("DROP TABLE IF EXISTS Orders");
            st.execute("CREATE TABLE Orders (" +
                    "id BIGINT NOT NULL ," +
                    " Customer VARCHAR(255) NOT NULL," +
                    " Product VARCHAR(255) NOT NULL," +
                    " Amount BIGINT NOT NULL ," +
                    " Price DEC(15,2) NOT NULL)");
        }
    }
}
