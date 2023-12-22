package com.teamcenter.vinfast.general.view;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.vf.utils.StringExtension;
import com.vf.utils.TCExtension;

public class ExchangeRate_Handler extends AbstractHandler {
	private TCSession session;
	private ExchangeRate_Dialog dlg;
	private LinkedHashMap<String, Double> rateList;
	private boolean usdBase = true;

	public ExchangeRate_Handler() {
		super();
	}

	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		try {
			session = (TCSession) AIFUtility.getCurrentApplication().getSession();

			// Init data
			String[] currencyRate = TCExtension.GetPreferenceValues("VF_CURRENCY_RATE", session);
			rateList = new LinkedHashMap<String, Double>();

			// Init UI
			dlg = new ExchangeRate_Dialog(new Shell());
			dlg.create();

			for (String rate : currencyRate) {
				if (rate.contains(";")) {
					String[] str = rate.split(";");
					if (str.length > 2) {
						if (StringExtension.isDouble(str[2])) {
							rateList.put(str[1], Double.parseDouble(str[2]));
							TableItem row = new TableItem(dlg.tblRate, SWT.NONE);
							row.setText(new String[] { str[0], str[1], str[2] });
						}
					}
				}
			}

			dlg.cbCurrencyFrom.setItems(rateList.keySet().toArray(new String[0]));
			dlg.cbCurrencyTo.setItems(rateList.keySet().toArray(new String[0]));

			dlg.tblRate.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					TableItem[] selection = dlg.tblRate.getSelection();
					if (selection.length > 0) {
						StringSelection stringSelection = new StringSelection(selection[0].getText(2));
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(stringSelection, null);
					}
				}
			});

			dlg.tblRate.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					TableItem[] selection = dlg.tblRate.getSelection();
					if (selection.length > 0) {
						String rate = selection[0].getText(1);
						if (!rate.isEmpty()) {
							if (usdBase)
								dlg.cbCurrencyFrom.setText(rate);
							else
								dlg.cbCurrencyTo.setText(rate);
						}
					}
				}
			});

			dlg.btnCalculate.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					convertRate();

				}
			});

			dlg.btnSwitch.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					usdBase = !usdBase;
					if (usdBase) {
						dlg.cbCurrencyFrom.setText(dlg.cbCurrencyTo.getText());
						dlg.cbCurrencyTo.setText("USD");
					} else {
						dlg.cbCurrencyTo.setText(dlg.cbCurrencyFrom.getText());
						dlg.cbCurrencyFrom.setText("USD");
					}
					dlg.cbCurrencyFrom.setEnabled(usdBase);
					dlg.cbCurrencyTo.setEnabled(!usdBase);
				}
			});

			dlg.cbCurrencyFrom.setText("USD");
			dlg.cbCurrencyTo.setText("USD");
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void convertRate() {
		dlg.txtResult.setText("");

		if (dlg.cbCurrencyFrom.getText().isEmpty())
			return;

		if (dlg.cbCurrencyTo.getText().isEmpty())
			return;

		String amount = dlg.txtAmount.getText();
		if (!StringExtension.isDouble(amount))
			return;

		Double rate1 = rateList.get(dlg.cbCurrencyFrom.getText());
		Double rate2 = rateList.get(dlg.cbCurrencyTo.getText());
		Double result = Double.parseDouble(amount) / rate1 * rate2;
		NumberFormat formatter = new DecimalFormat("#0.000");
		dlg.txtResult.setText(formatter.format(result));
	}
}
