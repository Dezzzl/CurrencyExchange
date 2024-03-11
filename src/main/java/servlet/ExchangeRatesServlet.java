package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Currency;
import entity.ExchangeRate;
import entity.error.ResponseError;
import exception.SameCurrencyExchangeException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import service.ExchangeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeService exchangeService = ExchangeService.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setStatus(SC_OK);
            List<ExchangeRate> exchangeRates = exchangeService.findAll();
            objectMapper.writeValue(resp.getWriter(), exchangeRates);
        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "the database is unavailable"
            ));
        }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");
        if (baseCurrencyCode == null) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing form field baseCurrencyCode"
            ));
            return;
        }
        if (targetCurrencyCode == null) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing form field targetCurrencyCode"
            ));
            return;
        }
        if (rate == null) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing form field rate."
            ));
            return;
        }
        BigDecimal exchangeRate;
        try{
            exchangeRate = new BigDecimal(rate);
        }
        catch (NumberFormatException e){
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrect format of rate"
            ));
            return;
        }
        try {
            Optional<Currency> baseCurrencyOptional = currencyService.findByCode(baseCurrencyCode);
            Optional<Currency> targetCurrencyOptional = currencyService.findByCode(targetCurrencyCode);
            if (baseCurrencyOptional.isEmpty() || targetCurrencyOptional.isEmpty()) {
                objectMapper.writeValue(resp.getWriter(), new ResponseError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Currency does not exist in the database"
                ));
            } else {
                Optional<ExchangeRate> save = exchangeService.save(baseCurrencyOptional.get(), targetCurrencyOptional.get(), exchangeRate);
                if (save.isEmpty()) {
                    objectMapper.writeValue(resp.getWriter(), new ResponseError(
                            HttpServletResponse.SC_CONFLICT,
                            "Currency pair exists"
                    ));
                } else {
                    resp.setStatus(SC_OK);
                    objectMapper.writeValue(resp.getWriter(), save.get());
                }
            }
        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database unavailable"
            ));
        }
        catch (SameCurrencyExchangeException e){
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Identical currencies"
            ));
        }
    }



}
