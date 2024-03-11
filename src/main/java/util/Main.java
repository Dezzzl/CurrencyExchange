//package util;
//
//import entity.Currency;
//import repository.CurrencyRepository;
//
//import javax.swing.*;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.List;
//
//public class Main {
//    public static void main(String[] args) throws SQLException {
////        try (Connection connection = ConnectionManager.open()) {
////            System.out.println(connection.getTransactionIsolation());
////        }
//        try (Connection connection = ConnectionManager.get();
////             Connection connection1 = ConnectionManager.get();
////             Connection connection2 = ConnectionManager.get();
////             Connection connection3 = ConnectionManager.get()
//        ) {
//            System.out.println(connection.getTransactionIsolation());
//            CurrencyRepository currencyRepository = CurrencyRepository.getInstance();
//            List<Currency> currencies = currencyRepository.findAll();
//            for (Currency currency : currencies) {
//                System.out.println(currency);
//            }
//        } finally {
//            ConnectionManager.closePool();
//        }
//    }
//}
