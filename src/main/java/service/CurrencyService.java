package service;

import entity.Currency;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import repository.CurrencyRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CurrencyService {
    private static final CurrencyService INSTANCE = new CurrencyService();

    private final CurrencyRepository currencyRepository = CurrencyRepository.getInstance();

    public static CurrencyService getInstance() {
        return INSTANCE;
    }

    public List<Currency> findAll() throws SQLException {
        return currencyRepository.findAll();
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        return currencyRepository.findByCode(code);
    }

    public Optional<Currency> save(Currency currency) throws SQLException {
        return currencyRepository.save(currency);
    }
}
