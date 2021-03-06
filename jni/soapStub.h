/* soapStub.h
   Generated by gSOAP 2.8.17r from c:\Users\joHn_ny\Desktop\new\deviceServices.h

Copyright(C) 2000-2013, Robert van Engelen, Genivia Inc. All Rights Reserved.
The generated code is released under one of the following licenses:
GPL or Genivia's license for commercial use.
This program is released under the GPL with the additional exemption that
compiling, linking, and/or using OpenSSL is allowed.
*/

#ifndef soapStub_H
#define soapStub_H
#include "stdsoap2.h"
#if GSOAP_VERSION != 20817
# error "GSOAP VERSION MISMATCH IN GENERATED CODE: PLEASE REINSTALL PACKAGE"
#endif


/******************************************************************************\
 *                                                                            *
 * Enumerations                                                               *
 *                                                                            *
\******************************************************************************/


/******************************************************************************\
 *                                                                            *
 * Types with Custom Serializers                                              *
 *                                                                            *
\******************************************************************************/


/******************************************************************************\
 *                                                                            *
 * Classes and Structs                                                        *
 *                                                                            *
\******************************************************************************/


#if 0 /* volatile type: do not declare here, declared elsewhere */

#endif

#ifndef SOAP_TYPE_ns__Sensor
#define SOAP_TYPE_ns__Sensor (7)
/* ns:Sensor */
struct ns__Sensor
{
public:
	char *mName;	/* optional element of type xsd:string */
	char *mVendor;	/* optional element of type xsd:string */
	int mVersion;	/* required element of type xsd:int */
	int mType;	/* required element of type xsd:int */
	float mMaxRange;	/* required element of type xsd:float */
	float mResolution;	/* required element of type xsd:float */
	float mPower;	/* required element of type xsd:float */
	int mMinDelay;	/* required element of type xsd:int */
public:
	int soap_type() const { return 7; } /* = unique type id SOAP_TYPE_ns__Sensor */
};
#endif

#ifndef SOAP_TYPE_ns__SensorArray
#define SOAP_TYPE_ns__SensorArray (9)
/* ns:SensorArray */
struct ns__SensorArray
{
public:
	int __size;	/* SOAP 1.2 RPC return element (when namespace qualified) */	/* sequence of elements <sensors> */
	struct ns__Sensor *sensors;	/* optional element of type ns:Sensor */
public:
	int soap_type() const { return 9; } /* = unique type id SOAP_TYPE_ns__SensorArray */
};
#endif

#ifndef SOAP_TYPE_ns__Location
#define SOAP_TYPE_ns__Location (11)
/* ns:Location */
struct ns__Location
{
public:
	double longitude;	/* SOAP 1.2 RPC return element (when namespace qualified) */	/* required element of type xsd:double */
	double latitude;	/* required element of type xsd:double */
public:
	int soap_type() const { return 11; } /* = unique type id SOAP_TYPE_ns__Location */
};
#endif

#ifndef SOAP_TYPE_ns__getAvailableSensorsInfo
#define SOAP_TYPE_ns__getAvailableSensorsInfo (17)
/* ns:getAvailableSensorsInfo */
struct ns__getAvailableSensorsInfo
{
public:
	void *_;	/* transient */
public:
	int soap_type() const { return 17; } /* = unique type id SOAP_TYPE_ns__getAvailableSensorsInfo */
};
#endif

#ifndef SOAP_TYPE_ns__getCurrentDeviceLocation
#define SOAP_TYPE_ns__getCurrentDeviceLocation (20)
/* ns:getCurrentDeviceLocation */
struct ns__getCurrentDeviceLocation
{
public:
	void *_;	/* transient */
public:
	int soap_type() const { return 20; } /* = unique type id SOAP_TYPE_ns__getCurrentDeviceLocation */
};
#endif

#ifndef WITH_NOGLOBAL

#ifndef SOAP_TYPE_SOAP_ENV__Header
#define SOAP_TYPE_SOAP_ENV__Header (21)
/* SOAP Header: */
struct SOAP_ENV__Header
{
public:
	int soap_type() const { return 21; } /* = unique type id SOAP_TYPE_SOAP_ENV__Header */
#ifdef WITH_NOEMPTYSTRUCT
private:
	char dummy;	/* dummy member to enable compilation */
#endif
};
#endif

#endif

#ifndef WITH_NOGLOBAL

