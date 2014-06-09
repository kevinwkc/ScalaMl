/**
 * Copyright 2013, 2014  by Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the only purpose of illustrating the 
 * concepts and algorithms presented in Scala for Machine Learning.
 */
package org.scalaml.stats

import scala.Array.canBuildFrom
import org.scalaml.core.Types

	/**
	 *  Parameterized class (view bound) that compute and update the statistics (mean,
	 *  standard deviation) for any set of observations for which the
	 *  type can be converted to a Double. 
	 *  @param values vector or array of elements of type T
	 *  @exception IllegalArgumentException if values is either undefined or have no elements
	 *  @author Patrick Nicolas
	 *  @date Jan 24, 2014
	 *  @project Scala for Machine Learning
	 */
import Stats._
import Types.ScalaMl._
class Stats[T <% Double](val values: DVector[T]) {
    require( values != null && values.size > 1, "Cannot initialize stats with undefined data")
	 
	private[this] var counters = values.foldLeft((Double.MaxValue, Double.MinValue, 0.0, 0.0))((c, x) => {
	  	            (if(x < c._1) x else c._1, if(x > c._2) x else c._2,  c._3 + x, c._4 + x*x ) })
    
	  	/**
	  	 * Arithmetic mean of the vector of values
	  	 */
	@inline
	lazy val mean = counters._3/values.size
	    /**
	     * Computation of variance for the array values
	     */
	lazy val variance = (counters._4 - mean*mean*values.size)/(values.size-1)
		 /**
	     * Computation of standard deviation for the array values
	     */
	lazy val stdDev = Math.sqrt(variance)
		/**
	     * Computation of minimun values of a vector. This values is
	     * computed during instantiation
	     */
	lazy val min = counters._1
		/**
	     * Computation of minimun values of a vector. This values is
	     * computed during instantiation
	     */
	lazy val max = counters._2
	
		/**
		 * Compute the Lidsstone smoothing factor for a set of values
		 * @param smoothing smoothing values ]0, 1] for Lidstone smoothing function
		 * @param dim Dimension of the model
		 * @exception IllegalArgumentException if either the smoothing or dimension of the model is out of range
		 */
	def lidstoneMean(smoothing: Double, dim: Int): Double = {
    	require( smoothing >0.0 && smoothing <= 1.0, "Lidstone smoothing factor " + smoothing + " is out of range")
    	require(dim > 0, "Dimension for Lidstone factor " + dim + " is out of range")

    	(counters._3 + smoothing)/(values.size + smoothing*dim)
    }
	
    
    	/**
		 * Compute the Laplace smoothing factor for a set of values
		 * @param smoothing smoothing values ]0, 1] for Laplace smoothing function
		 * @exception IllegalArgumentException if the smoothing factor is out of range
		 */
    def laplaceMean(dim: Int): Double = (counters._3 + 1.0)/(values.size + dim)

		/**
		 * Fast normalization of values within a range of [0, 1]
		 * @exception throw a Aritmetic exception if the min and max have identical values
		 */
	def normalize: DblVector = {
	   val range = max - min
	   
	   if( range < ZERO_EPS) 
	  	  throw new ArithmeticException ("Cannot normalize min: " + min + " and max: " + max)
	   values.map(x => (x - min)/range)
	}
    
    	/**
    	 * Normalization of values within a range [-0.5. 0.5]
    	 */
    def normalizeMean: DblVector = normalize(-0.5, 0.5)
    
    	/**
    	 * Normalize the data within a range [l, h]
    	 * @param l lower bound for the normalization
    	 * @param h higher bound for the normalization
    	 * @exception IllegalArgumentException of h <= l
    	 */
    def normalize(l: Double, h: Double): DblVector = {
    	require(h > l + ZERO_EPS, "Cannot normalized on undefined range " + l + ", " +h)
    	val range = h-l
    	values.map( x =>(x - l)/range)
    }
    
	   /**
	    * Normalize the data set using the mean and standard deviation. It is assumed
	    * that the data (values) follows a Gaussian distribution
	    * @exception  ArithmeticException in case of a divide by zero
	    */
    def zScore: DblVector = {
       val factor = (max - min)*stdDev
	   
	   if( factor < ZERO_EPS) 
	  	  throw new ArithmeticException ("Cannot compute the standard score divide by zero")
       values.map(x => (x - min)/factor )
    }
}

		/**
		 * Companion object to the Statistics class that define the main constructor
		 * apply and the Gaussian distributions
		 * @author Patrick Nicolas
		 * @date January 24, 2014
		 * @project Scala for Machine Learning
		 */
import Types.ScalaMl._
object Stats {
   final val ZERO_EPS = 1e-10
   final val INV_SQRT_2PI = 1.0/Math.sqrt(2.0*Math.PI)
   
   def apply[T <% Double](values: Array[T]): Stats[T] = new Stats[T](values)

   		/**
   		 * <p>Compute the Gauss density function for an array of values.</p>
   		 * @param mean mean values of the Gauss pdf
   		 * @param stdDev standard deviation of the Gauss pdf'
   		 * @param values  array of variables for which the Gauss pdf has to be computed
   		 * @exception IllegalArgumentExeption if stdDev is close t zero or the values are not defined.
   		 */
   def gauss(mean: Double, stdDev: Double, values: DblVector) : DblVector = {
      require(Math.abs(stdDev) > 1e-10, "Gauss standard deviation is close to zero")
      require(values != null, "Values for the Gauss distribution is undefined")
      
  	  values.map( x =>{val y = x - mean; INV_SQRT_2PI/stdDev * Math.exp(-0.5*y*y/stdDev)} )
   }
  
      	 /**
   		 * <p>Compute the Gauss density function for a floating point value</p>
   		 * @param mean mean values of the Gauss pdf
   		 * @param stdDev standard deviation of the Gauss pdf'
   		 * @param value  value for which the Gauss pdf has to be computed
   		 * @exception IllegalArgumentExeption if stdDev is close t zero
   		 */
   def gauss(mean: Double, stdDev: Double, x:Double) : Double = {
  	  require(Math.abs(stdDev) > 1e-10, "Gauss standard deviation is close to zero")
  	  val y = x - mean
  	  INV_SQRT_2PI/stdDev * Math.exp(-0.5*y*y/stdDev)
   }
}

// -------------------------  EOF -----------------------------------------