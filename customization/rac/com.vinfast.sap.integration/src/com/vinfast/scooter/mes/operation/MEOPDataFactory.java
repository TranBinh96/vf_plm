package com.vinfast.scooter.mes.operation;

public class MEOPDataFactory {
	private MEOPDataFactory() {

	}

	private static MEOPDataFactory instance = new MEOPDataFactory();

	public static MEOPDataFactory getInstance() {
		return instance;
	}

	public static final MEOPDataAbstract getMEOPData(String rawOperationType) {
		if (rawOperationType.equalsIgnoreCase("Poke Yoke")) {
			return new MEOPDataPokeYoke();
		} else if (rawOperationType.equalsIgnoreCase("Traceability")) {
			return new MEOPDataTraceability();
		} else if (rawOperationType.equalsIgnoreCase("Part Verification")) {
			return new MEOPDataPartVerification();
		} else if (rawOperationType.equalsIgnoreCase("Screwing")) {
			return new MEOPDataScrewing();
		} else if (rawOperationType.equalsIgnoreCase("Filling")) {
			return new MEOPDataFilling();
		} else if (rawOperationType.equalsIgnoreCase("Automatic Consumption")) {
			return new MEOPDataAutomaticComsumption();
		} else if (rawOperationType.equalsIgnoreCase("ECU")) {
			return new MEOPDataEcu();
		} else if (rawOperationType.equalsIgnoreCase("Buy-Off")) {
			return new MEOPDataBuyOff();
		} else if (rawOperationType.equalsIgnoreCase("Machining")) {
			return new MEOPDataMachining();
		} else if (rawOperationType.equalsIgnoreCase("Group Traceability")) {
			return new MEOPDataGroupTraceability();
		} else {
			return new MEOPDataInvalid();
		}
	}
}
