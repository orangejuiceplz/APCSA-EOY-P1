**
 * Retrieve accurate location from GPS or network services. 
 * 
 *
 * Class usage example:
 * 
 * public void onCreate(Bundle savedInstanceState) {
 *      ...
 *      my_location = new MyLocation();
 *      my_location.init(main.this, locationResult);
 * }
 * 
 * 
 * public LocationResult locationResult = new LocationResult(){
 *      @Override
 *      public void gotLocation(final Location location){
 *          // do something
 *          location.getLongitude();
 *          location.getLatitude();
 *      }
 *  };
 */
class MyLocation{

    /**
     * If GPS is enabled. 
     * Use minimal connected satellites count.
     */
    private static final int min_gps_sat_count = 5;

    /**
     * Iteration step time.
     */
    private static final int iteration_timeout_step = 500;

    LocationResult locationResult;
    private Location bestLocation = null;
    private Handler handler = new Handler();
    private LocationManager myLocationManager; 
    public Context context;

    private boolean gps_enabled = false;

    private int counts    = 0;
    private int sat_count = 0;

    private Runnable showTime = new Runnable() {
    
         public void run() {
            boolean stop = false;
            counts++;
            System.println("counts=" + counts);
            
            //if timeout (1 min) exceeded, stop tying
            if(counts > 120){
                stop = true;
            }
            
            //update last best location
            bestLocation = getLocation(context);
            
            //if location is not ready or don`t exists, try again
            if(bestLocation == null && gps_enabled){
                System.println("BestLocation not ready, continue to wait");
                handler.postDelayed(this, iteration_timeout_step);
            }else{
                //if best location is known, calculate if we need to continue to look for better location
                //if gps is enabled and min satellites count has not been connected or min check count is smaller then 4 (2 sec)  
                if(stop == false && !needToStop()){
                    System.println("Connected " + sat_count + " sattelites. continue waiting..");
                    handler.postDelayed(this, iteration_timeout_step);
                }else{
                    System.println("#########################################");
                    System.println("BestLocation found return result to main. sat_count=" + sat_count);
                    System.println("#########################################");

                    // removing all updates and listeners
                    myLocationManager.removeUpdates(gpsLocationListener);
                    myLocationManager.removeUpdates(networkLocationListener);    
                    myLocationManager.removeGpsStatusListener(gpsStatusListener);
                    sat_count = 0;
                    
                    // send best location to locationResult
                    locationResult.gotLocation(bestLocation);
                }
            }
         }
    };