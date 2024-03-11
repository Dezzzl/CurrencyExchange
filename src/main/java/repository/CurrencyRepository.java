package repository;

import entity.Currency;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CurrencyRepository implements CrudRepository<Integer, Currency> {
    private static final CurrencyRepository INSTANCE = new CurrencyRepository();

    public static CurrencyRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Currency> findAll() throws SQLException {
        String query = "SELECT * FROM currencies";
        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while (resultSet.next()) {
                currencies.add(buildCurrency(resultSet));
            }
            return currencies;
        }
    }

    @Override
    public Optional<Currency> findById(Integer id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        return false;
    }

    @Override
    public void update(Currency entity) throws SQLException {

    }

    @Override
    public Optional<Currency> save(Currency entity) throws SQLException {
        String query ="INSERT INTO currencies (code, full_name, sign) VALUES(?,?,?)";

        try (Connection connection = ConnectionManager.open();
         PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
//            connection.setAutoCommit(false);
            preparedStatement.setString(1, entity.getCode());
            preparedStatement.setString(2, entity.getFullName());
            preparedStatement.setString(3, entity.getSign());

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()){
               entity.setId(generatedKeys.getInt(1));
//                connection.commit();
               return Optional.of(entity);
            }
//            connection.commit();
            return Optional.empty();
        }
    }

    private Currency buildCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getObject("id", Integer.class),
                resultSet.getObject("code", String.class),
                resultSet.getObject("full_name", String.class),
                resultSet.getObject("sign", String.class)
        );
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        String query = "SELECT * FROM currencies WHERE code = ?";
        try (Connection connection = ConnectionManager.open()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            Currency currency = null;
            if(resultSet.next()){
               currency =  buildCurrency(resultSet);
            }
            return Optional.ofNullable(currency);
        }
    }
}
