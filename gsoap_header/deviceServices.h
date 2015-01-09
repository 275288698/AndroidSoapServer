//gsoap ns service name:    	DeviceServices
//gsoap ns service style:   	document
//gsoap ns service encoding:    literal
//gsoap ns service namespace:   http://localhost:8080/DeviceServices.wsdl
//gsoap ns service location:    http://localhost:8080
//gsoap ns schema namespace: 	urn:DeviceServices

struct ns__Sensor
{
	char*   mName;
	char*   mVendor;
	int     mVersion;
	int     mType;
	float   mMaxRange;
	float   mResolution;
	float   mPower;
	int     mMinDelay;
};

struct ns__SensorArray
{
	int __size;
	struct ns__Sensor *sensors;
};

struct ns__Location
{
	double longitude;
	double latitude;
};

struct ns__Message
{
	char*   message;
};

struct ns__Confirmation
{
	bool 	ok;
};

int ns__getAvailableSensorsInfo(void *_, struct ns__SensorArray *sensors);
int ns__getCurrentDeviceLocation(void *_, struct ns__Location *location);
int ns__logMessage(struct ns__Message *message, struct ns__Confirmation *confirmation);