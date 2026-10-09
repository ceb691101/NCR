import React, { useMemo, useState } from 'react';
import { getDeveloperBySearch } from '../../services/developerRegistrationService';

const PAY_MODE_LABELS = {
  Q: 'Quarterly',
  C: 'Cash',
  M: 'Monthly'
};

const buildOptions = (values) => {
  const unique = Array.from(new Set(values.filter(Boolean)));
  return unique.sort();
};

const sanitizeText = (value) => value.trim();

const isValidDate = (value) => {
  if (!value) return false;
  const date = new Date(value);
  return !Number.isNaN(date.getTime());
};

export default function TempPaymentsFormModal({
  isOpen,
  onClose,
  onSubmit,
  loading,
  options
}) {
  const [form, setForm] = useState({
    agent_code: '',
    cent_code: '',
    folio_no: '',
    counter: '',
    lot: '',
    stub_no: '',
    actl_pay_date: '',
    credit_date: '',
    pay_mode: '',
    paid_amt: ''
  });
  const [errors, setErrors] = useState({});

  const agentOptions = useMemo(() => buildOptions(options.agentCodes), [options.agentCodes]);
  const centerOptions = useMemo(() => buildOptions(options.centerCodes), [options.centerCodes]);
  const counterOptions = useMemo(() => buildOptions(options.counters), [options.counters]);
  const payModeOptions = useMemo(() => buildOptions(options.payModes), [options.payModes]);

  if (!isOpen) return null;

  const updateField = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    const nextErrors = {};

    const agentCode = sanitizeText(form.agent_code);
    const centCode = sanitizeText(form.cent_code);
    const folioNo = sanitizeText(form.folio_no);
    const counter = sanitizeText(form.counter);
    const lot = sanitizeText(form.lot);
    const stubNo = sanitizeText(form.stub_no);
    const payMode = sanitizeText(form.pay_mode);
    const paidAmt = sanitizeText(form.paid_amt);

    if (!agentCode) nextErrors.agent_code = 'Agent Code is required.';
    if (!centCode) nextErrors.cent_code = 'Center Code is required.';
    if (!folioNo) nextErrors.folio_no = 'Folio Number is required.';
    if (!counter) nextErrors.counter = 'Counter is required.';
    if (!lot) nextErrors.lot = 'Lot is required.';
    if (!stubNo) nextErrors.stub_no = 'Stub No is required.';
    if (!form.actl_pay_date || !isValidDate(form.actl_pay_date)) {
      nextErrors.actl_pay_date = 'Actual Pay Date is required.';
    }
    if (!form.credit_date || !isValidDate(form.credit_date)) {
      nextErrors.credit_date = 'Credit Date is required.';
    }
    if (!payMode) nextErrors.pay_mode = 'Pay Mode is required.';
    if (!paidAmt) nextErrors.paid_amt = 'Amount is required.';

    if (agentCode && !/^[A-Za-z0-9]{1,4}$/.test(agentCode)) {
      nextErrors.agent_code = 'Agent Code must be 1-4 letters or numbers.';
    }
    if (centCode && !/^[A-Za-z0-9]{1,3}$/.test(centCode)) {
      nextErrors.cent_code = 'Center Code must be 1-3 letters or numbers.';
    }
    if (folioNo && !/^[0-9]{1,10}$/.test(folioNo)) {
      nextErrors.folio_no = 'Folio Number must be a valid number.';
    }
    if (counter && !/^[A-Za-z0-9]{1,3}$/.test(counter)) {
      nextErrors.counter = 'Counter must be 1-3 letters or numbers.';
    }
    if (lot && !/^[A-Za-z0-9-]{1,2}$/.test(lot)) {
      nextErrors.lot = 'Lot must be 1-2 letters, numbers, or -.';
    }
    if (stubNo && (!/^[0-9]{1,5}$/.test(stubNo) || Number(stubNo) < 0)) {
      nextErrors.stub_no = 'Stub No must be a positive integer.';
    }
    if (payMode && !/^[A-Za-z]{1}$/.test(payMode)) {
      nextErrors.pay_mode = 'Pay Mode must be a single letter.';
    }
    if (paidAmt && (Number.isNaN(Number(paidAmt)) || Number(paidAmt) <= 0)) {
      nextErrors.paid_amt = 'Amount must be greater than 0.';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) return;

    let targetAccNbr = '';
    try {
      const dev = await getDeveloperBySearch('folio_no', sanitizeText(form.folio_no));
      if (dev && (dev.acc_nbr || dev.accNbr)) {
        targetAccNbr = dev.acc_nbr || dev.accNbr;
      }
    } catch (e) {
      console.warn("Could not resolve developer by folio_no:", e);
    }

    if (!targetAccNbr) {
      setErrors((prev) => ({ ...prev, folio_no: `Developer with Folio Number ${form.folio_no} not found.` }));
      return;
    }

    const payload = {
      agent_code: sanitizeText(form.agent_code).toUpperCase(),
      cent_code: sanitizeText(form.cent_code).toUpperCase(),
      acc_nbr: targetAccNbr,
      counter: sanitizeText(form.counter).toUpperCase(),
      lot: sanitizeText(form.lot).toUpperCase(),
      stub_no: Number(form.stub_no),
      actl_pay_date: form.actl_pay_date,
      credit_date: form.credit_date,
      pay_mode: sanitizeText(form.pay_mode).toUpperCase(),
      paid_amt: Number(form.paid_amt).toFixed(2)
    };

    const success = await onSubmit(payload);
    if (success) {
      setForm({
        agent_code: '',
        cent_code: '',
        folio_no: '',
        counter: '',
        lot: '',
        stub_no: '',
        actl_pay_date: '',
        credit_date: '',
        pay_mode: '',
        paid_amt: ''
      });
      setErrors({});
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 px-4">
      <div className="bg-white w-full max-w-4xl rounded-lg shadow-lg">
        <div className="flex items-center justify-between border-b px-6 py-4">
          <h2 className="text-xl font-semibold text-ink-800">Add Temporary Payment</h2>
          <button
            type="button"
            onClick={onClose}
            className="text-ink-500 hover:text-ink-700"
            aria-label="Close"
            disabled={loading}
          >
            <i className="fas fa-times"></i>
          </button>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="ds-label-field">Agent Code</label>
              <input
                list="agent-code-options"
                value={form.agent_code}
                onChange={(e) => updateField('agent_code', e.target.value)}
                className="ds-input"
                placeholder="e.g., CEBB"
                maxLength={4}
                required
              />
              <datalist id="agent-code-options">
                {agentOptions.map((value) => (
                  <option key={value} value={value} />
                ))}
              </datalist>
              {errors.agent_code && <p className="text-sm text-critical-600 mt-1">{errors.agent_code}</p>}
            </div>

            <div>
              <label className="ds-label-field">Center Code</label>
              <input
                list="center-code-options"
                value={form.cent_code}
                onChange={(e) => updateField('cent_code', e.target.value)}
                className="ds-input"
                placeholder="e.g., QB0"
                maxLength={3}
                required
              />
              <datalist id="center-code-options">
                {centerOptions.map((value) => (
                  <option key={value} value={value} />
                ))}
              </datalist>
              {errors.cent_code && <p className="text-sm text-critical-600 mt-1">{errors.cent_code}</p>}
            </div>

            <div>
              <label className="ds-label-field">Folio Number</label>
              <input
                value={form.folio_no}
                onChange={(e) => updateField('folio_no', e.target.value)}
                className="ds-input"
                placeholder="e.g., 1081"
                maxLength={10}
                required
              />
              {errors.folio_no && <p className="text-sm text-critical-600 mt-1">{errors.folio_no}</p>}
            </div>

            <div>
              <label className="ds-label-field">Counter</label>
              <input
                list="counter-options"
                value={form.counter}
                onChange={(e) => updateField('counter', e.target.value)}
                className="ds-input"
                placeholder="e.g., BB"
                maxLength={3}
                required
              />
              <datalist id="counter-options">
                {counterOptions.map((value) => (
                  <option key={value} value={value} />
                ))}
              </datalist>
              {errors.counter && <p className="text-sm text-critical-600 mt-1">{errors.counter}</p>}
            </div>

            <div>
              <label className="ds-label-field">Lot</label>
              <input
                value={form.lot}
                onChange={(e) => updateField('lot', e.target.value)}
                className="ds-input"
                placeholder="e.g., -"
                maxLength={2}
                required
              />
              {errors.lot && <p className="text-sm text-critical-600 mt-1">{errors.lot}</p>}
            </div>

            <div>
              <label className="ds-label-field">Stub No</label>
              <input
                type="number"
                min="0"
                value={form.stub_no}
                onChange={(e) => updateField('stub_no', e.target.value)}
                className="ds-input"
                placeholder="e.g., 1"
                required
              />
              {errors.stub_no && <p className="text-sm text-critical-600 mt-1">{errors.stub_no}</p>}
            </div>

            <div>
              <label className="ds-label-field">Actual Pay Date</label>
              <input
                type="date"
                value={form.actl_pay_date}
                onChange={(e) => updateField('actl_pay_date', e.target.value)}
                className="ds-input"
                required
              />
              {errors.actl_pay_date && <p className="text-sm text-critical-600 mt-1">{errors.actl_pay_date}</p>}
            </div>

            <div>
              <label className="ds-label-field">Credit Date</label>
              <input
                type="date"
                value={form.credit_date}
                onChange={(e) => updateField('credit_date', e.target.value)}
                className="ds-input"
                required
              />
              {errors.credit_date && <p className="text-sm text-critical-600 mt-1">{errors.credit_date}</p>}
            </div>

            <div>
              <label className="ds-label-field">Pay Mode</label>
              <select
                value={form.pay_mode}
                onChange={(e) => updateField('pay_mode', e.target.value)}
                className="ds-input"
                required
              >
                <option value="">Select Pay Mode</option>
                {payModeOptions.map((value) => (
                  <option key={value} value={value}>
                    {value} {PAY_MODE_LABELS[value] ? `- ${PAY_MODE_LABELS[value]}` : ''}
                  </option>
                ))}
                {!payModeOptions.includes('Q') && <option value="Q">Q - Quarterly</option>}
                {!payModeOptions.includes('C') && <option value="C">C - Cash</option>}
                {!payModeOptions.includes('M') && <option value="M">M - Monthly</option>}
              </select>
              {errors.pay_mode && <p className="text-sm text-critical-600 mt-1">{errors.pay_mode}</p>}
            </div>

            <div>
              <label className="ds-label-field">Amount</label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={form.paid_amt}
                onChange={(e) => updateField('paid_amt', e.target.value)}
                className="ds-input"
                placeholder="e.g., 3003035.70"
                required
              />
              {errors.paid_amt && <p className="text-sm text-critical-600 mt-1">{errors.paid_amt}</p>}
            </div>
          </div>

          <div className="mt-6 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-ink-200 text-ink-700 rounded-md hover:bg-ink-300"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2 bg-navy-600 text-white rounded-md hover:bg-navy-700 disabled:bg-ink-400"
              disabled={loading}
            >
              {loading ? 'Saving...' : 'Save Payment'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
