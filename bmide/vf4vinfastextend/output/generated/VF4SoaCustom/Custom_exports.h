/* 
 @<COPYRIGHT>@
 ==================================================
 Copyright 2012
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
 ==================================================
 @<COPYRIGHT>@

 ==================================================

   Auto-generated source from service interface.
                 DO NOT EDIT

 ==================================================
*/

#include <common/library_indicators.h>

#ifdef EXPORTLIBRARY
#define EXPORTLIBRARY something else
#error ExportLibrary was already defined
#endif

#define EXPORTLIBRARY            libvf4soacustom

#if !defined(IPLIB)
#   error IPLIB is not defined
#endif

/* Handwritten code should use SOACUSTOM_API, not SOACUSTOMEXPORT */

#define SOACUSTOM_API SOACUSTOMEXPORT

/* Support SOACUSTOMEXPORT for autogenerated schema/pif code only */

#if IPLIB==libvf4soacustom
#   if defined(__lint)
#       define SOACUSTOMEXPORT       __export(vf4soacustom)
#       define SOACUSTOMGLOBAL       extern __global(vf4soacustom)
#       define SOACUSTOMPRIVATE      extern __private(vf4soacustom)
#   elif defined(_WIN32)
#       define SOACUSTOMEXPORT       __declspec(dllexport)
#       define SOACUSTOMGLOBAL       extern __declspec(dllexport)
#       define SOACUSTOMPRIVATE      extern
#   else
#       define SOACUSTOMEXPORT
#       define SOACUSTOMGLOBAL       extern
#       define SOACUSTOMPRIVATE      extern
#   endif
#else
#   if defined(__lint)
#       define SOACUSTOMEXPORT       __export(vf4soacustom)
#       define SOACUSTOMGLOBAL       extern __global(vf4soacustom)
#   elif defined(_WIN32) && !defined(WNT_STATIC_LINK)
#       define SOACUSTOMEXPORT      __declspec(dllimport)
#       define SOACUSTOMGLOBAL       extern __declspec(dllimport)
#   else
#       define SOACUSTOMEXPORT
#       define SOACUSTOMGLOBAL       extern
#   endif
#endif
