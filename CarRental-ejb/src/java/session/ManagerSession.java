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
        return new HashSet<>(em.createQuery("SELECT c.carTypes FROM CarRentalCompany c WHERE c.name LIKE :company")
                    .setParameter("company", company)
                    .getResultList());
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        return new HashSet<>(em.createQuery("SELECT c.cars FROM CarRentalCompany c WHERE c.name LIKE :company AND c.cars.type LIKE :type")
                    .setParameter("company", company)
                    .setParameter("type", type)
                    .getResultList());
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        return em.createQuery("SELECT c.cars FROM CarRentalCompany c WHERE c.name LIKE :company AND c.cars.id LIKE :id")
                    .setParameter("company", company)
                    .setParameter("id", id)
                    .getResultList().size();
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        return em.createQuery("SELECT COUNT(c.cars) FROM CarRentalCompany c WHERE c.name LIKE :company AND c.cars.type LIKE :type")
            .setParameter("company", company)
            .setParameter("type", type).getFirstResult();
    }
    
    @Override
    public Set<String> getBestClients() {
        return new HashSet<>(em.createQuery("SELECT r.carRenter FROM Reservation r GROUP BY r.carRenter ORDER BY count(r) DESC LIMIT 1")
            .getResultList());
    }
    
    @Override
    public CarType getMostPopularCarType(String company, int year) {
        return em.createQuery("SELECT r.type FROM Reservation r GROUP BY r.type ORDER BY count(r.type) DESC LIMIT 1 ")
    }
    
    @Override
    public void addCompany(String name, List<String> regions) {
        CarRentalCompany company = new CarRentalCompany(name, regions, new ArrayList<Car>());
        em.persist(company);
    }
    
    @Override
    public void addCarType(String name, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed) {
        CarType carType = new CarType(name, nbOfSeats, trunkSpace, rentalPricePerDay, smokingAllowed);
        em.persist(carType);
    }
    
    @Override
    public void addCar(String companyName, int uid, CarType type) {
        Car car = new Car(uid, type);
        CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
        company.addCar(car);
        em.persist(car);
    }

}