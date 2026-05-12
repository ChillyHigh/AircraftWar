package edu.hitsz.DAO;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class UserDataDaoImpl implements UserDataDao {
    private List<UserData> userdata = new ArrayList<>();
    private final String filePath;

    public UserDataDaoImpl(String difficulty) {
        this.filePath = "./UserDataRank_" + difficulty.toLowerCase() + ".txt";
        this.userdata = getAllUserData();
    }

    @Override
    public List<UserData> getAllUserData() {
        List<UserData> userDataList = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            userdata = userDataList;
            return userDataList;
        }

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    userDataList.add(new UserData(
                            Integer.parseInt(parts[0].trim()),
                            parts[1].trim(),
                            Integer.parseInt(parts[2].trim()),
                            parts[3].trim()
                    ));
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        userdata = userDataList;
        return userDataList;
    }

    @Override
    public void saveUserData() {
        userdata.sort(Comparator.comparingInt(UserData::getScore).reversed().thenComparingInt(UserData::getId));
        try (BufferedWriter out = new BufferedWriter(new FileWriter(filePath))) {
            for (UserData userData : userdata) {
                out.write(userData.getId() + ","
                        + userData.getUsername() + ","
                        + userData.getScore() + ","
                        + userData.getTime() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUserData(UserData userData) {
        userdata.add(userData);
        saveUserData();
    }

    @Override
    public void deleteUserData(int id) {
        userdata.removeIf(userData -> userData.getId() == id);
        saveUserData();
    }

    public int nextId() {
        int maxId = 0;
        for (UserData userData : userdata) {
            maxId = Math.max(maxId, userData.getId());
        }
        return maxId + 1;
    }
}
