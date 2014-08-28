package edu.agh.wsserver.utils.dto;

public class SensorDto {

	public final String  mName;
    public final String  mVendor;
    public final int     mVersion;
    public final int     mType;
    public final float   mMaxRange;
    public final float   mResolution;
    public final float   mPower;
    public final int     mMinDelay;

    public SensorDto(String mName, String mVendor, int mVersion, int mType, float mMaxRange, float mResolution, float mPower, int mMinDelay) {
		this.mName = mName;
		this.mVendor = mVendor;
		this.mVersion = mVersion;
		this.mType = mType;
		this.mMaxRange = mMaxRange;
		this.mResolution = mResolution;
		this.mPower = mPower;
		this.mMinDelay = mMinDelay;
	}
}