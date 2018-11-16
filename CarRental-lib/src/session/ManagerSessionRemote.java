package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
    
    public int getNumberOfReservationsBy(String renter);
    
    public Set<String> getBestClients();
    
    public CarType getMostPopularCarType(String company, int year);
    
    public void addCompany(String name, List<String> regions);
    
    public void addCarType(String name, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed);
    
    public void addCar(String companyName, int id, String carTypeName);
}