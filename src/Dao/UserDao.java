package Dao;

import Model.User;
import Model.Outlet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private Connection connection;

    public UserDao() {

        connection = KoneksiDatabase.getConnection();

    }

    public boolean insertUser(User user) {

        String sql = "INSERT INTO users (name, username, email, password, role, phone, address, id_outlet) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getAddress());

            if (user.getIdOutlet() > 0) {

                stmt.setInt(8, user.getIdOutlet());

            } else {

                stmt.setNull(8, Types.INTEGER);

            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        user.setId(generatedKeys.getInt(1));

                    }

                }

                return true;

            }

        } catch (SQLException e) {

            System.err.println("Error insert user: " + e.getMessage());

        }

        return false;

    }

    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, o.nama as outlet_nama FROM users u LEFT JOIN outlet o ON u.id_outlet = o.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                User user = resultSetToUser(rs);
                users.add(user);

            }

        } catch (SQLException e) {

            System.err.println("Error get all users: " + e.getMessage());

        }

        return users;

    }

    public User getUserById(int id) {

        String sql = "SELECT u.*, o.nama as outlet_nama FROM users u LEFT JOIN outlet o ON u.id_outlet = o.id WHERE u.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToUser(rs);
                }

            }

        } catch (SQLException e) {

            System.err.println("Error get user by id: " + e.getMessage());

        }

        return null;

    }

    public User getUserByUsername(String username) {

        String sql = "SELECT u.*, o.nama as outlet_nama FROM users u LEFT JOIN outlet o ON u.id_outlet = o.id WHERE u.username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToUser(rs);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get user by username: " + e.getMessage());

        }

        return null;

    }

    public List<User> getUsersByOutlet(int outletId) {

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, o.nama as outlet_nama FROM users u LEFT JOIN outlet o ON u.id_outlet = o.id WHERE u.id_outlet = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, outletId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    User user = resultSetToUser(rs);
                    users.add(user);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get users by outlet: " + e.getMessage());

        }

        return users;

    }

    public List<User> getUsersByRole(String role) {

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, o.nama as outlet_nama FROM users u LEFT JOIN outlet o ON u.id_outlet = o.id WHERE u.role = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, role);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    User user = resultSetToUser(rs);
                    users.add(user);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error get users by role: " + e.getMessage());

        }

        return users;

    }

    public boolean updateUser(User user) {

        String sql = "UPDATE users SET name = ?, username = ?, email = ?, password = ?, role = ?, phone = ?, address = ?, id_outlet = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getAddress());

            if (user.getIdOutlet() > 0) {

                stmt.setInt(8, user.getIdOutlet());

            } else {

                stmt.setNull(8, Types.INTEGER);

            }

            stmt.setInt(9, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error update user: " + e.getMessage());

        }

        return false;

    }

    public boolean deleteUser(int id) {

        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println("Error delete user: " + e.getMessage());

        }

        return false;

    }

    public User login(String username, String password) {

        String sql = "SELECT u.*, o.nama as outlet_nama FROM users u LEFT JOIN outlet o ON u.id_outlet = o.id WHERE u.username = ? AND u.password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return resultSetToUser(rs);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error login: " + e.getMessage());

        }

        return null;

    }

    public List<User> searchUsers(String keyword) {

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, o.nama as outlet_nama FROM users u LEFT JOIN outlet o ON u.id_outlet = o.id " +
                "WHERE u.name LIKE ? OR u.username LIKE ? OR u.email LIKE ? OR u.role LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    User user = resultSetToUser(rs);
                    users.add(user);

                }

            }

        } catch (SQLException e) {

            System.err.println("Error search users: " + e.getMessage());

        }

        return users;

    }

    private User resultSetToUser(ResultSet rs) throws SQLException {

        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setIdOutlet(rs.getInt("id_outlet"));
        user.setEmailVerifiedAt(rs.getTimestamp("email_verified_at"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));


        if (rs.getString("outlet_nama") != null) {

            Outlet outlet = new Outlet();
            outlet.setId(rs.getInt("id_outlet"));
            outlet.setNama(rs.getString("outlet_nama"));
            user.setOutlet(outlet);

        }

        return user;

    }

}