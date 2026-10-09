-- SQL script to create invoices and invoice_status_history tables in Informix database.

CREATE TABLE dbadmin.invoices (
    id SERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    account_number VARCHAR(10) NOT NULL,
    area_code VARCHAR(2) NOT NULL,
    bill_cycle INTEGER NOT NULL,
    invoice_month VARCHAR(20) NOT NULL,
    issue_date DATE NOT NULL,
    region VARCHAR(50),
    folio_no INTEGER,
    sr_no VARCHAR(50),
    company_name VARCHAR(255) NOT NULL,
    project_name VARCHAR(255) NOT NULL,
    file_ref_no VARCHAR(50),
    reference_code VARCHAR(100),
    capacity_mw DECIMAL(10,3) NOT NULL,
    allowed_generation_mw DECIMAL(10,3) NOT NULL,
    present_reading_date DATE NOT NULL,
    previous_reading_date DATE NOT NULL,
    total_present_reading INTEGER NOT NULL,
    total_previous_reading INTEGER NOT NULL,
    multiply_factor DECIMAL(8,3) NOT NULL,
    energy_kwh DECIMAL(15,2) NOT NULL,
    eligible_energy_kwh DECIMAL(15,2) NOT NULL,
    period_of_generation_days INTEGER NOT NULL,
    plant_factor_percent DECIMAL(5,2) NOT NULL,
    energy_purchased_kwh DECIMAL(15,2) NOT NULL,
    rate_per_kwh DECIMAL(7,2) NOT NULL,
    cost_of_energy DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    prepared_by VARCHAR(50) NOT NULL,
    prepared_at DATETIME YEAR TO FRACTION(3) NOT NULL,
    approved_by VARCHAR(50),
    approved_at DATETIME YEAR TO FRACTION(3)
);

CREATE TABLE dbadmin.invoice_status_history (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL,
    status_from VARCHAR(20),
    status_to VARCHAR(20) NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    changed_at DATETIME YEAR TO FRACTION(3) NOT NULL,
    remarks LVARCHAR(1000),
    FOREIGN KEY (invoice_id) REFERENCES dbadmin.invoices(id) ON DELETE CASCADE
);
