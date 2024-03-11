package service;

import entity.Currency;
import entity.ExchangeRate;
import exception.SameCurrencyExchangeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import repository.ExchangeRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ExchangeService {
    private static final ExchangeService INSTANCE = new ExchangeService();

    private final ExchangeRepository exchangeRepository = ExchangeRepository.getInstance();

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    public List<ExchangeRate> findAll() throws SQLException {
        return exchangeRepository.findAll();
    }

    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) throws SQLException {
        return exchangeRepository.findByCodes(baseCode, targetCode);
    }

    public Optional<ExchangeRate> save(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) throws SQLException, SameCurrencyExchangeException {
        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(baseCurrency.getCode(), targetCurrency.getCode());
        if (exchangeRateOptional.isPresent()) {
            return Optional.empty();
        }
        if (Objects.equals(baseCurrency.getId(), targetCurrency.getId())) throw new SameCurrencyExchangeException();
        return exchangeRepository.save(new ExchangeRate(0, baseCurrency, targetCurrency, rate));
    }

    public void update(ExchangeRate exchangeRate) throws SQLException {
        exchangeRepository.update(exchangeRate);
    }
}
