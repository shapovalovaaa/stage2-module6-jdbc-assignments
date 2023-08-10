package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {

    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final String CREATE_USER_SQL = "INSERT INTO myusers (firstname, lastname, age) VALUES (?, ?, ?);";
    private static final String UPDATE_USER_SQL = "UPDATE myusers SET firstname= ? , lastname = ?, age = ? WHERE id = ?;";
    private static final String DELETE_USER = "DELETE FROM myusers WHERE id = ?;";
    private static final String FIND_USER_BY_ID_SQL = " SELECT * FROM myusers WHERE id = ?;";
    private static final String FIND_USER_BY_NAME_SQL = "SELECT * FROM myusers WHERE firstname = ?;";
    private static final String FIND_ALL_USER_SQL = "SELECT * FROM myusers;";

    public Long createUser(User user) {
        try (Connection connection = CustomDataSource.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setInt(3, user.getAge());
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            Long id = null;
            if (generatedKeys.next()) {
                id = generatedKeys.getLong("id");
                user.setId(id);
            }
            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserById(Long userId) {
        try (Connection connection = CustomDataSource.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_USER_BY_ID_SQL)) {
            ps.setLong(1, userId);
            ResultSet resultSet = ps.executeQuery();
            User user = null;
            if (resultSet.next()) {
                user = buildUser(resultSet);
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserByName(String userName) {
        try (Connection connection = CustomDataSource.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_USER_BY_NAME_SQL)) {
            ps.setString(1, userName);
            ResultSet resultSet = ps.executeQuery();
            User user = null;
            if (resultSet.next()) {
                user = buildUser(resultSet);
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAllUser() {
        List<User> userList = new ArrayList<>();
        try (Connection connection = CustomDataSource.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_ALL_USER_SQL)) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                userList.add(buildUser(resultSet));
            }
            return userList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User updateUser(User user) {
        try (Connection connection = CustomDataSource.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_USER_SQL)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setInt(3, user.getAge());
            ps.setLong(4, user.getId());
            User updatedUser = null;
            int updatedRows = ps.executeUpdate();
            if (updatedRows == 1) {
                updatedUser = findUserById(user.getId());
            }
            return updatedUser;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(Long userId) {
        try (Connection con = CustomDataSource.getInstance().getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(DELETE_USER)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User buildUser(ResultSet resultSet) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .firstName(resultSet.getString("firstname"))
                .lastName(resultSet.getString("lastname"))
                .age(resultSet.getInt("age"))
                .build();
    }
}