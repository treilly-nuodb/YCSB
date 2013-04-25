package com.yahoo.ycsb.generator;

import java.util.ArrayList;

import com.yahoo.ycsb.Utils;

/**
 * An extension of the zipfian genarator that focuses the hotspots on the center of fractions of the
 * keyspace.   Centering is acheived by adding an centered offset to the zipfian value and treating odd/even 
 * as +/- the center.   Focusing increases as the number of clients is added to keep the percentages of requests
 * hitting a particular slice constant, this is achieved by using an array of ZipfianGenerators for each power
 * of two leading to the number of clients.
 */
public class FocusedZipfianGenerator extends LongGenerator 
{
	ZipfianGenerator gen;
	int genIndex;
	long _itemcount,_offset;
	
	/**
	 * 
	 * @param max
	 * @param num numerator, or client id should be 0 -> denom
	 * @param denom denominator, or number of clients
	 */
	public FocusedZipfianGenerator(long itemcount, long num, long denom)
	{
		_itemcount=itemcount;
		_offset = (_itemcount / (2*denom)) * (num*2+1);
		gen = new ZipfianGenerator(0,_itemcount);
	}	
	
	/**
	 * Create a focused distribution that limits it self to a fraction of the keyspace instead of hitting all keys
	 * @param itemcount size of entire key space
	 * @param num numerator or client id, tells us what slice to hit
	 * @param denom denominator or number of clients
	 * @param partition dummy flag to separate different constructors
	 */
	public FocusedZipfianGenerator(long itemcount, long num, long denom, boolean partition)
	{
		_itemcount = itemcount / denom;
		_offset = (itemcount / (2*denom)) * (num*2+1);
		gen = new ZipfianGenerator(0, _itemcount);
	}
	
	/**************************************************************************************************/
	
	/**
	 * Return the next long in the sequence.
	 */
	@Override
	public long nextLong()
	{
		long ret=gen.nextLong();
		
		// odd numbers are greater than center, even less
		if (ret % 2 == 0)
			ret = -1 * ret;
		
		// since we spread odd/even up/down we must divide by two to avoid holes.
		ret /= 2;
		
		ret=_offset + ret;
		setLastLong(ret);
		return ret;
	}

	@Override
	public double mean() 
	{
		return 0;
	}
}
