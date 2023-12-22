//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/** 
    @file 

    This file contains the declaration for the Dispatch Library  vf4vinfastextenddispatch

*/

#include <common/library_indicators.h>

#ifdef EXPORTLIBRARY
#define EXPORTLIBRARY something else
#error ExportLibrary was already defined
#endif

#define EXPORTLIBRARY            libvf4vinfastextenddispatch

#if !defined(LIBVF4VINFASTEXTENDDISPATCH) && !defined(IPLIB)
#   error IPLIB or LIBVF4VINFASTEXTENDDISPATCH is not defined
#endif

/* Handwritten code should use VF4VINFASTEXTENDDISPATCH_API, not VF4VINFASTEXTENDDISPATCHEXPORT */

#define VF4VINFASTEXTENDDISPATCH_API VF4VINFASTEXTENDDISPATCHEXPORT

#if IPLIB==libvf4vinfastextenddispatch || defined(LIBVF4VINFASTEXTENDDISPATCH)
#   if defined(__lint)
#       define VF4VINFASTEXTENDDISPATCHEXPORT       __export(vf4vinfastextenddispatch)
#       define VF4VINFASTEXTENDDISPATCHGLOBAL       extern __global(vf4vinfastextenddispatch)
#       define VF4VINFASTEXTENDDISPATCHPRIVATE      extern __private(vf4vinfastextenddispatch)
#   elif defined(_WIN32)
#       define VF4VINFASTEXTENDDISPATCHEXPORT       __declspec(dllexport)
#       define VF4VINFASTEXTENDDISPATCHGLOBAL       extern __declspec(dllexport)
#       define VF4VINFASTEXTENDDISPATCHPRIVATE      extern
#   else
#       define VF4VINFASTEXTENDDISPATCHEXPORT
#       define VF4VINFASTEXTENDDISPATCHGLOBAL       extern
#       define VF4VINFASTEXTENDDISPATCHPRIVATE      extern
#   endif
#else
#   if defined(__lint)
#       define VF4VINFASTEXTENDDISPATCHEXPORT       __export(vf4vinfastextenddispatch)
#       define VF4VINFASTEXTENDDISPATCHGLOBAL       extern __global(vf4vinfastextenddispatch)
#   elif defined(_WIN32) && !defined(WNT_STATIC_LINK)
#       define VF4VINFASTEXTENDDISPATCHEXPORT      __declspec(dllimport)
#       define VF4VINFASTEXTENDDISPATCHGLOBAL       extern __declspec(dllimport)
#   else
#       define VF4VINFASTEXTENDDISPATCHEXPORT
#       define VF4VINFASTEXTENDDISPATCHGLOBAL       extern
#   endif
#endif
