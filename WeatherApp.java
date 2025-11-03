import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class WeatherApp extends Frame implements ActionListener {
    Label lblTitle, lblCity, lblTemp, lblHumidity, lblWind, lblCondition, lblStatus;
    TextField tfCity, tfTemp, tfHumidity, tfWind, tfCondition;
    Button btnGetWeather, btnAdd, btnUpdate, btnDelete, btnClear, btnShowAll;
    TextArea displayArea;
    Panel mainPanel, inputPanel, buttonPanel, statusPanel;
    private static final String DB_URL = "jdbc:mysql://localhost:3305/weatherdb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    public WeatherApp() {
        setTitle("Weather Information System (CRUD)");
        setSize(650, 750);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 245, 250));
        lblTitle = new Label("Weather Data Management", Label.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);
        mainPanel = new Panel(new BorderLayout(10, 10));
        inputPanel = new Panel(new GridLayout(6, 2, 10, 10));
         lblCity = new Label("City:");
        tfCity = new TextField(20);
        lblTemp = new Label("Temperature (°C):");
        tfTemp = new TextField(15);
         lblHumidity = new Label("Humidity (%):");
        tfHumidity = new TextField(15);
         lblWind = new Label("Wind Speed (km/h):");
        tfWind = new TextField(15);
         lblCondition = new Label("Condition:");
        tfCondition = new TextField(15);
        inputPanel.add(lblCity);
        inputPanel.add(tfCity);
        inputPanel.add(lblTemp);
        inputPanel.add(tfTemp);
        inputPanel.add(lblHumidity);
        inputPanel.add(tfHumidity);
        inputPanel.add(lblWind);
        inputPanel.add(tfWind);
        inputPanel.add(lblCondition);
        inputPanel.add(tfCondition);    
         mainPanel.add(inputPanel, BorderLayout.NORTH);
        buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGetWeather = new Button("Get Weather");
        btnAdd = new Button("Add");
        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnUpdate = new Button("Update");
        btnUpdate.setBackground(new Color(23, 162, 184));
        btnUpdate.setForeground(Color.WHITE);
        btnDelete = new Button("Delete");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnClear = new Button("Clear Fields");
        btnShowAll = new Button("Show All Records");
         buttonPanel.add(btnGetWeather);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnShowAll);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        statusPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        lblStatus = new Label("Welcome! Enter a city name to begin.", Label.LEFT);
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        statusPanel.add(lblStatus);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        displayArea.setVisible(false);
        mainPanel.add(displayArea, BorderLayout.SOUTH);
        btnGetWeather.addActionListener(this);
        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
        btnClear.addActionListener(this);
        btnShowAll.addActionListener(this);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
        setVisible(true);
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnGetWeather) {
            fetchWeatherData();
        } else if (e.getSource() == btnAdd) {
            addWeatherData();
        } else if (e.getSource() == btnUpdate) {
            updateWeatherData();
        } else if (e.getSource() == btnDelete) {
            deleteWeatherData();
        } else if (e.getSource() == btnClear) {
            clearFields();
        } else if (e.getSource() == btnShowAll) {
            displayArea.setVisible(true);
            loadAllWeatherData();
        }}
     private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    private void fetchWeatherData() {
        String city = tfCity.getText().trim();
        if (city.isEmpty()) {
            setStatus("Please enter a city name to fetch.", Color.RED);
            return;
        }
        String query = "SELECT * FROM weather_data WHERE city = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, city);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tfTemp.setText(rs.getString("temperature"));
                    tfHumidity.setText(rs.getString("humidity"));
                    tfWind.setText(rs.getString("wind_speed"));
                    tfCondition.setText(rs.getString("weather_condition"));
                    setStatus("Data found for " + city, new Color(0, 128, 0));
                    showMessage("Weather details fetched for " + city);
                } else {
                    setStatus("No data found for " + city, Color.RED);
                    showMessage("No record found for " + city);
                    clearDisplayFields();
                }
            }
        } catch (SQLException ex) {
            handleException(ex);
        }
    }
    private void addWeatherData() {
        String city = tfCity.getText().trim();
        if (city.isEmpty()) {
            setStatus("City cannot be empty.", Color.RED);
            return;
        }
        try {
            Double.parseDouble(tfTemp.getText());
            Integer.parseInt(tfHumidity.getText());
            Double.parseDouble(tfWind.getText());
        } catch (NumberFormatException e) {
            setStatus("Temperature, Humidity, Wind must be numbers.", Color.RED);
            return;
        }

        String query = "INSERT INTO weather_data (city, temperature, humidity, wind_speed, weather_condition) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, city);
            pstmt.setDouble(2, Double.parseDouble(tfTemp.getText()));
            pstmt.setInt(3, Integer.parseInt(tfHumidity.getText()));
            pstmt.setDouble(4, Double.parseDouble(tfWind.getText()));
            pstmt.setString(5, tfCondition.getText().trim());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                setStatus("Record added for " + city, new Color(0, 128, 0));
                showMessage("✅ Successfully added record for " + city + "!");
                clearFields();
            }
        } catch (SQLException ex) {
            handleException(ex);
        }
    }
    private void updateWeatherData() {
        String city = tfCity.getText().trim();
        if (city.isEmpty()) {
            setStatus("Please enter a city name to update.", Color.RED);
            return;
        }
        String query = "UPDATE weather_data SET temperature=?, humidity=?, wind_speed=?, weather_condition=? WHERE city=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             pstmt.setDouble(1, Double.parseDouble(tfTemp.getText()));
            pstmt.setInt(2, Integer.parseInt(tfHumidity.getText()));
            pstmt.setDouble(3, Double.parseDouble(tfWind.getText()));
            pstmt.setString(4, tfCondition.getText().trim());
            pstmt.setString(5, city);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                setStatus("Record updated for " + city, new Color(0, 128, 0));
                showMessage("✅ Record updated successfully for " + city + "!");
                clearFields();
            } else {
                setStatus("No record found for " + city, Color.RED);
                showMessage("❌ No record found for " + city);
            }
        } catch (SQLException ex) {
            handleException(ex);
        }
    }
    private void deleteWeatherData() {
        String city = tfCity.getText().trim();
        if (city.isEmpty()) {
            setStatus("Enter city name to delete.", Color.RED);
            return;
        }
         String query = "DELETE FROM weather_data WHERE city = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, city);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                setStatus("Deleted record for " + city, new Color(0, 128, 0));
                showMessage("🗑️ Record deleted successfully for " + city + "!");
                clearFields();
            } else {
                setStatus("No record found for " + city, Color.RED);
                showMessage("❌ No record found for " + city);
            }
        } catch (SQLException ex) {
            handleException(ex);
        }
    }
    private void loadAllWeatherData() {
        String query = "SELECT * FROM weather_data";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s %-12s %-10s %-12s %-18s\n", "City", "Temp", "Humidity", "Wind", "Condition"));
        sb.append("--------------------------------------------------------------------------\n");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                sb.append(String.format("%-15s %-12.2f %-10d %-12.2f %-18s\n",
                        rs.getString("city"),
                        rs.getDouble("temperature"),
                        rs.getInt("humidity"),
                        rs.getDouble("wind_speed"),
                        rs.getString("weather_condition")));
            }
            displayArea.setText(sb.toString());
            displayArea.setVisible(true);
            displayArea.revalidate();
            displayArea.repaint();
            showMessage("✅ All records displayed successfully!");
        } catch (SQLException ex) {
            handleException(ex);
        }
    }

  private void clearFields() {
        tfCity.setText("");
        clearDisplayFields();
        setStatus("Fields cleared.", Color.GRAY);
    }

    private void clearDisplayFields() {
        tfTemp.setText("");
        tfHumidity.setText("");
        tfWind.setText("");
        tfCondition.setText("");
    }

    private void setStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }
    private void showMessage(String message) {
        Dialog d = new Dialog(this, "Message", true);
        d.setLayout(new FlowLayout());
        Label msg = new Label(message);
        Button ok = new Button("OK");
        ok.addActionListener(ae -> d.dispose());
        d.add(msg);
        d.add(ok);
        d.setSize(350, 150);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
    private void handleException(Exception ex) {
        ex.printStackTrace();
        setStatus("Error: " + ex.getMessage(), Color.RED);
        showMessage("⚠️ Database Error: " + ex.getMessage());
    }   public static void main(String[] args) {
        new WeatherApp();
    }}
