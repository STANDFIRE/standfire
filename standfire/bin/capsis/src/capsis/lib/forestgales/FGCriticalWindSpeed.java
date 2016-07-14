package capsis.lib.forestgales;
import capsis.kernel.PathManager;


/**
 * ForestGales Critical Wind speed
 *
 * @author B. Gardiner, T. Labbe, C; Meredieu October 2013
 */
public class FGCriticalWindSpeed  {

	private static final int SECTIONS = 4;/////////////////////////WE CHOSE THIS VALUE FOR COMPUTATION, BARRY?????????????



	public  static double calculateSimpleOverturningCriticalWindSpeed (FGConfiguration configuration, double currentSpacing ,
			double overturningMomentMultiplier, double stemWeight, double gustFactor, double crownFactor, double height, double roughnessSimple_d , double roughnessSimple_z0 ) {

		double airDensity = configuration.getAirDensity ();
		double K = configuration.getVonKarmanConstant ();
		//double currentSpacing = 100. / Math.sqrt (nha);

		System.out.println 	("stemWeight :" +stemWeight);
		System.out.println 	("gustFactor:" +gustFactor);
		System.out.println 	("crownFactor:" +crownFactor);
		System.out.println 	("height:" +height);

		double simpleOverturningCriticalWindSpeed = (1. / (K * currentSpacing))
				* Math.sqrt ((overturningMomentMultiplier * stemWeight) / (airDensity * gustFactor * roughnessSimple_d))
				* Math.sqrt (1. / crownFactor) * Math.log ((height - roughnessSimple_d) / roughnessSimple_z0);


		System.out.println 	("results of overturningCriticalWindSpeed : " + simpleOverturningCriticalWindSpeed );



		return simpleOverturningCriticalWindSpeed;
	}



	public  static double calculateSimpleBreakageCriticalWindSpeed (FGConfiguration configuration, double currentSpacing ,
			double modulusOfRupture, double dbh, double gustFactor, double knotFactor, double crownFactor, double height, double roughnessSimple_d , double roughnessSimple_z0) {

		double airDensity = configuration.getAirDensity ();
		double K = configuration.getVonKarmanConstant ();
		//double currentSpacing = 100. / Math.sqrt (nha);


		double simpleBreakageCriticalWindSpeed = (1. / (K * currentSpacing))
				* Math.sqrt ((Math.PI * modulusOfRupture * Math.pow (dbh, 3))
						/ (32. * airDensity * gustFactor * (roughnessSimple_d - 1.3)))
				* Math.sqrt (knotFactor / crownFactor) * Math.log ((height - roughnessSimple_d) / roughnessSimple_z0);

		System.out.println 	("results of breakageCriticalWindSpeed : " + simpleBreakageCriticalWindSpeed );

		return simpleBreakageCriticalWindSpeed;
	}

	//IS IT A GOOD PLACE FOR THIS METHOD??????????????????????????????????????????????????????????????????????????????????????????????????????????
 	//public void calculateWindProfile( double nha, double height= mean height for stand ){
    public static double [] calculateWindProfile( double currentSpacing, double height, double roughnessSimple_d , double roughnessSimple_z0 ){

		//double currentSpacing = 100. / Math.sqrt (nha);
        double S_H = currentSpacing/ height;

		double [] U_UH = new double [SECTIONS] ;


        for( int i = 0 ; i < SECTIONS/2; i++ )
        {
            U_UH[i] = Math.exp( -( -2.4 * Math.log(S_H) + 1.621 ) * ( 1.0 - (i/(SECTIONS/2.0))) );
        }
        for( int i = SECTIONS/2 ; i < SECTIONS ; i++)
        {
            U_UH[i] = Math.log( ( (i * height/(SECTIONS/2.0)) - roughnessSimple_d)/roughnessSimple_z0) / Math.log(( height - roughnessSimple_d)/roughnessSimple_z0);
        }

       //System.out.println 	("results of WindProfile : " + m.U_UH );
       System.out.println 	("results of WindProfile : " );
	   		for (int i = 0; i < SECTIONS; i++) {
	   				System.out.println("" + U_UH[i]);
		}
		return U_UH;

    }




}