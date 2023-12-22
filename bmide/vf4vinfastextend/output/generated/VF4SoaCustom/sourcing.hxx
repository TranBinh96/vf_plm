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
#ifndef TEAMCENTER_SERVICES_CUSTOM_SOURCING_HXX 
#define TEAMCENTER_SERVICES_CUSTOM_SOURCING_HXX


#include <sourcing1906.hxx>
#include <sourcing2010.hxx>
#include <sourcing2012.hxx>


namespace VF4
{
    namespace Soa
    {
        namespace Custom
        {
            class Sourcing;
        }
    }
}


/**
 * Sourcing Services
 * <br>
 * <br>
 * <br>
 * <b>Library Reference:</b>
 * <ul>
 * <li type="disc">libvf4soacustom.dll
 * </li>
 * </ul>
 */

class VF4::Soa::Custom::Sourcing
    : public VF4::Soa::Custom::_2019_06::Sourcing,
             public VF4::Soa::Custom::_2020_10::Sourcing,
             public VF4::Soa::Custom::_2020_12::Sourcing
{};

#endif

