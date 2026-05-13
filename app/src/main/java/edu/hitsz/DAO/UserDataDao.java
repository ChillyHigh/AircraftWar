package edu.hitsz.DAO;

import java.util.List;

public interface UserDataDao {
    List<UserData> getAllUserData();

    void saveUserData();

    void addUserData(UserData userData);

    void deleteUserData(int id);
}
