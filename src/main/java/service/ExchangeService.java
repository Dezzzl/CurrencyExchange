package service;

import dto.ExchangeDto;
import entity.Currency;
import entity.ExchangeRate;
import exception.SameCurrencyExchangeException;
import lombok.NoArgsConstructor;
import repository.ExchangeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ExchangeService {
    private static final ExchangeService INSTANCE = new ExchangeService();

    private final ExchangeRepository exchangeRepository = ExchangeRepository.getInstance();

    private final CurrencyService currencyService = CurrencyService.getInstance();

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

    public Optional<ExchangeDto> getCurrencyExchange(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException, SameCurrencyExchangeException {
        Optional<BigDecimal> rateOptional;
        rateOptional = getRateWithDirectCourse(baseCurrencyCode, targetCurrencyCode);
        if (rateOptional.isEmpty()) {
            rateOptional = getRateWithReverseCourse(baseCurrencyCode, targetCurrencyCode);
        }
        if (rateOptional.isEmpty()) {
            rateOptional = getRateWithUSD(baseCurrencyCode, targetCurrencyCode);

        }
        if (rateOptional.isEmpty()) {
            return Optional.empty();
        } else {
            Currency baseCurrency = currencyService.findByCode(baseCurrencyCode).get();
            Currency targetCurrency = currencyService.findByCode(targetCurrencyCode).get();
            BigDecimal rate = rateOptional.get();
            Optional<ExchangeRate> exchangeRate;
            BigDecimal convertedAmount = amount.multiply(rate);
            exchangeRate = save(baseCurrency, targetCurrency, rate);
            if (exchangeRate.isEmpty()){
                exchangeRate = findByCodes(baseCurrencyCode, targetCurrencyCode);
            }
            return Optional.of(createExchangeDto(exchangeRate.get(), amount, convertedAmount));
        }

    }

    private Optional<BigDecimal> getRateWithDirectCourse(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> directCourseOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        if (directCourseOptional.isPresent()) {
            ExchangeRate exchangeRate = directCourseOptional.get();
            return Optional.of(exchangeRate.getRate());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> getRateWithReverseCourse(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> reverseCourseOptional = exchangeRepository.findByCodes(targetCurrencyCode, baseCurrencyCode);
        if (reverseCourseOptional.isPresent()) {
            ExchangeRate reverseCourse = reverseCourseOptional.get();
            BigDecimal reverseRate = reverseCourse.getRate();
            BigDecimal rate = BigDecimal.ONE.divide(reverseRate, 2, RoundingMode.HALF_UP);
            return Optional.of(rate);
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> getRateWithUSD(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> baseExchangeOptional = exchangeRepository.findByCodes("USD", baseCurrencyCode);
        Optional<ExchangeRate> targetExchangeOptional = exchangeRepository.findByCodes("USD", targetCurrencyCode);
        BigDecimal baseExchangeRate;
        if (baseExchangeOptional.isPresent()) {
            ExchangeRate baseExchange = baseExchangeOptional.get();
            baseExchangeRate = baseExchange.getRate();
        } else return Optional.empty();
        if (targetExchangeOptional.isPresent()) {
            ExchangeRate targetExchange = targetExchangeOptional.get();
            BigDecimal targetExchangeRate = targetExchange.getRate();
            return Optional.of(targetExchangeRate.divide(baseExchangeRate, 2, RoundingMode.HALF_UP));
        } else {
            return Optional.empty();
        }
    }


    private ExchangeDto createExchangeDto(ExchangeRate exchangeRate, BigDecimal amount, BigDecimal convertedAmount) {
        return ExchangeDto.builder()
                .exchangeRate(exchangeRate)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();
    }


}
