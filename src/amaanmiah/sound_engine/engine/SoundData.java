package amaanmiah.sound_engine.engine;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import static java.lang.Math.*;

public class SoundData {
	
    /**
     * Converts from a byte array to an audio sample float array.
     *
     * @param bytes   the byte array, filled by the AudioInputStream
     * @param samples an array to fill up with audio samples
     * @param bLen    the return value of AudioInputStream.read
     * @param format     the source AudioFormat
     *
     * @return the number of valid audio samples converted
     *
     * @throws NullPointerException if bytes, samples or format is null
     * @throws ArrayIndexOutOfBoundsException
     *         if bytes.length is less than bLen or
     *         if samples.length is less than bLen / bytesPerSample(fmt.getSampleSizeInBits())
     */
	public static int decode(byte[] bytes, float[] samples, int bLen, AudioFormat format) {
		int bitsPerSample = format.getSampleSizeInBits();
		int bytesPerSample = BytesPerSample(bitsPerSample);
		
		boolean isBigEndian = format.isBigEndian();
		Encoding encoding = format.getEncoding();
		
		double fullscale = fullScale(bitsPerSample);
		
		int i = 0;
		int s = 0;
		
		while (i < bLen) {
			long temp = unpackBits(bytes, i, isBigEndian, bytesPerSample);
			float sample = 0f;
			
			if (encoding == Encoding.PCM_SIGNED) {
				temp = extendSign(temp, bitsPerSample);
				sample = (float) (temp / fullscale);
			}
			
			else if (encoding == Encoding.PCM_UNSIGNED) {
				temp = unsignedToSigned(temp, bitsPerSample);
				sample = (float) (temp / fullscale);
			}
			
			else if (encoding == Encoding.PCM_FLOAT) {
				if (bitsPerSample == 32) {
					sample = Float.intBitsToFloat((int) temp);
				} else if (bitsPerSample == 64) {
					sample = (float) Double.longBitsToDouble(temp);
				}
			}
			
			else if (encoding == Encoding.ULAW) {
				sample = bitsToMuLaw(temp);
			}
			
			else if (encoding == Encoding.ALAW) {
				sample = bitsToALaw(temp);
			}
			
			samples[s] = sample;
			i += bytesPerSample;
			s++;
		}
		
		
		return s;
	}
	
    /**
     * Converts from an audio sample float array to a byte array.
     *
     * @param samples an array of audio samples to encode
     * @param bytes   an array to fill up with bytes
     * @param sLen    the return value of the decode method
     * @param format     the destination AudioFormat
     *
     * @return the number of valid bytes converted
     *
     * @throws NullPointerException if samples, bytes or format is null
     * @throws ArrayIndexOutOfBoundsException
     *         if samples.length is less than sLen or
     *         if bytes.length is less than sLen * bytesPerSample(fmt.getSampleSizeInBits())
     */
	public static int encode(float[] samples, byte[] bytes, int sLen, AudioFormat format) {
		int bitsPerSample = format.getSampleSizeInBits();
		int bytesPerSample = BytesPerSample(bitsPerSample);
		
		boolean isBigEndian = format.isBigEndian();
		Encoding encoding = format.getEncoding();
		
		double fullscale = fullScale(bitsPerSample);
		
		int i = 0;
		int s = 0;
		
		while (s < sLen) {
			long temp = 0L;
			float sample = samples[s];
			
			if (encoding == Encoding.PCM_SIGNED) {
				temp = (long) (sample * fullscale);
			}
			
			else if (encoding == Encoding.PCM_UNSIGNED) {
				temp = (long) (sample * fullscale);
				sample = signedToUnsigned(temp, bitsPerSample);
			}
			
			else if (encoding == Encoding.PCM_FLOAT) {
				if (bitsPerSample == 32) {
					temp = Float.floatToRawIntBits(sample);
				} else if (bitsPerSample == 64) {
					temp = Double.doubleToRawLongBits(sample);
				}
			}
			
			else if (encoding == Encoding.ULAW) {
				sample = muLawToBits(sample);
			}
			
			else if (encoding == Encoding.ALAW) {
				sample = aLawToBits(sample);
			}
			
			packBits(bytes, i, temp, isBigEndian, bytesPerSample);
			
			i += bytesPerSample;
			s++;
		}
		
		
		return i;
	}
	
	public static int BytesPerSample(int bitsPerSample) {
		return (int) ceil(bitsPerSample / 8.0f);
	}
	
	public static double fullScale(int bitsPerSample) {
		return pow(2,bitsPerSample-1);
	}
	
	public static long unpackBits(byte[] bytes, int i, boolean isBigEndian, int bytesPerSample) {
		
		switch (bytesPerSample) {
			case  1: return unpack8Bit(bytes, i);
			case  2: return unpack16Bit(bytes, i, isBigEndian);
			default: return unpackAnyBit(bytes, i, isBigEndian, bytesPerSample);
		}
		
	}

	private static long unpack8Bit(byte[] bytes, int i) {
		return bytes[i] & 0xffL;
	}

