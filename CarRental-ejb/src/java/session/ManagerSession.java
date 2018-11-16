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
        return new HashSet<>(em.createQuery("SELECT company.cars.id FROM CarRentalCompany company WHERE c.name = :company AND company.cars.type = :type")
                    .setParameter("company", company)
                    .setParameter("type", type)
                    .getResultList());
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        return em.createQuery("SELECT company.cars FROM CarRentalCompany company WHERE company.name = :company AND company.cars.id = :id")
                    .setParameter("company", company)
                    .setParameter("id", id)
                    .getResultList().size();
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        return 0;
        //return em.createQuery("SELECT COUNT(company.cars) FROM CarRentalCompany company WHERE company.name = :company AND company.cars.type = :type")
        //    .setParameter("company", company)
        //    .setParameter("type", type).getFirstResult();
    }
    
    @Override
    public int getNumberOfReservationsBy(String renter) {
        return em.createQuery("SELECT COUNT(reservation) FROM Reservation reservation WHERE reservation.carRenter = :renter")
            .setParameter("renter", renter).getFirstResult();
    }   
    
    @Override
    public Set<String> getBestClients() {
        return new HashSet<>(em.createQuery("SELECT reservation.carRenter FROM Reservation reservation GROUP BY reservation.carRenter ORDER BY count(reservation) DESC LIMIT 1")
            .getResultList());
    }
    
    @Override
    public CarType getMostPopularCarType(String company, int year) {
        //return em.createQuery("SELECT r.type FROM Reservation r GROUP BY r.type ORDER BY count(r.type) DESC LIMIT 1 ")
        return null;
    }
    
    @Override
    public synchronized void addCompany(String name, List<String> regions) {
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
            company.addCar(car);
        }
    }

}