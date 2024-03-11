package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Currency;
import entity.error.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String requestURI = req.getRequestURI();
        String code = requestURI.replace("/currency/", "");
        if (code.isEmpty()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_BAD_REQUEST,
                    "The currency code was not transmitted"));
            return;
        }
        try {
            Optional<Currency> currencyOptional = currencyService.findByCode(code);
            if(currencyOptional.isEmpty()){
                objectMapper.writeValue(resp.getWriter(), new ResponseError(
                        SC_NOT_FOUND,
                        "The currency was not found"
                ));
            }
            else {
                objectMapper.writeValue(resp.getWriter(), currencyOptional.get());
            }
        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_INTERNAL_SERVER_ERROR,
                    "the database is unavailable"
            ));
        }
    }
}