	private static long unpack16Bit(byte[] bytes, int i, boolean isBigEndian) {
		if (isBigEndian) {
			return ((bytes[i] & 0xffL) << 8) | (bytes[i + 1] & 0xffL);
		}
		else {
			return (bytes[i] & 0xffL) | ((bytes[i + 1] & 0xffL) << 8);
		}
	}

	private static long unpackAnyBit(byte[] bytes, int i, boolean isBigEndian, int bytesPerSample) {
		long temp = 0;
		
		if (isBigEndian) {
			for (int b = 0; b < bytesPerSample; b++) {
				temp |= (bytes[i + b] & 0xffL) 
						<< (8 * (bytesPerSample - b - 1));
			}
		}
		else {
				for (int b = 0; b < bytesPerSample; b ++) {
					temp |= (bytes[i + b] & 0xffL) << (8 * b);
				}
			}
		
		return temp;
	}
	
	
	private static void packBits(byte[] bytes, int i, long temp, boolean isBigEndian, int bytesPerSample) {
		switch (bytesPerSample) {
			case 1 : pack8Bit(bytes, i, temp);
			case 2 : pack16Bit(bytes, i, temp, isBigEndian);
			default: packAnyBit(bytes, i, temp, isBigEndian, bytesPerSample);
		}
	}

	private static void pack8Bit(byte[] bytes, int i, long temp) {
		bytes[i] = (byte) (temp & 0xffL);
	}

	private static void pack16Bit(byte[] bytes, int i, long temp, boolean isBigEndian) {
		if (isBigEndian) {
			bytes[i] = (byte) ((temp >>> 8) & 0xffL);
			bytes[i + 1] = (byte) (temp & 0xffL);
		}
		else {
			bytes[i] = (byte) (temp & 0xffL);
			bytes[i + 1] = (byte) ((temp >>> 8) & 0xffL);
		}
	}

	private static void packAnyBit(byte[] bytes, int i, long temp, boolean isBigEndian, int bytesPerSample) {
		if (isBigEndian) {
			for (int b = 0; b < bytesPerSample; b++) {
				bytes[i + b] = (byte) ((temp >>> (8 * (bytesPerSample - b - 1))) & 0xffL);
			}
		}
		
		else {
			for (int b = 0; b < bytesPerSample; b ++) {
				bytes[i + b] = (byte) ((temp >>> (8 * b)) & 0xffL);
			}
		}
	}
	
	private static long extendSign(long temp, int bitsPerSample) {
		int bitsToExtend = Long.SIZE - bitsPerSample;
		return (temp << bitsToExtend) >> bitsToExtend;
	}
	
	private static long unsignedToSigned(long temp, int bitsPerSample) {
		return temp - (long) fullScale(bitsPerSample);
	}
	
	private static long signedToUnsigned(long temp, int bitsPerSample) {
		return temp + (long) fullScale(bitsPerSample);
	}
	
    // mu-law constant
    private static final double MU = 255.0;
    // A-law constant
    private static final double A = 87.7;
    // natural logarithm of A
    private static final double LN_A = log(A);
	
	private static float bitsToMuLaw(long temp) {
        temp ^= 0xffL;
        if ((temp & 0x80L) != 0) {
            temp = -(temp ^ 0x80L);
        }

        float sample = (float) (temp / fullScale(8));

        return (float) (
            signum(sample)
                *
            (1.0 / MU)
                *
            (pow(1.0 + MU, abs(sample)) - 1.0)
        );
    }

    private static long muLawToBits(float sample) {
        double sign = signum(sample);
        sample = abs(sample);

        sample = (float) (
            sign * (log(1.0 + (MU * sample)) / log(1.0 + MU))
        );

        long temp = (long) (sample * fullScale(8));

        if (temp < 0) {
            temp = -temp ^ 0x80L;
        }

        return temp ^ 0xffL;
    }

    private static float bitsToALaw(long temp) {
        temp ^= 0x55L;
        if ((temp & 0x80L) != 0) {
            temp = -(temp ^ 0x80L);
        }

        float sample = (float) (temp / fullScale(8));

        float sign = signum(sample);
        sample = abs(sample);

        if (sample < (1.0 / (1.0 + LN_A))) {
            sample = (float) (sample * ((1.0 + LN_A) / A));
        } else {
            sample = (float) (exp((sample * (1.0 + LN_A)) - 1.0) / A);
        }

        return sign * sample;
    }

    private static long aLawToBits(float sample) {
        double sign = signum(sample);
        sample = abs(sample);

        if (sample < (1.0 / A)) {
            sample = (float) ((A * sample) / (1.0 + LN_A));
        } else {
            sample = (float) ((1.0 + log(A * sample)) / (1.0 + LN_A));
        }

        sample *= sign;

        long temp = (long) (sample * fullScale(8));

        if (temp < 0) {
            temp = -temp ^ 0x80L;
        }

        return temp ^ 0x55L;
    }
	
}
