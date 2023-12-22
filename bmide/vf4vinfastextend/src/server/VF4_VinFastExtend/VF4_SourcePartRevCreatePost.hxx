//@<COPYRIGHT>@
//==================================================
//Copyright $2021.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/* 
 * @file 
 *
 *   This file contains the declaration for the Extension VF4_SourcePartRevCreatePost
 *
 */
 
#ifndef VF4_SOURCEPARTREVCREATEPOST_HXX
#define VF4_SOURCEPARTREVCREATEPOST_HXX
#include <tccore/method.h>
#include <VF4_VinFastExtend/libvf4_vinfastextend_exports.h>

#include <string>

using namespace std;

#ifdef __cplusplus
         extern "C"{
#endif
                 
extern VF4_VINFASTEXTEND_API int VF4_SourcePartRevCreatePost(METHOD_message_t* msg, va_list args);
                 
#ifdef __cplusplus
                   }
#endif


#include <VF4_VinFastExtend/libvf4_vinfastextend_undef.h>




int handle (const tag_t &rev);

#endif  // VF4_SOURCEPARTREVCREATEPOST_HXX
