package session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        return new HashSet<>(em.createQuery("SELECT DISTINCT company.carTypes FROM CarRentalCompany company WHERE company.name = :company")
                    .setParameter("company", company)
                    .getResultList());
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<>();
        try {
            CarRentalCompany carRentalCompany = em.find(CarRentalCompany.class, company);
            for(Car c: carRentalCompany.getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        return em.createQuery("SELECT r.id FROM Reservation r WHERE r.rentalCompany = :company AND r.carType = :type AND id = :id")
            .setParameter("company", company)
            .setParameter("id", id)
            .setParameter("type", type).getResultList().size();
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        return em.createQuery("SELECT r.id FROM Reservation r WHERE r.rentalCompany = :company AND r.carType = :type")
            .setParameter("company", company)
            .setParameter("type", type).getResultList().size();
    }
    
    @Override
    public int getNumberOfReservationsBy(String renter) {
        return em.createQuery("SELECT r.id FROM Reservation r WHERE r.carRenter = :renter")
            .setParameter("retner", renter).getResultList().size();
    }   
    
    @Override
    public Set<String> getBestClients() {
        List<Object[]> res =
            em.createQuery("SELECT r.carRenter, COUNT(r.id) FROM Reservation r GROUP BY r.carRenter ORDER BY COUNT(r.id) DESC")
            .getResultList();
        Set<String> bestClients = new HashSet<>();
        long max = 0;
        for (Object[] o : res) {
            String client = (String) o[0];
            long current = (Long) o[1];
            if(current >= max) {
                bestClients.add(client);
                max = current;
            }
        }     
        return bestClients;
    }
    
    @Override
    public CarType getMostPopularCarType(String company, int year) {
        String carTypeName = 
            (String) em.createQuery("SELECT r.carType FROM Reservation r WHERE r.rentalCompany = :company AND FUNCTION('YEAR', r.startDate) = :year GROUP BY r.carType ORDER BY COUNT(r.id) DESC")
                .setParameter("company", company)
                .setParameter("year", year)
                .getResultList().get(0);
        return em.find(CarType.class, carTypeName);
    }
    
    @Override
    public void addCompany(String name, List<String> regions) {
        CarRentalCompany company = new CarRentalCompany(name, regions, new ArrayList<Car>());
        em.persist(company);
    }
    
    @Override
    public void addCarType(String name, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed) {
        if (em.find(CarType.class, name) == null) {
            CarType carType = new CarType(name, nbOfSeats, trunkSpace, rentalPricePerDay, smokingAllowed);
            em.persist(carType);
        }
    }
    
    @Override
    public void addCar(String companyName, int id, String carTypeName) {
        if (em.find(Car.class, id) == null) {
            CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
            CarType carType = em.find(CarType.class, carTypeName);
            Car car = new Car(id, carType);
            car.setType(carType);
            company.addCar(car);
        }
    }

}