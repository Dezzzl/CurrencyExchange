package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeDto;
import entity.error.ResponseError;
import exception.SameCurrencyExchangeException;
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

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    ExchangeService exchangeService = ExchangeService.getInstance();
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String exchangeAmount = req.getParameter("amount");
        if (baseCurrencyCode == null){
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "Missing form field from"
            ));
            return;
        }
        if (targetCurrencyCode == null){
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "Missing form field to"
            ));
            return;
        }
        if (exchangeAmount == null){
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "Missing form field amount"
            ));
            return;
        }
        BigDecimal amount;
        try{
             amount = BigDecimal.valueOf(Double.parseDouble(exchangeAmount));
        }
        catch(NumberFormatException e){
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "Field amount must be a number"
            ));
            return;
        }
        if (amount.doubleValue()<=0){
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "amount must be positive"
            ));
            return;
        }
        try {
            Optional<ExchangeDto> currencyExchangeOptional = exchangeService.getCurrencyExchange(baseCurrencyCode, targetCurrencyCode, amount);
            if (currencyExchangeOptional.isPresent()){
                objectMapper.writeValue(resp.getWriter(), currencyExchangeOptional.get());
            }
            else{
                objectMapper.writeValue(resp.getWriter(), new ResponseError(
                        SC_NOT_FOUND,
                        "Exchange isn't found"
                ));
            }
        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_INTERNAL_SERVER_ERROR,
                    "Database is unavailable"
            ));
        } catch (SameCurrencyExchangeException e) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "Identical currencies"
            ));
        }


    }
}
