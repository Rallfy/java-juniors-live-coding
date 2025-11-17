package com.bvd.java_fundamentals;

import com.bvd.java_fundamentals.model.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/*
 * Implement the methods below so that the requirements are met.
 */
public class OrderUtil {

    private OrderUtil() {
    }

    // retrieve orders from csv lines
    public static List<Order> parseCsvLines(final List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }

        List<Order> result = new ArrayList<>();
// VERSION 1:
//        for (String line : lines) {
//            if (line == null || line.isBlank())
//                continue;
//
//            String[] parts = line.split(",");
//            if (parts.length != 7)
//                continue;
//
//            try {
//                String orderId = parts[0].trim();
//                String customerId = parts[1].trim();
//                LocalDate date = LocalDate.parse(parts[2].trim());
//                String product = parts[3].trim();
//                String category = parts[4].trim();
//                BigDecimal unitPrice = new BigDecimal(parts[5].trim());
//                int quantity = Integer.parseInt(parts[6].trim());
//
//                result.add(new Order(orderId, customerId, date, product, category, unitPrice, quantity));
//            } catch (Exception ignored) {
//                break;
//            }
//        }
//        return result;

        // VERSION 2: with streams
        return lines.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> line.split(","))
                .filter(parts -> parts.length == 7)
                .map(parts -> {
                    try {
                        String orderId = parts[0].trim();
                        String customerId = parts[1].trim();
                        LocalDate date = LocalDate.parse(parts[2].trim());
                        String product = parts[3].trim();
                        String category = parts[4].trim();
                        BigDecimal unitPrice = new BigDecimal(parts[5].trim());
                        int quantity = Integer.parseInt(parts[6].trim());

                        return new Order(orderId, customerId, date, product, category, unitPrice, quantity);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


    }

    // calculate revenue by day
    // revenue = unitPrice * quantity
    public static Map<LocalDate, BigDecimal> revenueByDay(final List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getOrderDate,
                        Collectors.mapping(o -> o.getUnitPrice().multiply(BigDecimal.valueOf(o.getQuantity())), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))
                );
    }

    // get top "n" products by revenue
    public static List<Map.Entry<String, BigDecimal>> topProductsByRevenue(final List<Order> orders, final int n) {
        if (orders == null || orders.isEmpty() || n <=0 )
            return Collections.emptyList();

        Map<String, BigDecimal> revenueByProduct = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getProductName,
                        Collectors.mapping(o -> o.getUnitPrice().multiply(BigDecimal.valueOf(o.getQuantity())), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return revenueByProduct.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    // get customers who ordered products from at least "minCategories" different categories
    public static List<String> customersWithCategoryDiversity(final List<Order> orders, final int minCategories) {
        if (orders == null || orders.isEmpty() || minCategories <=0 )
            return Collections.emptyList();


        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getCustomerId,
                        Collectors.mapping(Order::getCategory, Collectors.toSet())
                ))
                .entrySet().stream()
                .filter(e -> e.getValue().size() >= minCategories)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    // find the first product containing a given substring (case-insensitive)
    public static Optional<Order> findFirstProductContaining(final List<Order> orders, final String product) {
        if (orders == null || orders.isEmpty() || product == null || product.isBlank())
            return Optional.empty();

        String lowerCaseProduct = product.toLowerCase();

        return orders.stream()
                .filter(o -> o.getProductName().toLowerCase().contains(lowerCaseProduct))
                .findFirst();

    }
}
