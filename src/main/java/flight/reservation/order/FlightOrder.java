package flight.reservation.order;

import flight.reservation.Customer;
import flight.reservation.flight.ScheduledFlight;
import flight.reservation.payment.Payment;
import flight.reservation.payment.CreditCard;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FlightOrder extends Order {
    private final List<ScheduledFlight> flights;
    static List<String> noFlyList = Arrays.asList("Peter", "Johannes");

    public FlightOrder(List<ScheduledFlight> flights) {
        this.flights = flights;
    }

    public static List<String> getNoFlyList() {
        return noFlyList;
    }

    public List<ScheduledFlight> getScheduledFlights() {
        return flights;
    }

    public static boolean isOrderValid(Customer customer, List<String> passengerNames, List<ScheduledFlight> flights) {
        boolean valid = true;
        valid = valid && !noFlyList.contains(customer.getName());
        valid = valid && passengerNames.stream().noneMatch(passenger -> noFlyList.contains(passenger));
        valid = valid && flights.stream().allMatch(scheduledFlight -> {
            try {
                return scheduledFlight.getAvailableCapacity() >= passengerNames.size();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            }
        });
        return valid;
    }

    @Deprecated
    public boolean processOrderWithCreditCardDetail(String number, Date expirationDate, String cvv) throws IllegalStateException {
        CreditCard creditCard = new CreditCard(number, expirationDate, cvv);
        return processOrderWithCreditCard(creditCard);
    }

    public boolean processOrderWithCreditCard(CreditCard creditCard) throws IllegalStateException {
        return processOrder(() -> Payment.payWithCreditCard(creditCard, this.getPrice()));
    }

    public boolean processOrderWithPayPal(String email, String password) throws IllegalStateException {
        return processOrder(() -> Payment.payWithPayPal(email, password, this.getPrice()));
    }

    private boolean processOrder(ProcessOrder processOrder) {
        if (isClosed()) {
            // Payment is already proceeded
            return true;
        }
        boolean isPaid = processOrder.execute();
        if (isPaid) {
            this.setClosed();
        }
        return isPaid;
    }

    private interface ProcessOrder {
        boolean execute();
    }
}
