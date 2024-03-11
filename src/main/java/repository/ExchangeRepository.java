package repository;

import entity.Currency;
import entity.ExchangeRate;
import lombok.NoArgsConstructor;
import util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ExchangeRepository implements CrudRepository<Integer, ExchangeRate> {
    private static final ExchangeRepository INSTANCE = new ExchangeRepository();

    public static ExchangeRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public List<ExchangeRate> findAll() throws SQLException {
        String query = """
                SELECT
                er.id AS id,
                er.base_currency_id AS base_id,
                er.target_currency_id AS  target_id,
                er.rate AS rate,
                c1.id AS base_id,
                c1.code AS base_code,
                c1.full_name AS base_full_name,
                c1.sign AS base_sign,
                c2.id AS target_id,
                c2.code AS target_code,
                c2.full_name AS target_full_name,
                c2.sign AS target_sign
                FROM exchange_rates er
                JOIN currencies c1 on er.base_currency_id = c1.id
                JOIN currencies c2 on er.target_currency_id =  c2.id
                """;
        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        }
    }

    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        return ExchangeRate.builder()
                .id(resultSet.getInt("id"))
                .rate(resultSet.getBigDecimal("rate"))
                .baseCurrency(new Currency(
                        resultSet.getInt("base_id"),
                        resultSet.getString("base_code"),
                        resultSet.getString("base_full_name"),
                        resultSet.getString("base_sign")
                ))
                .targetCurrency(new Currency(
                        resultSet.getInt("target_id"),
                        resultSet.getString("target_code"),
                        resultSet.getString("target_full_name"),
                        resultSet.getString("target_sign")
                ))
                .build();
    }

    @Override
    public Optional<ExchangeRate> findById(Integer id) throws SQLException {
        String query = """
                 SELECT
                er.id AS id,
                er.base_currency_id AS base_id,
                er.target_currency_id AS  target_id,
                er.rate AS rate,
                c1.id AS base_id,
                c1.code AS base_code,
                c1.full_name AS base_full_name,
                c1.sign AS base_sign,
                c2.id AS target_id,
                c2.code AS target_code,
                c2.full_name AS target_full_name,
                c2.sign AS target_sign
                FROM exchange_rates er
                JOIN currencies c1 on er.base_currency_id = c1.id
                JOIN currencies c2 on er.target_currency_id =  c2.id
                WHERE c1.id = ?;
                """;
        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            ExchangeRate exchangeRate = null;
            if (resultSet.next()) {
                exchangeRate = buildExchangeRate(resultSet);
            }
            return Optional.ofNullable(exchangeRate);
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        return false;
    }

    @Override
    public void update(ExchangeRate entity) throws SQLException {
        String query = """
                UPDATE exchange_rates
                SET rate = ?
                WHERE id = ?;
                """;
        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, entity.getRate());
            preparedStatement.setInt(2, entity.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public Optional<ExchangeRate> save(ExchangeRate entity) throws SQLException {
        String query = """
                    INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES (?,?,?);
                """;
        try (Connection connection = ConnectionManager.open();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, entity.getBaseCurrency().getId());
            preparedStatement.setInt(2, entity.getTargetCurrency().getId());
            preparedStatement.setBigDecimal(3, entity.getRate());

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int exchangeId = generatedKeys.getInt(1);
                entity.setId(exchangeId);
                return Optional.of(entity);
            }
            return Optional.empty();
        }

    }

    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) throws SQLException {
        String query = """
                    SELECT
                er.id AS id,
                er.base_currency_id AS base_id,
                er.target_currency_id AS  target_id,
                er.rate AS rate,
                c1.id AS base_id,
                c1.code AS base_code,
                c1.full_name AS base_full_name,
                c1.sign AS base_sign,
                c2.id AS target_id,
                c2.code AS target_code,
                c2.full_name AS target_full_name,
                c2.sign AS target_sign
                FROM exchange_rates er
                JOIN currencies c1 on er.base_currency_id = c1.id
                JOIN currencies c2 on er.target_currency_id =  c2.id
                WHERE base_code = ? AND target_code = ?
                """;
        try (Connection connection = ConnectionManager.open()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);

            ResultSet resultSet = preparedStatement.executeQuery();

            ExchangeRate exchangeRate = null;
            if (resultSet.next()) {
                exchangeRate = buildExchangeRate(resultSet);
            }
            return Optional.ofNullable(exchangeRate);
        }
    }
}
