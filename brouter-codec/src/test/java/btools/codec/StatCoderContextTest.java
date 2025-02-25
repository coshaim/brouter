package btools.codec;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class StatCoderContextTest
{
  @Test
  public void noisyVarBitsEncodeDecodeTest()
  {
    byte[] ab = new byte[40000];
    StatCoderContext ctx = new StatCoderContext( ab );
    for ( int noisybits = 1; noisybits < 12; noisybits++ )
    {
      for ( int i = 0; i < 1000; i++ )
      {
        ctx.encodeNoisyNumber( i, noisybits );
      }
    }
    ctx = new StatCoderContext( ab );

    for ( int noisybits = 1; noisybits < 12; noisybits++ )
    {
      for ( int i = 0; i < 1000; i++ )
      {
        int value = ctx.decodeNoisyNumber( noisybits );
        if ( value != i )
        {
          Assert.fail( "value mismatch: noisybits=" + noisybits + " i=" + i + " value=" + value );
        }
      }
    }
  }

  @Test
  public void noisySignedVarBitsEncodeDecodeTest()
  {
    byte[] ab = new byte[80000];
    StatCoderContext ctx = new StatCoderContext( ab );
    for ( int noisybits = 0; noisybits < 12; noisybits++ )
    {
      for ( int i = -1000; i < 1000; i++ )
      {
        ctx.encodeNoisyDiff( i, noisybits );
      }
    }
    ctx = new StatCoderContext( ab );

    for ( int noisybits = 0; noisybits < 12; noisybits++ )
    {
      for ( int i = -1000; i < 1000; i++ )
      {
        int value = ctx.decodeNoisyDiff( noisybits );
        if ( value != i )
        {
          Assert.fail( "value mismatch: noisybits=" + noisybits + " i=" + i + " value=" + value );
        }
      }
    }
  }

  @Test
  public void predictedValueEncodeDecodeTest()
  {
    byte[] ab = new byte[80000];
    StatCoderContext ctx = new StatCoderContext( ab );
    for ( int value = -100; value < 100; value += 5 )
    {
      for ( int predictor = -200; predictor < 200; predictor += 7 )
      {
        ctx.encodePredictedValue( value, predictor );
      }
    }
    ctx = new StatCoderContext( ab );

    for ( int value = -100; value < 100; value += 5 )
    {
      for ( int predictor = -200; predictor < 200; predictor += 7 )
      {
        int decodedValue = ctx.decodePredictedValue( predictor );
        if ( value != decodedValue )
        {
          Assert.fail( "value mismatch: value=" + value + " predictor=" + predictor + " decodedValue=" + decodedValue );
        }
      }
    }
  }

  @Test
  public void sortedArrayEncodeDecodeTest()
  {
    Random rand = new Random();
    int size = 1000000;
    int[] values = new int[size];
    for ( int i = 0; i < size; i++ )
    {
      values[i] = rand.nextInt() & 0x0fffffff;
    }
    values[5] = 175384; // force collision
    values[8] = 175384;

    values[15] = 275384; // force neighbours
    values[18] = 275385;

    Arrays.sort( values );

    byte[] ab = new byte[3000000];
    StatCoderContext ctx = new StatCoderContext( ab );
    ctx.encodeSortedArray( values, 0, size, 0x08000000, 0 );

    ctx = new StatCoderContext( ab );

    int[] decodedValues = new int[size];
    ctx.decodeSortedArray( decodedValues, 0, size, 27, 0 );

    for ( int i = 0; i < size; i++ )
    {
      if ( values[i] != decodedValues[i] )
      {
        Assert.fail( "mismatch at i=" + i + " " + values[i] + "<>" + decodedValues[i] );
      }
    }
  }
}
