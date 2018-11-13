package session;

import java.util.HashSet;
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

}