#ifndef SOAP_TYPE_SOAP_ENV__Code
#define SOAP_TYPE_SOAP_ENV__Code (22)
/* SOAP Fault Code: */
struct SOAP_ENV__Code
{
public:
	char *SOAP_ENV__Value;	/* optional element of type xsd:QName */
	struct SOAP_ENV__Code *SOAP_ENV__Subcode;	/* optional element of type SOAP-ENV:Code */
public:
	int soap_type() const { return 22; } /* = unique type id SOAP_TYPE_SOAP_ENV__Code */
};
#endif

#endif

#ifndef WITH_NOGLOBAL

#ifndef SOAP_TYPE_SOAP_ENV__Detail
#define SOAP_TYPE_SOAP_ENV__Detail (24)
/* SOAP-ENV:Detail */
struct SOAP_ENV__Detail
{
public:
	char *__any;
	int __type;	/* any type of element <fault> (defined below) */
	void *fault;	/* transient */
public:
	int soap_type() const { return 24; } /* = unique type id SOAP_TYPE_SOAP_ENV__Detail */
};
#endif

#endif

#ifndef WITH_NOGLOBAL

#ifndef SOAP_TYPE_SOAP_ENV__Reason
#define SOAP_TYPE_SOAP_ENV__Reason (25)
/* SOAP-ENV:Reason */
struct SOAP_ENV__Reason
{
public:
	char *SOAP_ENV__Text;	/* optional element of type xsd:string */
public:
	int soap_type() const { return 25; } /* = unique type id SOAP_TYPE_SOAP_ENV__Reason */
};
#endif

#endif

#ifndef WITH_NOGLOBAL

#ifndef SOAP_TYPE_SOAP_ENV__Fault
#define SOAP_TYPE_SOAP_ENV__Fault (26)
/* SOAP Fault: */
struct SOAP_ENV__Fault
{
public:
	char *faultcode;	/* optional element of type xsd:QName */
	char *faultstring;	/* optional element of type xsd:string */
	char *faultactor;	/* optional element of type xsd:string */
	struct SOAP_ENV__Detail *detail;	/* optional element of type SOAP-ENV:Detail */
	struct SOAP_ENV__Code *SOAP_ENV__Code;	/* optional element of type SOAP-ENV:Code */
	struct SOAP_ENV__Reason *SOAP_ENV__Reason;	/* optional element of type SOAP-ENV:Reason */
	char *SOAP_ENV__Node;	/* optional element of type xsd:string */
	char *SOAP_ENV__Role;	/* optional element of type xsd:string */
	struct SOAP_ENV__Detail *SOAP_ENV__Detail;	/* optional element of type SOAP-ENV:Detail */
public:
	int soap_type() const { return 26; } /* = unique type id SOAP_TYPE_SOAP_ENV__Fault */
};
#endif

#endif

/******************************************************************************\
 *                                                                            *
 * Typedefs                                                                   *
 *                                                                            *
\******************************************************************************/

#ifndef SOAP_TYPE__QName
#define SOAP_TYPE__QName (5)
typedef char *_QName;
#endif

#ifndef SOAP_TYPE__XML
#define SOAP_TYPE__XML (6)
typedef char *_XML;
#endif


/******************************************************************************\
 *                                                                            *
 * Externals                                                                  *
 *                                                                            *
\******************************************************************************/


/******************************************************************************\
 *                                                                            *
 * Server-Side Operations                                                     *
 *                                                                            *
\******************************************************************************/


SOAP_FMAC5 int SOAP_FMAC6 ns__getAvailableSensorsInfo(struct soap*, void *_, struct ns__SensorArray *sensors);

SOAP_FMAC5 int SOAP_FMAC6 ns__getCurrentDeviceLocation(struct soap*, void *_, struct ns__Location *location);

/******************************************************************************\
 *                                                                            *
 * Server-Side Skeletons to Invoke Service Operations                         *
 *                                                                            *
\******************************************************************************/

extern "C" SOAP_FMAC5 int SOAP_FMAC6 soap_serve(struct soap*);

extern "C" SOAP_FMAC5 int SOAP_FMAC6 soap_serve_request(struct soap*);

SOAP_FMAC5 int SOAP_FMAC6 soap_serve_ns__getAvailableSensorsInfo(struct soap*);

SOAP_FMAC5 int SOAP_FMAC6 soap_serve_ns__getCurrentDeviceLocation(struct soap*);

#endif

/* End of soapStub.h */
