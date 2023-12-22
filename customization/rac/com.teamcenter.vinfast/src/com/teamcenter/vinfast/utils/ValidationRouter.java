package com.teamcenter.vinfast.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.common.NotDefinedException;

import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.TCExtension;
import com.vf.utils.VFRacDefine.ValidateKey;

public class ValidationRouter {
	public static IValidator getValidator(String program, ExecutionEvent event, LinkedHashMap<String, String> mapping) throws TCException, NotDefinedException, VFNotSupportException {
		IParameter cmdParamMainProgram = event.getCommand().getParameter("com.teamcenter.vinfast.commands.StartSourcing.isCOP");
		IParameter cmdParamIsMEPart = event.getCommand().getParameter("com.teamcenter.vinfast.commands.StartSourcing.isMEPart");
		String isCOPSourcingStr = (cmdParamMainProgram != null && cmdParamMainProgram.getName() != null) ? cmdParamMainProgram.getName() : "";
		String isMEPartStr = (cmdParamIsMEPart != null && cmdParamIsMEPart.getName() != null) ? cmdParamIsMEPart.getName() : "";

		IValidator validator = null;
		boolean isCOPSourcing = (isCOPSourcingStr.isEmpty() == false);
		boolean isMEPart = (isMEPartStr.isEmpty() == false);
		if (isCOPSourcing) {
			if (program.trim().contains("CUV")) {
				validator = new ValidatorCOP_CUV();
			}
		} else if (isMEPart) {
			validator = new ValidatorMEPart();
		} else {
			String validateKey = "";
			if (mapping.containsKey(program))
				validateKey = mapping.get(program);
			if (validateKey.compareToIgnoreCase((ValidateKey.CUV)) == 0) {
				validator = new ValidatorCUV();
			} else if (validateKey.compareToIgnoreCase((ValidateKey.BatteryCar)) == 0) {
				validator = new ValidatorBatteryCar();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.EScooter) == 0) {
				validator = new ValidatorEScooter();
			} else if (validateKey.compareToIgnoreCase((ValidateKey.DSUV)) == 0) {
				validator = new ValidatorDSUV();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.EBUS) == 0) {
				validator = new ValidatorEBUS();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.AFS) == 0) {
				validator = new ValidatorAFS();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.AFSEScooter) == 0) {
				validator = new ValidatorAFSEScooter();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.AFSMagna) == 0) {
				validator = new ValidatorAFSMagna();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.Cell) == 0) {
				validator = new ValidatorCell();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.EEComponent) == 0) {
				validator = new ValidatorEEComponent();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.SIC) == 0) {
				validator = new ValidatorSIC();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.EEComponent_Electric_Electronics) == 0) {
				validator = new ValidatorEEComponent_Electric_Electronics();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.MEPart) == 0) {
				validator = new ValidatorMEPart();
			} else if (validateKey.compareToIgnoreCase(ValidateKey.CAR) == 0) {
				validator = new ValidatorCar();
			}
		}
		if (validator == null) {
			throw new VFNotSupportException("Cannot find " + (isCOPSourcing ? "COP-Sourcing's" : "") + " validator for program " + program + "!");
		}

		return validator;
	}

	public static IValidator getValidator(String program, LinkedHashMap<String, String> mapping, boolean isMePart) throws TCException, NotDefinedException, VFNotSupportException {
		IValidator validator = null;
		String validateKey = "";
		if (mapping.containsKey(program))
			validateKey = mapping.get(program);
		if (validateKey.compareToIgnoreCase((ValidateKey.CUV)) == 0) {
			validator = new ValidatorCUV();
		} else if (validateKey.compareToIgnoreCase((ValidateKey.BatteryCar)) == 0) {
			validator = new ValidatorBatteryCar();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.EScooter) == 0) {
			validator = new ValidatorEScooter();
		} else if (validateKey.compareToIgnoreCase((ValidateKey.DSUV)) == 0) {
			validator = new ValidatorDSUV();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.EBUS) == 0) {
			validator = new ValidatorEBUS();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.AFS) == 0) {
			validator = new ValidatorAFS();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.AFSEScooter) == 0) {
			validator = new ValidatorAFSEScooter();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.AFSMagna) == 0) {
			validator = new ValidatorAFSMagna();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.Cell) == 0) {
			validator = new ValidatorCell();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.EEComponent) == 0) {
			validator = new ValidatorEEComponent();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.SIC) == 0) {
			validator = new ValidatorSIC();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.EEComponent_Electric_Electronics) == 0) {
			validator = new ValidatorEEComponent_Electric_Electronics();
		} else if (validateKey.compareToIgnoreCase(ValidateKey.CAR) == 0) {
			validator = new ValidatorCar();
		}

		if (validator == null) {
			throw new VFNotSupportException("Cannot find validator for program " + program + "!");
		}

		return validator;
	}

	public static LinkedHashMap<String, String> getProgramValidateMapping(TCSession session) {
		LinkedHashMap<String, String> mapping = new LinkedHashMap<>();
		String[] values = TCExtension.GetPreferenceValues("VF_SOURCING_PROGRAM_2_VALIDATE", session);
		if (values != null) {
			for (String value : values) {
				String[] str = value.split("==");
				if (str.length >= 2) {
					String validateKey = str[0];
					ArrayList<String> programList = new ArrayList<>(Arrays.asList(str[1].split(";")));
					for (String program : programList) {
						mapping.put(program, validateKey);
					}
				}
			}
		}

		return mapping;
	}
}
