package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    @PersistenceContext
    EntityManager em;
    
    private String renter;
    private List<Quote> quotes = new LinkedList<>();

    @Override
    public Set<String> getAllRentalCompanies() {
        return new HashSet<>(em.createQuery("SELECT c.name FROM CarRentalCompany c")
                    .getResultList());
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarType> availableCarTypes = new LinkedList<>();
        for(String company : getAllRentalCompanies()) {
            CarRentalCompany carRentalCompany = em.find(CarRentalCompany.class, company);
            for(CarType ct : carRentalCompany.getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            }
        }
        return availableCarTypes;
    }
    
    @Override 
    public String getCheapestCarType(Date start, Date end, String region) {
        // TODO: filter on region
        return (String) em.createQuery("SELECT DISTINCT ct.name FROM Car car, CarRentalCompany c, CarType ct WHERE car.type = ct AND car.id != ANY (SELECT r.carId FROM Reservation r WHERE (r.startDate > :start AND r.startDate < :end) OR (r.endDate > :start AND r.endDate < :end)) ORDER BY ct.rentalPricePerDay")
        .setParameter("start", start)
        .setParameter("end", end)
        //.setParameter("region", region)
                .getResultList().get(0);
    }

    @Override
    public synchronized Quote createQuote(String client, ReservationConstraints constraints) throws Exception {
        Set<String> companies = getAllRentalCompanies();
        Exception exception = null;
        for(String company : companies) {
            try {
                CarRentalCompany carRentalCompany = em.find(CarRentalCompany.class, company);
                Quote out = carRentalCompany.createQuote(constraints, client);
                quotes.add(out);
                return out;
            } catch(IllegalArgumentException | ReservationException e) {
                exception = e;
            }           
        }
        throw new ReservationException("No available cars");
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public synchronized List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompany carRentalCompany = em.find(CarRentalCompany.class, quote.getRentalCompany());
                done.add(carRentalCompany.confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done) {
                CarRentalCompany carRentalCompany = em.find(CarRentalCompany.class, r.getRentalCompany());
                carRentalCompany.cancelReservation(r);
            }
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
}