package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@ApplicationScoped
public class WordService {

    @ConfigProperty(name = "quarkus.datasource.jdbc.url") 
    String url;

    @ConfigProperty(name = "quarkus.datasource.jdbc.username") 
    String username;

    @ConfigProperty(name = "quarkus.datasource.jdbc.password") 
    String password;

    @ConfigProperty(name = "client") 
    String client;

    public RestResponse<JSONObject> check(String word) throws IOException {
        String[] response = new String[5];
        if (wordDoesNotExist(word)) {
            JSONObject json = new JSONObject();
            json.put("response", "Can not find the word in the database. Please try a different word.");
            return build(json);
        }

        String solution = getSolution();
        for (int i = 0; i < word.length(); i++) {
            String wordChar = word.substring(i, i + 1);
            String solutionChar = solution.substring(i, i + 1);
            if (wordChar.equalsIgnoreCase(solutionChar)) {
                response[i] = "lightgreen";
            } else if (solution.toUpperCase().contains(wordChar.toUpperCase())) {
                response[i] = "yellow";
            } else {
                response[i] = "#a8a8a8";
            }
        }

        // Dealing with edge-case
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < word.length(); i++) {
            if (response[i] == "yellow") {
                char wordChar = word.charAt(i);
                ArrayList<String> responseVals = new ArrayList<String>();
                long wordCount = 0;
                for (int j = 0; j < word.length(); j++) {
                    if (word.charAt(j) == wordChar) {
                        wordCount = wordCount + 1;
                        responseVals.add(response[j]);
                    }
                }
                long solutionCount = solution.chars().filter(ch -> ch == wordChar).count();
                if (wordCount > solutionCount) {
                    int numberOfRight = 0;
                    for (int k = 0; k < responseVals.size(); k++) {
                        if (responseVals.get(k) == "lightgreen") {
                            numberOfRight++;
                        }
                    }
                    String wordStr = word.substring(i, i + 1);
                    if (solutionCount - numberOfRight - (map.get(wordStr) != null ? map.get(wordStr) : 0) <= 0) {
                        response[i] = "#a8a8a8";
                    } else {
                        if (map.get(wordStr) != null) {
                            map.put(wordStr, map.get(wordStr) + 1);
                        } else {
                            map.put(wordStr, 1);
                        }
                    }
                }
            }
        }
        JSONObject json = new JSONObject();
        json.put("response", response);

        return build(json);
    }

    public boolean wordDoesNotExist(String word) {
        Connection conn = connect();
        if (conn != null) {
            String countSQL = "SELECT COUNT(*) AS total FROM fiveletterwords WHERE word =?";
            PreparedStatement countSTMT;
            try {
                countSTMT = conn.prepareStatement(countSQL);
                countSTMT.setString(1, word.toLowerCase());
                ResultSet rs1 = countSTMT.executeQuery();
                while (rs1.next()) {
                    int count = rs1.getInt("total");
                    return count == 0;
                }
                conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    public String getSolution() {
        String solution = "";
        Connection conn = connect();
        if (conn != null) {
            Calendar calendar = Calendar.getInstance();
            Date date = new Date();
            calendar.setTime(date);
            Integer day = calendar.get(Calendar.DAY_OF_MONTH);
            Integer month = calendar.get(Calendar.MONTH) + 1;
            Integer year = calendar.get(Calendar.YEAR);
            String constructedDate = day + "-" + month + "-" + year;
            String SQL = "SELECT * FROM fiveletterwords WHERE date =?";
            PreparedStatement stmt;
            try {
                stmt = conn.prepareStatement(SQL);
                stmt.setString(1, constructedDate);
                ResultSet rs2 = stmt.executeQuery();
                while (rs2.next()) {
                    solution = rs2.getString("word");
                }
                conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return solution;
    }

    public RestResponse<JSONObject> solution() {
        String solution = getSolution();

        JSONObject json = new JSONObject();
        json.put("response", solution);

        return build(json);
    }

    public RestResponse<JSONObject> build(JSONObject json) {
        return ResponseBuilder.ok(json, MediaType.TEXT_PLAIN_TYPE)
        .header("Access-Control-Allow-Origin", client)
        .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        .header("Access-Control-Allow-Headers", "Content-Type")
        .build();
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;
    }
}