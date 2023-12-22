//@<COPYRIGHT>@
//==================================================
//Copyright $2023.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/** 
    @file 

    This file contains the declaration for the Dispatch Library  VF4_VinFastExtend

*/

#include <common/library_indicators.h>

#ifdef EXPORTLIBRARY
#define EXPORTLIBRARY something else
#error ExportLibrary was already defined
#endif

#define EXPORTLIBRARY            libVF4_VinFastExtend

#if !defined(LIBVF4_VINFASTEXTEND) && !defined(IPLIB)
#   error IPLIB or LIBVF4_VINFASTEXTEND is not defined
#endif

/* Handwritten code should use VF4_VINFASTEXTEND_API, not VF4_VINFASTEXTENDEXPORT */

#define VF4_VINFASTEXTEND_API VF4_VINFASTEXTENDEXPORT

#if IPLIB==libVF4_VinFastExtend || defined(LIBVF4_VINFASTEXTEND)
#   if defined(__lint)
#       define VF4_VINFASTEXTENDEXPORT       __export(VF4_VinFastExtend)
#       define VF4_VINFASTEXTENDGLOBAL       extern __global(VF4_VinFastExtend)
#       define VF4_VINFASTEXTENDPRIVATE      extern __private(VF4_VinFastExtend)
#   elif defined(_WIN32)
#       define VF4_VINFASTEXTENDEXPORT       __declspec(dllexport)
#       define VF4_VINFASTEXTENDGLOBAL       extern __declspec(dllexport)
#       define VF4_VINFASTEXTENDPRIVATE      extern
#   else
#       define VF4_VINFASTEXTENDEXPORT
#       define VF4_VINFASTEXTENDGLOBAL       extern
#       define VF4_VINFASTEXTENDPRIVATE      extern
#   endif
#else
#   if defined(__lint)
#       define VF4_VINFASTEXTENDEXPORT       __export(VF4_VinFastExtend)
#       define VF4_VINFASTEXTENDGLOBAL       extern __global(VF4_VinFastExtend)
#   elif defined(_WIN32) && !defined(WNT_STATIC_LINK)
#       define VF4_VINFASTEXTENDEXPORT      __declspec(dllimport)
#       define VF4_VINFASTEXTENDGLOBAL       extern __declspec(dllimport)
#   else
#       define VF4_VINFASTEXTENDEXPORT
#       define VF4_VINFASTEXTENDGLOBAL       extern
#   endif
#endif
