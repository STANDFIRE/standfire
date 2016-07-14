package capsis.lib.fire.intervener.physicalfireeffect;

import capsis.lib.fire.fuelitem.FiPlant;
import fireparadox.model.FmModel;

public class FiLocalPhysicalProperties {
	/**
	 * Frequency of population cell mortality in arrhenius model for crown
	 * (needles and buds)
	 * 
	 * @param speciesName
	 * @param particle
	 *            (FiModel.LEAVESb for leaves, !FiModel.LEAVESb for buds)
	 * @return Z (1/s)
	 */
	public static double getZ(String speciesName, boolean particle) {
		// specific value of leaves for QUERCUS_ILEX
		if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
			if (particle == FiretecVoxel.LEAVEb) {
				return 4.12e63;
			} else {// buds
				return 1.22e26;
			}
		}
		// other leaves
		if (particle == FiretecVoxel.LEAVEb) {
			return 1.56e41;
		} else {
			if (speciesName.equals(FmModel.PINUS_HALEPENSIS)
					|| speciesName.equals(FmModel.PINUS_BRUTIA)) {
				return 1.56e41;
			}
			if (speciesName.equals(FmModel.PINUS_PINEA)) {
				return 1.01e26;
			}
			if (speciesName.equals(FmModel.PINUS_NIGRA)
					|| speciesName.equals(FmModel.PINUS_NIGRA_LARICIO)) {
				return 6.98e41;
			}
			if (speciesName.equals(FmModel.PINUS_PINASTER)) {
				return 1.04e20;
			}
		}
		// DEFAULT VALUE
		return 1.56e41;
	}

	/**
	 * Energy activation of population cell mortality in arrhenius model for
	 * crown (needles and buds)
	 * 
	 * @param speciesName
	 * @param particle
	 *            (FiModel.LEAVESb for leaves, !FiModel.LEAVESb for buds)
	 * @return E (J.mol-1)
	 */
	public static double getE(String speciesName, boolean particle) {
		// specific value of leaves for QUERCUS_ILEX
		if (speciesName.equals(FmModel.QUERCUS_ILEX)) {
			if (particle == FiretecVoxel.LEAVEb) {
				return 411820d;
			} else {// buds
				return 178200d;
			}
		}
		// other leaves
		if (particle == FiretecVoxel.LEAVEb) {
			return 272030d;
		} else {
			if (speciesName.equals(FmModel.PINUS_HALEPENSIS)
					|| speciesName.equals(FmModel.PINUS_BRUTIA)) {
				return 272030d;
			}
			if (speciesName.equals(FmModel.PINUS_PINEA)) {
				return 176790d;
			}
			if (speciesName.equals(FmModel.PINUS_NIGRA)
					|| speciesName.equals(FmModel.PINUS_NIGRA_LARICIO)) {
				return 280320d;
			}
			if (speciesName.equals(FmModel.PINUS_PINASTER)) {
				return 140090d;
			}
		}
		// DEFAULT VALUE
		return 1.56e41;

	}

}
