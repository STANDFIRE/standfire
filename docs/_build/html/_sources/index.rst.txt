.. standfire documentation master file, created by
   sphinx-quickstart on Mon Dec  7 22:08:48 2015.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

####################
Welcome to STANDFIRE
####################
STANDFIRE is a prototype collaborative platform for modeling wildland fuels and fire behavior. STANDFIRE builds upon and extends the capabilities of the Fire and Fuels Extension of the Forest Vegetation Simulator (FFE-FVS), modeling wildland fuels and fire behavior in 3D, providing a more detailed approach for examining how fuel changes such as fuel treatments may affect fire behavior. 

A key goal of the STANDFIRE prototype modeling platform is to advance our capabilities in fuel modeling. STANDFIRE offers new possibilities, representing wildland fuels in the United States with more detail than has been possible before. Rather than representing canopy fuels inputs to fire models as single, stand level values, STANDFIRE represents forests as collections of individual trees with their own attributes, taking advantage of all the detail provided by forest inventory data and associated modeled outputs from FVS. STANDFIRE also provides a new set of capabilities for addressing surface fuel heterogeneity, allowing the representation of fuels that are discontinuous and/or overlapping; important heterogeneities, such as mixes of live and dead components within the same fuel type (i.e. grass or shrub) can be represented as well.

STANDFIRE provides a systematic process by which forest inventory and fuels data can be developed as inputs for two independent physics-based fire behavior models, WFDS (the Wildland Urban Interface Fire Dynamics Simulator), and FIRETEC. WFDS is a version of the Fire Dynamics Simulator developed through collaboration between the National Institute of Standards and Technology (NIST) and the USFS Pacific Northwest Station. FIRETEC was developed at the Los Alamos National Laboratory. STANDFIRE is comprised of three main parts: fuel modeling, fire modeling, and post-processing, to analyze fire simulation outputs. The fire models in STANDFIRE were developed by other institutions; STANDFIRE simply gets data into the fire models, and then deals with the outputs. 

STANDFIRE continues to be in active development, so this online documentation may not reflect the most recent modifications to the STANDFIRE system. For more information about STANDFIRE, please contact Russ Parsons  rparsons@fs.fed.us.  


Contents:

.. toctree::
   :maxdepth: 3

   user_guide
   api_ref

##################
Indices and tables
##################

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

