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

    private static final String CREATE_USER_SQL = """
            INSERT INTO myusers (firstname, lastname, age)
            VALUES (?, ?, ?);
            """;
    private static final String UPDATE_USER_SQL = """
            UPDATE myusers 
            SET firstname = ?, lastname = ?, age = ? 
            WHERE id = ?;
            """;
    private static final String DELETE_USER = """
            DELETE FROM myusers 
            WHERE id = ?;
            """;
    private static final String FIND_USER_BY_ID_SQL = """
            SELECT id, firstname, lastname, age
            FROM myusers
            WHERE id = ?;
            """;
    private static final String FIND_USER_BY_NAME_SQL = """
            SELECT id, firstname, lastname, age
            FROM myusers
            WHERE firstname = ?;
            """;
    private static final String FIND_ALL_USER_SQL = """
            SELECT id, firstname, lastname, age
            FROM myusers
            """;
    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    public Long createUser(User user) {
        try (Connection con = CustomDataSource.getInstance().getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setInt(3, user.getAge());

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            Long userId = null;
            if (generatedKeys.next()) {
                userId = generatedKeys.getLong("id");
                user.setId(userId);
            }
            return userId;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUserById(Long userId) {
        try (Connection con = CustomDataSource.getInstance().getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(FIND_USER_BY_ID_SQL)) {
            preparedStatement.setLong(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
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
        try (Connection con = CustomDataSource.getInstance().getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(FIND_USER_BY_NAME_SQL)) {
            preparedStatement.setString(1, userName);

            ResultSet resultSet = preparedStatement.executeQuery();
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
        List<User> users = new ArrayList<>();
        try (Connection con = CustomDataSource.getInstance().getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(FIND_ALL_USER_SQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User updateUser(User user) {
        try (Connection con = CustomDataSource.getInstance().getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(UPDATE_USER_SQL)) {
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setInt(3, user.getAge());
            preparedStatement.setLong(4, user.getId());

            User updatedUser = null;
            int updatedRows = preparedStatement.executeUpdate();
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