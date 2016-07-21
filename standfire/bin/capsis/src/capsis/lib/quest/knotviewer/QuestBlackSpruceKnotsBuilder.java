package capsis.lib.quest.knotviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.math3.distribution.BetaDistribution;

import capsis.extension.treelogger.GPieceDisc;
import capsis.lib.quest.commons.QuestTaper;

/**
 * QuestKnotsBuilder computes the knots distribution and geometry for black
 * spruce.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestBlackSpruceKnotsBuilder extends QuestKnotsBuilder {

	private Random random;
	private int nGU;
	private QuestTaper taper;
	private List<Double> dbhs;
	private List<Double> heights;

	// Parameters for equation of number of knots per growth unit
	// private double n1 = 1.919;
	// private double n2 = 4.233;
	// private double n3 = 0.017;

	private double n1 = 2.2484;
	private double n2 = 76.9822;
	private double n3 = -0.0669;
	private double n4 = 2.0942;

	// Parameters for the relative position of the knots in their gu
	private double b1 = -0.792;
	private double b2 = 1.658;
	private double b3 = 0.022;
	private double b4 = -0.003;
	private double b5 = 1.319;
	private double b6 = -0.009;
	private double b7 = -0.038;
	private double b8 = -0.826;

	// Parameters for the knot diameter equation
	private double c1_1 = 0;
	private double c2_1 = 0;
	private double c3_1 = 1.0144;
	private double c4_1 = 0.3661;
	private double c5_1 = 0;
	private double c6_1 = 0.2653;
	private double c7_1 = 0;
	private double c8_1 = 0;
	private double c9_1 = -0.0011;
	private double c10_1 = 0;

	private double c1_2 = -0.0338;
	private double c2_2 = 0.5166;
	private double c3_2 = -0.0302;
	private double c4_2 = 0.1285;
	private double c5_2 = 0;
	private double c6_2 = 0.1031;
	private double c7_2 = 0.0549;
	private double c8_2 = -0.0004;
	private double c9_2 = -0.0004;
	private double c10_2 = 0;

	private double c1_3 = 0.0139;
	private double c2_3 = 0.9699;
	private double c3_3 = -0.0020;
	private double c4_3 = 0.0068;
	private double c5_3 = 0.0002;
	private double c6_3 = 0.0057;
	private double c7_3 = 0;
	private double c8_3 = -0.0001;
	private double c9_3 = -0.0002;
	private double c10_3 = 0.0006;

	// Parameters for knot curvature equation
	private double d1_1 = -0.2753;
	private double d2_1 = -0.0027;
	private double d3_1 = 0.1864;
	private double d4_1 = -0.0039;
	private double d5_1 = 0.1294;
	private double d6_1 = 0.2498;
	private double d7_1 = 0.0064;
	private double d8_1 = 0.0036;
	private double d9_1 = 0.0009;

	private double d1_2 = 0.0188;
	private double d2_2 = -0.0003;
	private double d3_2 = 0.9719;
	private double d4_2 = 0.0002;
	private double d5_2 = -0.0357;
	private double d6_2 = -0.0033;
	private double d7_2 = 0;
	private double d8_2 = 0;
	private double d9_2 = 0.0001;

	// Parameters for mortality model
	private double m1 = 3.2064;
	private double m2 = 0.0033;
	private double m3 = 0.1081;
	private double m4 = -0.7667;
	private double m5 = 0.1019;
	private double m6 = -0.0187;
	private double m7 = -0.0468;
	private double m8 = 0.0393;

	// Parameters for pruning model
	private double p1 = -4.0743;
	private double p2 = 0.0491;
	private double p3 = -0.4951;
	private double p4 = 0.0577;
	private double p5 = 0.1341;

	private List<QuestGU> gus;

	/**
	 * Constructor.
	 */
	public QuestBlackSpruceKnotsBuilder() {
		random = new Random();
	}

	@Override
	public void execute(QuestTaper taper, List<Double> dbhs, List<Double> heights) {
		this.taper = taper;
		nGU = heights.size();

		gus = new ArrayList<QuestGU>();
		// knots = new ArrayList<QuestKnot>();

		this.dbhs = dbhs;
		this.heights = heights;

//		System.out.println("QuestBlackSpruceKnotsBuilder nGU: " + nGU);

		// Get the growth units z0s and lengths
		List<Double> guLengths_m = new ArrayList<>();
		List<Double> guZ0s_m = new ArrayList<>();

		double z = 0;
		for (double h : heights) {
			guZ0s_m.add(z);

			double length_m = h - z;
			guLengths_m.add(length_m);
			z += length_m;
		}

		// 1. Create the knots
		initKnots(guZ0s_m, guLengths_m);

		// 2. Add the knots geometry
		createKnotsGeometry(gus /* guZ0s_m, guLengths_m */);

	}

	/**
	 * Create the knots with their z0 and azimut.
	 */
	private void initKnots(List<Double> guZ0s_m, List<Double> guLengths_m) {
//		System.out.println("QuestBlackSpruceKnotsBuilder initKnots");

		for (int gu = 1; gu <= nGU; gu++) {

			int i = gu - 1;
			double guZ0_m = guZ0s_m.get(i);
			double guLength_m = guLengths_m.get(i);

			// If missing part at the bottom of the tree, add no knots inside
			if (gu == 1 && skipGu0 (guLengths_m)) // skip gu0 if missing data
				continue;
//			if (gu == 1 && guLength_m >= 2) // a gu can not be 2 m long
//				continue;

			// Model 1
			int n = (int) Math.round(n1 + n2 * guLength_m + n3 * guZ0_m + n4 * guLength_m * guZ0_m);

			double treeDbh_cm = dbhs.get(gu - 1);
			double treeHeight_m = heights.get(gu - 1);

			QuestGU growthUnit = new QuestGU(gu, guZ0_m, guLength_m, n, treeDbh_cm, treeHeight_m);
			gus.add(growthUnit);

			// Model 2
			// int n = (int) Math.round(Math.log(n1 + n2 * guLength_m + n3 *
			// guZ0_m));

//			System.out.println("QuestBlackSpruceKnotsBuilder gu: " + gu + " n: " + n);

			for (int k = 1; k <= n; k++) {

				String knotId = "" + gu + "_" + k;

				// double Pbr = Math.pow(k, -0.26);

				// To check Capsis Quest against R
				// double zr = Pbr;
				// double zrMean = b1 + b2 * Pbr + b3 * n + b4 * guZ0_m + b5 *
				// guLength_m + b6 * Pbr * n + b7 * n
				// * guLength_m + b8 * Pbr * guLength_m;

				// // A beta distribution may be used, ongoing work by ed
				// double zr = zrMean; // tmp

				double alpha = 1.69291 - 0.02622 * k;
				double beta = 20.00094 * (1 - Math.exp(-0.02544 * k)) - 0.32242 * k;
				BetaDistribution dist = new BetaDistribution(alpha, beta);

				double zr = dist.sample();

//				if (k == 1)
//					System.out.println("QuestBlackSpruceKnotsBuilder k: 1 zr: " + zr);
//				if (zr < 0 || zr > 1)
//					System.out
//							.println("*** QuestBlackSpruceKnotsBuilder WRONG ZR, should be in [0, 1]: " + zr + " ***");

				// if (zr < 0)
				// zr = 0;
				// if (zr > 1)
				// zr = 1;

				double z0_mm = (guZ0_m + zr * guLength_m) * 1000;

				// if (gu > 1)
				// System.out.println(knotId + " guZ0_m: " + guZ0_m +
				// " guLength_m: "
				// + guLength_m + " zr: " + zr + " z0_mm: "
				// + z0_mm);

				double azimut = random.nextDouble() * 360;

				QuestKnot knot = new QuestKnot(knotId, /* gu, */zr, z0_mm, azimut);
				growthUnit.addKnot(knot);
			}

		}

	}

	/**
	 * Evaluate if the gu0 should be drawn. In case of missing data, the gu0
	 * is very thick and we do not want to draw it.
	 */
	private boolean skipGu0(List<Double> guLengths_m) {
		try {
			List<Double> copy = new ArrayList<Double>(guLengths_m);

			Double length0 = copy.remove(0);
			
			double sum = 0d;
			int n = copy.size ();
			
			for (double length : copy) {
				sum += length;
			}
			double mean = sum / n;

			if (length0 > 2 * mean) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// If trouble, do not skip, the problem may be visible
			return false;
		}

	}

	/**
	 * Create the knots geometry.
	 */
	private void createKnotsGeometry(List<QuestGU> gus /*
														 * List<Double>
														 * guZ0s_m,
														 * List<Double>
														 * guLengths_m
														 */) {
//		System.out.println("Trace: createKnotsGeometry... ");

		boolean overBark = false;

		for (QuestGU growthUnit : gus) {

			for (QuestKnot knot : growthUnit.getKnots()) {
				// knot.z0

				int gu = growthUnit.id;
				// int gu = knot.gu;

				int ii = gu - 1;

				double guZ0_m = growthUnit.z0_m;
				double guLength_m = growthUnit.length_m;
				// double guZ0_m = guZ0s_m.get(ii);
				// double guLength_m = guLengths_m.get(ii);

				int nRings = nGU + 1 - gu;
				double[] ringRadius = new double[nRings]; // mm
				double[] ringLength = new double[nRings]; // mm

				double prevR_mm = 0;

				for (int r = 0; r < nRings; r++) {

					int ring = r + 1;

					double treeDbh = dbhs.get(gu - 1 + r); //
					double treeHeight = heights.get(gu - 1 + r); //

					double r_cm = taper.getTreeRadius_cm(treeDbh, treeHeight, knot.z0_mm / 1000d, overBark);

					// if (gu == 2) {
					// System.out.println("Taper called: gu: "+gu+" treeDbh: " +
					// treeDbh + " treeHeight: " + treeHeight + " z0_m: "
					// + knot.z0_mm / 1000d + " returned r_cm: " + r_cm);
					// }

					double r_mm = r_cm * 10;
					ringRadius[r] = r_mm;
					ringLength[r] = r_mm - prevR_mm;

					prevR_mm = r_mm;
				}

				double prevDeltaZ = 0;
				double sumDeltaZ = 0;

				double prevKnotDiameter_mm = 0;
				double prevDeltaDiameter_mm = 0;
				QuestKnotDiameter prevKnotDiameter = null;

				double timeFromDeath = 0;

				// System.out.println("QuestBSKnotsBuilder knot: "+knot.id+" creating diameters...");

				for (int ring = 1; ring <= nRings; ring++) {

					double c1 = c1_1;
					double c2 = c2_1;
					double c3 = c3_1;
					double c4 = c4_1;
					double c5 = c5_1;
					double c6 = c6_1;
					double c7 = c7_1;
					double c8 = c8_1;
					double c9 = c9_1;
					double c10 = c10_1;
					if (ring >= 4 && ring <= 25) {
						c1 = c1_2;
						c2 = c2_2;
						c3 = c3_2;
						c4 = c4_2;
						c5 = c5_2;
						c6 = c6_2;
						c7 = c7_2;
						c8 = c8_2;
						c9 = c9_2;
						c10 = c10_2;
					} else if (ring > 25) {
						c1 = c1_3;
						c2 = c2_3;
						c3 = c3_3;
						c4 = c4_3;
						c5 = c5_3;
						c6 = c6_3;
						c7 = c7_3;
						c8 = c8_3;
						c9 = c9_3;
						c10 = c10_3;
					}

					double d1 = d1_1;
					double d2 = d2_1;
					double d3 = d3_1;
					double d4 = d4_1;
					double d5 = d5_1;
					double d6 = d6_1;
					double d7 = d7_1;
					double d8 = d8_1;
					double d9 = d9_1;
					if (ring > 50) {
						d1 = d1_2;
						d2 = d2_2;
						d3 = d3_2;
						d4 = d4_2;
						d5 = d5_2;
						d6 = d6_2;
						d7 = d7_2;
						d8 = d8_2;
						d9 = d9_2;
					}

					int index = gu - 1 + ring;
					int i = index - 1;
					double d_cm = dbhs.get(i);
					double h_m = heights.get(i);
					double hd = h_m / d_cm;

					double r_mm = ringRadius[ring - 1];
					double length_mm = ringLength[ring - 1];

					// System.out.println("QuestBSKnotsBuilder ring: "+ring+
					// " dbh (= d_cm): "+d_cm);

					// Knot diameter
					double knotDiameter_mm = 0;
					if (ring < 4) {

						knotDiameter_mm = c1 + c2 * prevDeltaDiameter_mm + c3 * prevKnotDiameter_mm + c4 * knot.zr + c5
								* r_mm + c6 * length_mm + c7 * hd + c8 * index + c9 * d_cm + c10 * h_m;

					} else {

						double delta = c1 + c2 * prevDeltaDiameter_mm + c3 * prevKnotDiameter_mm + c4 * knot.zr + c5
								* r_mm + c6 * length_mm + c7 * hd + c8 * index + c9 * d_cm + c10 * h_m;
						knotDiameter_mm = prevKnotDiameter_mm + delta;

					}

					// Knot delta Z
					double deltaZ_mm = d1 + d2 * prevKnotDiameter_mm + d3 * prevDeltaZ + d4 * r_mm + d5 * length_mm
							+ d6 * knot.zr + d7 * hd + d8 * index + d9 * d_cm;

					double z = knot.z0_mm + sumDeltaZ + deltaZ_mm;

					double x = r_mm * Math.cos(knot.azimut);
					double y = r_mm * Math.sin(knot.azimut);

					// if (knot.zr == 1 && gu == 2 && (ring == 1 || ring == 2 ||
					// ring == 3)) {
					// System.out.println("----------------------------------------------------------------------");
					// System.out.println("QuestBSKnotsBuilder Checking gu: " +
					// gu + " knot: 1 diameter: " + ring);
					// System.out.println("d_cm: " + d_cm);
					// System.out.println("h_m:  " + h_m);
					// System.out.println();
					// System.out.println("guZ0_m:      " + guZ0_m);
					// System.out.println("guLength_m:  " + guLength_m);
					// System.out.println();
					// System.out.println("hd:              " + hd);
					// System.out.println("knot.z0_mm:      " + knot.z0_mm);
					// System.out.println("r_mm:          " + r_mm);
					// System.out.println("length_mm:     " + length_mm);
					// System.out.println();
					// System.out.println("prevDeltaDiameter_mm: " +
					// prevDeltaDiameter_mm);
					// System.out.println("prevKnotDiameter_mm:  " +
					// prevKnotDiameter_mm);
					// System.out.println("knot.zr:              " + knot.zr);
					// System.out.println("index:                " + index);
					// System.out.println("knotDiameter_mm:       " +
					// knotDiameter_mm);
					// System.out.println();
					// System.out.println("prevDeltaZ:           " +
					// prevDeltaZ);
					// System.out.println("deltaZ_mm:             " +
					// deltaZ_mm);
					// System.out.println();
					// System.out.println("z:                " + z);
					// System.out.println("***");
					//
					// }

					// Knot is alive or dead ?
					boolean alive = true;
					if (prevKnotDiameter != null && !prevKnotDiameter.alive) {
						alive = false;
						knotDiameter_mm = prevKnotDiameter_mm;
						timeFromDeath++;

					} else {

						// fc+ed-19.3.2015 removed m7 * d_cm
						// double p = m1 + m2 * ring + m3 * knot.zr + m4 *
						// guLength_m
						// + m5 * knotDiameter_mm + m6 * r_mm + m8
						// * guZ0_m;

						double p = m1 + m2 * ring + m3 * knot.zr + m4 * guLength_m + m5 * knotDiameter_mm + m6 * r_mm
								+ m7 * d_cm + m8 * guZ0_m;

						double pDead = 1 - (Math.exp(p) / (1 + Math.exp(p)));

						if (random.nextDouble() <= pDead) {
							alive = false;
						}
						// if (pDead <= 0.05) {
						//
						// if (gu <= 3)
						// System.out.println("QuestBlackSpruceKnotsBuilder DEAD gu: "
						// + gu + " zr: " + knot.zr
						// + " ring: " + ring + " p: " + p + " pDead: " +
						// pDead);
						//
						// alive = false;
						// }

						// // TEST disable mortality TMP
						// alive = true;
					}

					// Knot is pruned ?
					if (!alive) {

						double p = p1 + p2 * timeFromDeath + p3 * knot.zr + p4 * knotDiameter_mm + p5 * guZ0_m;

						double pPruned = Math.exp(p) / (1 + Math.exp(p));

						// if (gu <= 3)
						// System.out.println("QuestBlackSpruceKnotsBuilder PRUNED gu: "
						// + gu + " zr: " + knot.zr
						// + " ring: " + ring + " p: " + p + " pPruned: " +
						// pPruned);

						// if (pPruned <= 0.80) {
						// break;
						// }
						if (random.nextDouble() <= pPruned) {
							break;
						}
					}

					// Add a diameter in the knot
					QuestKnotDiameter kd = new QuestKnotDiameter(ring, x, y, z, knotDiameter_mm, alive);
					knot.addDiameter(kd);

					prevKnotDiameter = kd;

					prevDeltaDiameter_mm = knotDiameter_mm - prevKnotDiameter_mm;
					prevKnotDiameter_mm = knotDiameter_mm;

					prevDeltaZ = deltaZ_mm;
					sumDeltaZ += deltaZ_mm;
				}

			}
		}

	}

	public List<QuestGU> getGUs() {
		return gus;
	}

}
