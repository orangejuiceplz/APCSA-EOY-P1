public class main {

LocationResult locationResult = new LocationResult(){
    @Override
    public void gotLocation(Location location){
        //Got the location!
    } 
};
MyLocation myLocation = new MyLocation();
myLocation.getLocation(this, locationResult);
}
