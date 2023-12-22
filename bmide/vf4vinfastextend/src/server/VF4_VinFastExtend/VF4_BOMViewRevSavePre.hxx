//@<COPYRIGHT>@
//==================================================
//Copyright $2022.
//Siemens Product Lifecycle Management Software Inc.
//All Rights Reserved.
//==================================================
//@<COPYRIGHT>@

/* 
 * @file 
 *
 *   This file contains the declaration for the Extension VF4_BOMViewRevSavePre
 *
 */
 
#ifndef VF4_BOMVIEWREVSAVEPRE_HXX
#define VF4_BOMVIEWREVSAVEPRE_HXX
#include <tccore/method.h>
#include <VF4_VinFastExtend/libvf4_vinfastextend_exports.h>
#ifdef __cplusplus
         extern "C"{
#endif
                 
extern VF4_VINFASTEXTEND_API int VF4_BOMViewRevSavePre(METHOD_message_t* msg, va_list args);
                 
#ifdef __cplusplus
                   }
#endif
                
#include <VF4_VinFastExtend/libvf4_vinfastextend_undef.h>
                
#endif  // VF4_BOMVIEWREVSAVEPRE_HXX
