package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.ExchangeRate;
import entity.error.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeService exchangeService = ExchangeService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        String codes = requestURI.replace("/exchangeRate/", "");
        if (codes.length() != 6) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "The currency codes of the pair are missing in the address"));
            return;
        }
        String baseCode = codes.substring(0, 3);
        String targetCode = codes.substring(3);
        try {
            Optional<ExchangeRate> exchangeRate = exchangeService.findByCodes(baseCode, targetCode);
            if (exchangeRate.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new ResponseError(
                        SC_NOT_FOUND,
                        "the codes have not been transmitted"));
            } else {
                resp.setStatus(SC_OK);
                objectMapper.writeValue(resp.getWriter(), exchangeRate.get());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!method.equals("PATCH")) {
            super.service(req, resp);
        }
        this.doPatch(req, resp);
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String rate = req.getParameter("rate");
        String requestURI = req.getRequestURI();
        String codes = requestURI.replace("/exchangeRate/", "");
        if (rate == null) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "Missing form field rate"
            ));
            return;
        }
        if (codes.length() != 6) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "The currency codes of the pair are missing in the address"
            ));
            return;
        }
        String baseCode = codes.substring(0, 3);
        String targetCode = codes.substring(3);
        BigDecimal exchangeRate;
        try {
            exchangeRate = new BigDecimal(rate);
        } catch (NumberFormatException e) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "Incorrect format of rate"
            ));
            return;
        }
        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeService.findByCodes(baseCode, targetCode);
            if (exchangeRateOptional.isEmpty()) {
                objectMapper.writeValue(resp.getWriter(), new ResponseError(
                        SC_NOT_FOUND,
                        "Currency does not exist in the database"
                ));
            } else {
                ExchangeRate updatedExchangeRate = exchangeRateOptional.get();
                updatedExchangeRate.setRate(exchangeRate);
                exchangeService.update(exchangeRateOptional.get());
                objectMapper.writeValue(resp.getWriter(), updatedExchangeRate);
            }
        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database unavailable"
            ));
        }
    }
}
