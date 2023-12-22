package com.teamcenter.vinfast.model;

import com.teamcenter.rac.kernel.TCSession;

public class UpdateALForCreatePartValidate_Model {
	private TCSession session;
	private String module_1 = "";
	private String module_2 = "";
	private String module_3 = "";
	private boolean module_1_Validation = true;
	private boolean module_2_Validation = true;
	private boolean module_3_Validation = true;
	
	public UpdateALForCreatePartValidate_Model(TCSession _session) {
		session = _session;
	}

	public String getModule_1() {
		return module_1;
	}

	public void setModule_1(String module_1) {
		this.module_1 = module_1;
	}

	public String getModule_2() {
		return module_2;
	}

	public void setModule_2(String module_2) {
		this.module_2 = module_2;
	}

	public String getModule_3() {
		return module_3;
	}

	public void setModule_3(String input) {
		if (input.contains("\n")) {
			this.module_3 = input.replace("\\n", ",");
		}
		else {
			this.module_3 = input;
		}
	}
}
