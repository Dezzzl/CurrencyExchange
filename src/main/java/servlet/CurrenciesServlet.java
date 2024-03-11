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
import java.util.List;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Currency> allCurrencies = currencyService.findAll();
            objectMapper.writeValue(resp.getWriter(), allCurrencies);
        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        if (name==null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "the name of the currency is not specified"));
            return;
        }
        if (code==null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "the currency code is not specified"));
            return;
        }
        if (sign==null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "the currency sign is not specified"
            ));
            return;
        }
        try {
            Currency currency = new Currency(code, name, sign);
            Optional<Currency> optionalCurrency = currencyService.save(currency);
            if (optionalCurrency.isEmpty()){
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(), new ResponseError(
                        HttpServletResponse.SC_CONFLICT,
                        "A currency with this code already exists"
                ));
            }
            else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(resp.getWriter(), optionalCurrency.get());
            }
        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ResponseError(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable"
            ));
        }
    }
}
