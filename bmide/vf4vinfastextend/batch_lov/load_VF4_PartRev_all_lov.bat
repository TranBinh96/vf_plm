echo off
echo === START UPLOAD ===

echo === processing... VF4_scp_veh_variant_lov.bat
call VF4_scp_veh_variant_lov.bat


echo === processing... VF4_cuv_veh_variant_lov.bat
call VF4_cuv_veh_variant_lov.bat


echo === processing... VF4_ebus_veh_variant_lov.bat
call VF4_ebus_veh_variant_lov.bat


echo === processing... VF4_cuv_veh_type_lov.bat
call VF4_cuv_veh_type_lov.bat

echo === processing... VF4_donnor_veh_lov.bat
call VF4_donnor_veh_lov.bat

echo === processing... VF4_ebus_veh_type_lov.bat
call VF4_ebus_veh_type_lov.bat

echo === processing... VF4_cuv_mule_veh_type_lov.bat
call VF4_cuv_mule_veh_type_lov.bat


echo === END UPLOAD ===

