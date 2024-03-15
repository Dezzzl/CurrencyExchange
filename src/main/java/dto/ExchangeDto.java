package dto;

import entity.ExchangeRate;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ExchangeDto {
    ExchangeRate exchangeRate;
    BigDecimal amount;
    BigDecimal convertedAmount;
}
