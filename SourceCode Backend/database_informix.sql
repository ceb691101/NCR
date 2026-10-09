
CREATE TABLE dbadmin.provinces (
    prov_code CHAR(2) NOT NULL,
    prov_name CHAR(20) NOT NULL,
    PRIMARY KEY (prov_code)
);
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('1', 'WPN');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('2', 'Western South I');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('3', 'Colombo City');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('4', 'NP');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('5', 'Central');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('6', 'Uva');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('7', 'Eastern');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('8', 'North Western');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('9', 'Sabaragamuwa');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('A', 'NCP');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('B', 'Southern Province');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('C', 'WPSII');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('D', 'NWP2');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('E', 'CP2');
INSERT INTO dbadmin.provinces (prov_code, prov_name) VALUES ('F', 'SP2');

CREATE TABLE dbadmin.areas (
    area_code CHAR(2) NOT NULL,
    prov_code CHAR(2) NOT NULL,
    area_name CHAR(20) NOT NULL,
    address_l1 VARCHAR(40,0),
    address_l2 VARCHAR(40,0),
    address_l3 VARCHAR(25,0),
    ar_tel_1 VARCHAR(12,0),
    ar_tel_2 VARCHAR(12,0),
    ar_tel_3 VARCHAR(12,0),
    ar_fax_1 VARCHAR(13,0),
    ar_fax_2 VARCHAR(13,0),
    ar_email VARCHAR(75,0),
    region CHAR(2),
    ar_email_2 VARCHAR(75,0),
    PRIMARY KEY (area_code),
    FOREIGN KEY (prov_code) REFERENCES dbadmin.provinces(prov_code)
);
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('01', '3', 'Colombo North', 'No. 123, Main Street', 'Colombo North Office', 'Colombo', '0112345678', '0112345679', '0771234567', '0112345680', '0112345681', 'colombonorth@office.lk', 'R1', 'admin@colombonorth.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('02', '3', 'Colombo East', 'No. 456, East Road', 'Colombo East Branch', 'Colombo', '0112456789', '0112456780', '0771234568', '0112456781', '0112456782', 'colomboeast@office.lk', 'R1', 'admin@colomboeast.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('03', '3', 'Colombo South', 'No. 789, South Avenue', 'Colombo South Office', 'Colombo', '0112567890', '0112567891', '0771234569', '0112567892', '0112567893', 'colombosouth@office.lk', 'R1', 'admin@colombosouth.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('04', '3', 'Colombo West', 'No. 321, West Lane', 'Colombo West Branch', 'Colombo', '0112678901', '0112678902', '0771234570', '0112678903', '0112678904', 'colombowest@office.lk', 'R1', 'admin@colombowest.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('27', '1', 'Ja Ela', 'No. 147, Negombo Road', 'Ja Ela Office', 'Ja Ela', '0112789012', '0112789013', '0771234571', '0112789014', '0112789015', 'jaela@office.lk', 'R2', 'admin@jaela.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('48', '1', 'Kelaniya', 'No. 258, Temple Road', 'Kelaniya Branch', 'Kelaniya', '0112890123', '0112890124', '0771234572', '0112890125', '0112890126', 'kelaniya@office.lk', 'R2', 'admin@kelaniya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('49', '1', 'Gampaha', 'No. 369, Central Road', 'Gampaha Office', 'Gampaha', '0112901234', '0112901235', '0771234573', '0112901236', '0112901237', 'gampaha@office.lk', 'R2', 'admin@gampaha.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('53', '1', 'Veyangoda', 'No. 741, Station Road', 'Veyangoda Branch', 'Veyangoda', '0113012345', '0113012346', '0771234574', '0113012347', '0113012348', 'veyangoda@office.lk', 'R2', 'admin@veyangoda.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('37', '1', 'Negombo', 'No. 852, Beach Road', 'Negombo Office', 'Negombo', '0113123456', '0113123457', '0771234575', '0113123458', '0113123459', 'negombo@office.lk', 'R2', 'admin@negombo.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('43', 'D', 'Kurunegala', 'No. 963, Clock Tower Road', 'Kurunegala Branch', 'Kurunegala', '0113234567', '0113234568', '0771234576', '0113234569', '0113234570', 'kurunegala@office.lk', 'R1', 'admin@kurunegala.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('21', '2', 'Ratmalana', 'No. 174, Airport Road', 'Ratmalana Office', 'Ratmalana', '0113345678', '0113345679', '0771234577', '0113345680', '0113345681', 'ratmalana@office.lk', 'R4', 'admin@ratmalana.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('42', 'C', 'Sri Jayawardana Pura', 'No. 285, University Road', 'SJP Branch', 'Sri J Pura', '0113456789', '0113456790', '0771234578', '0113456791', '0113456792', 'sjp@office.lk', 'R3', 'admin@sjp.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('38', '2', 'Dehiwala', 'No. 396, Galle Road', 'Dehiwala Office', 'Dehiwala', '0113567890', '0113567891', '0771234579', '0113567892', '0113567893', 'dehiwala@office.lk', 'R4', 'admin@dehiwala.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('46', 'C', 'Avissawella', 'No. 507, Main Street', 'Avissawella Branch', 'Avissawella', '0113678901', '0113678902', '0771234580', '0113678903', '0113678904', 'avissawella@office.lk', 'R3', 'admin@avissawella.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('31', 'C', 'Horana', 'No. 618, Panadura Road', 'Horana Office', 'Horana', '0113789012', '0113789013', '0771234581', '0113789014', '0113789015', 'horana@office.lk', 'R3', 'admin@horana.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('41', 'C', 'Homagama', 'No. 729, High Level Road', 'Homagama Branch', 'Homagama', '0113890123', '0113890124', '0771234582', '0113890125', '0113890126', 'homagama@office.lk', 'R3', 'admin@homagama.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('44', '2', 'Kalutara', 'No. 830, Galle Road', 'Kalutara Office', 'Kalutara', '0113901234', '0113901235', '0771234583', '0113901236', '0113901237', 'kalutara@office.lk', 'R4', 'admin@kalutara.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('59', '8', 'Kuliyapitiya', 'No. 941, Kandy Road', 'Kuliyapitiya Branch', 'Kuliyapitiya', '0114012345', '0114012346', '0771234584', '0114012347', '0114012348', 'kuliyapitiya@office.lk', 'R1', 'admin@kuliyapitiya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('50', '8', 'Wennappuwa', 'No. 152, Puttalam Road', 'Wennappuwa Office', 'Wennappuwa', '0114123456', '0114123457', '0771234585', '0114123458', '0114123459', 'wennappuwa@office.lk', 'R1', 'admin@wennappuwa.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('76', 'D', 'Wariyapola', 'No. 263, Market Street', 'Wariyapola Branch', 'Wariyapola', '0114234567', '0114234568', '0771234586', '0114234569', '0114234570', 'wariyapola@office.lk', 'R1', 'admin@wariyapola.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('19', '8', 'Chilaw', 'No. 374, Coconut Road', 'Chilaw Office', 'Chilaw', '0114345678', '0114345679', '0771234587', '0114345680', '0114345681', 'chilaw@office.lk', 'R1', 'admin@chilaw.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('77', '5', 'Kandy City', 'No. 485, Peradeniya Road', 'Kandy City Branch', 'Kandy', '0114456789', '0114456790', '0771234588', '0114456791', '0114456792', 'kandycity@office.lk', 'R2', 'admin@kandycity.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('17', 'E', 'Peradeniya', 'No. 596, Botanic Road', 'Peradeniya Office', 'Peradeniya', '0114567890', '0114567891', '0771234589', '0114567892', '0114567893', 'peradeniya@office.lk', 'R2', 'admin@peradeniya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('22', 'E', 'Nawalapitiya', 'No. 607, Hill Street', 'Nawalapitiya Branch', 'Nawalapitiya', '0114678901', '0114678902', '0771234590', '0114678903', '0114678904', 'nawalapitiya@office.lk', 'R2', 'admin@nawalapitiya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('30', '5', 'Matale', 'No. 718, Temple Street', 'Matale Office', 'Matale', '0114789012', '0114789013', '0771234591', '0114789014', '0114789015', 'matale@office.lk', 'R2', 'admin@matale.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('35', 'E', 'Nuwaraeliya', 'No. 829, Queen Street', 'Nuwaraeliya Branch', 'Nuwaraeliya', '0114890123', '0114890124', '0771234592', '0114890125', '0114890126', 'nuwaraeliya@office.lk', 'R2', 'admin@nuwaraeliya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('40', '5', 'Kundasale', 'No. 930, Digana Road', 'Kundasale Office', 'Kundasale', '0114901234', '0114901235', '0771234593', '0114901236', '0114901237', 'kundasale@office.lk', 'R2', 'admin@kundasale.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('71', '5', 'Katugastota', 'No. 141, Colombo Street', 'Katugastota Branch', 'Katugastota', '0115012345', '0115012346', '0771234594', '0115012347', '0115012348', 'katugastota@office.lk', 'R2', 'admin@katugastota.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('29', 'A', 'Anuradhapura', 'No. 252, Sacred City Road', 'Anuradhapura Office', 'Anuradhapura', '0115123456', '0115123457', '0771234595', '0115123458', '0115123459', 'anuradhapura@office.lk', 'R1', 'admin@anuradhapura.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('78', 'A', 'Kekirawa', 'No. 363, Tank Road', 'Kekirawa Branch', 'Kekirawa', '0115234567', '0115234568', '0771234596', '0115234569', '0115234570', 'kekirawa@office.lk', 'R1', 'admin@kekirawa.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('33', 'A', 'Minneriya', 'No. 474, Park Avenue', 'Minneriya Office', 'Minneriya', '0115345678', '0115345679', '0771234597', '0115345680', '0115345681', 'minneriya@office.lk', 'R1', 'admin@minneriya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('45', 'B', 'Galle', 'No. 585, Fort Street', 'Galle Branch', 'Galle', '0115456789', '0115456790', '0771234598', '0115456791', '0115456792', 'galle@office.lk', 'R4', 'admin@galle.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('47', 'F', 'Matara', 'No. 696, Beach Road', 'Matara Office', 'Matara', '0115567890', '0115567891', '0771234599', '0115567892', '0115567893', 'matara@office.lk', 'R4', 'admin@matara.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('25', 'F', 'Hambantota', 'No. 707, Harbor Road', 'Hambantota Branch', 'Hambantota', '0115678901', '0115678902', '0771234600', '0115678903', '0115678904', 'hambantota@office.lk', 'R4', 'admin@hambantota.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('28', 'B', 'Ambalangoda', 'No. 818, Coastal Road', 'Ambalangoda Office', 'Ambalangoda', '0115789012', '0115789013', '0771234601', '0115789014', '0115789015', 'ambalangoda@office.lk', 'R4', 'admin@ambalangoda.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('54', 'B', 'Akuressa', 'No. 929, Town Road', 'Akuressa Branch', 'Akuressa', '0115890123', '0115890124', '0771234602', '0115890125', '0115890126', 'akuressa@office.lk', 'R4', 'admin@akuressa.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('79', '9', 'Ruwanwella', 'No. 130, Avissawella Road', 'Ruwanwella Office', 'Ruwanwella', '0115901234', '0115901235', '0771234603', '0115901236', '0115901237', 'ruwanwella@office.lk', 'R3', 'admin@ruwanwella.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('52', '9', 'Kahawatta', 'No. 241, Estate Road', 'Kahawatta Branch', 'Kahawatta', '0116012345', '0116012346', '0771234604', '0116012347', '0116012348', 'kahawatta@office.lk', 'R3', 'admin@kahawatta.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('26', '9', 'Ratnapura', 'No. 352, Gem Road', 'Ratnapura Office', 'Ratnapura', '0116123456', '0116123457', '0771234605', '0116123458', '0116123459', 'ratnapura@office.lk', 'R3', 'admin@ratnapura.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('20', 'E', 'Kegalle', 'No. 463, Colombo Road', 'Kegalle Branch', 'Kegalle', '0116234567', '0116234568', '0771234606', '0116234569', '0116234570', 'kegalle@office.lk', 'R2', 'admin@kegalle.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('23', '6', 'Diyatalawa', 'No. 574, Military Road', 'Diyatalawa Office', 'Diyatalawa', '0116345678', '0116345679', '0771234607', '0116345680', '0116345681', 'diyatalawa@office.lk', 'R3', 'admin@diyatalawa.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('36', '6', 'Badulla', 'No. 685, Station Road', 'Badulla Branch', 'Badulla', '0116456789', '0116456790', '0771234608', '0116456791', '0116456792', 'badulla@office.lk', 'R3', 'admin@badulla.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('16', '4', 'Jaffna', 'No. 796, Hospital Street', 'Jaffna Office', 'Jaffna', '0116567890', '0116567891', '0771234609', '0116567892', '0116567893', 'jaffna@office.lk', 'R1', 'admin@jaffna.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('18', '4', 'Kilinochchi', 'No. 807, Main Street', 'Kilinochchi Branch', 'Kilinochchi', '0116678901', '0116678902', '0771234610', '0116678903', '0116678904', 'kilinochchi@office.lk', 'R1', 'admin@kilinochchi.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('24', '7', 'Ampara', 'No. 918, Kandy Road', 'Ampara Office', 'Ampara', '0116789012', '0116789013', '0771234611', '0116789014', '0116789015', 'ampara@office.lk', 'R2', 'admin@ampara.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('32', '7', 'Batticaloa', 'No. 129, Lagoon Road', 'Batticaloa Branch', 'Batticaloa', '0116890123', '0116890124', '0771234612', '0116890125', '0116890126', 'batticaloa@office.lk', 'R2', 'admin@batticaloa.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('34', '7', 'Trincomalee', 'No. 230, Harbor Street', 'Trincomalee Office', 'Trincomalee', '0116901234', '0116901235', '0771234613', '0116901236', '0116901237', 'trincomalee@office.lk', 'R2', 'admin@trincomalee.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('69', '7', 'Kalmunai', 'No. 341, Beach Road', 'Kalmunai Branch', 'Kalmunai', '0117012345', '0117012346', '0771234614', '0117012347', '0117012348', 'kalmunai@office.lk', 'R2', 'admin@kalmunai.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('61', 'E', 'Ginigathhena', 'No. 452, Hill Road', 'Ginigathhena Office', 'Ginigathhena', '0117123456', '0117123457', '0771234615', '0117123458', '0117123459', 'ginigathhena@office.lk', 'R2', 'admin@ginigathhena.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('39', '6', 'Monaragala', 'No. 563, Wellawaya Road', 'Monaragala Branch', 'Monaragala', '0117234567', '0117234568', '0771234616', '0117234569', '0117234570', 'monaragala@office.lk', 'R3', 'admin@monaragala.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('66', '1', 'Divulapitiya', 'No. 896, Nattandiya Road', 'Divulapitiya Office', 'Divulapitiya', '0117567890', '0117567891', '0771234619', '0117567892', '0117567893', 'divulapitiya@office.lk', 'R2', 'admin@divulapitiya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('62', 'F', 'Tangalle', 'No. 107, Matara Road', 'Tangalle Branch', 'Tangalle', '0117678901', '0117678902', '0771234620', '0117678903', '0117678904', 'tangalle@office.lk', 'R4', 'admin@tangalle.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('63', '9', 'Eheliyagoda', 'No. 218, Ratnapura Road', 'Eheliyagoda Office', 'Eheliyagoda', '0117789012', '0117789013', '0771234621', '0117789014', '0117789015', 'eheliyagoda@office.lk', 'R3', 'admin@eheliyagoda.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('64', '9', 'Embilipitiya', 'No. 329, Nonagama Road', 'Embilipitiya Branch', 'Embilipitiya', '0117890123', '0117890124', '0771234622', '0117890125', '0117890126', 'embilipitiya@office.lk', 'R3', 'admin@embilipitiya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('57', '4', 'Vavuniya', 'No. 430, Mannar Road', 'Vavuniya Office', 'Vavuniya', '0117901234', '0117901235', '0771234623', '0117901236', '0117901237', 'vavuniya@office.lk', 'R1', 'admin@vavuniya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('65', 'C', 'Bandaragama', 'No. 541, Horana Road', 'Bandaragama Office', 'Bandaragama', '0118012345', '0118012346', '0771234624', '0118012347', '0118012348', 'bandaragama@office.lk', 'R3', 'admin@bandaragama.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('80', '5', 'Galagedara', 'No. 652, Nawalapitiya Road', 'Galagedara Branch', 'Galagedara', '0118123456', '0118123457', '0771234625', '0118123458', '0118123459', 'galagedara@office.lk', 'R2', 'admin@galagedara.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('82', 'E', 'Mawanella', 'No. 763, Kegalle Road', 'Mawanella Office', 'Mawanella', '0118234567', '0118234568', '0771234626', '0118234569', '0118234570', 'mawanella@office.lk', 'R2', 'admin@mawanella.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('83', '8', 'Puttalama', 'No. 874, Chilaw Road', 'Puttalama Branch', 'Puttalama', '0118345678', '0118345679', '0771234627', '0118345680', '0118345681', 'puttalama@office.lk', 'R1', 'admin@puttalama.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('85', '5', 'Dambulla', 'No. 985, Anuradhapura Road', 'Dambulla Office', 'Dambulla', '0118456789', '0118456790', '0771234628', '0118456791', '0118456792', 'dambulla@office.lk', 'R2', 'admin@dambulla.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('84', 'D', 'Narammala', 'No. 196, Kurunegala Road', 'Narammala Branch', 'Narammala', '0118567890', '0118567891', '0771234629', '0118567892', '0118567893', 'narammala@office.lk', 'R1', 'admin@narammala.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('86', 'B', 'Baddegama', 'No. 207, Galle Road', 'Baddegama Office', 'Baddegama', '0118678901', '0118678902', '0771234630', '0118678903', '0118678904', 'baddegama@office.lk', 'R4', 'admin@baddegama.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('87', '2', 'Matugama', 'No. 318, Aluthgama Road', 'Matugama Branch', 'Matugama', '0118789012', '0118789013', '0771234631', '0118789014', '0118789015', 'matugama@office.lk', 'R4', 'admin@matugama.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('88', '6', 'Mahiyanganaya', 'No. 429, Badulla Road', 'Mahiyanganaya Office', 'Mahiyanganaya', '0118890123', '0118890124', '0771234632', '0118890125', '0118890126', 'mahiyanganaya@office.lk', 'R3', 'admin@mahiyanganaya.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('89', 'D', 'Mahawa', 'No. 530, Wariyapola Road', 'Mahawa Branch', 'Mahawa', '0118901234', '0118901235', '0771234633', '0118901236', '0118901237', 'mahawa@office.lk', 'R1', 'admin@mahawa.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('55', '1', 'Kirindiwela', 'No. 641, Gampaha Road', 'Kirindiwela Office', 'Kirindiwela', '0119012345', '0119012346', '0771234634', '0119012347', '0119012348', 'kirindiwela@office.lk', 'R2', 'admin@kirindiwela.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('51', 'E', 'Hanguranketha', 'No. 752, Kandy Road', 'Hanguranketha Branch', 'Hanguranketha', '0119123456', '0119123457', '0771234635', '0119123458', '0119123459', 'hanguranketha@office.lk', 'R2', 'admin@hanguranketha.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('56', '7', 'Valaichchenai', 'No. 863, Batticaloa Road', 'Valaichchenai Office', 'Valaichchenai', '0119234567', '0119234568', '0771234636', '0119234569', '0119234570', 'valaichchenai@office.lk', 'R2', 'admin@valaichchenai.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('58', '4', 'Jaffna East', 'No. 974, Point Pedro Road', 'Jaffna East Branch', 'Jaffna', '0119345678', '0119345679', '0771234637', '0119345680', '0119345681', 'jaffnaeast@office.lk', 'R1', 'admin@jaffnaeast.lk');
INSERT INTO dbadmin.areas (area_code, prov_code, area_name, address_l1, address_l2, address_l3, ar_tel_1, ar_tel_2, ar_tel_3, ar_fax_1, ar_fax_2, ar_email, region, ar_email_2)  VALUES ('60', '6', 'Wellawaya', 'No. 185, Monaragala Road', 'Wellawaya Office', 'Wellawaya', '0119456789', '0119456790', '0771234638', '0119456791', '0119456792', 'wellawaya@office.lk', 'R3', 'admin@wellawaya.lk');

CREATE TABLE dbadmin.log_file (
    pro_code VARCHAR(10,0),
    area_code CHAR(2),
    date_time DATETIME YEAR TO FRACTION(3),
    bill_cycle SMALLINT,
    no_of_recs SMALLINT,
    start_time VARCHAR(10,0),
    end_time VARCHAR(10,0),
    duration_min SMALLINT,
    user_id CHAR(20)
);
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (1, '8.01', '01', '2025-09-02 20:06:00.000', 444, 3, '20:06:41', '0:00:00', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (2, '8.01', '02', '2025-09-02 20:07:00.000', 444, 8, '20:07:29', '0:00:00', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (3, '1.04', '03', '2025-09-03 13:59:00.000', 444, 0, '13:59:52', '13:59:52', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (4, '1.05', '03', '2025-09-03 14:00:00.000', 444, 3, '14:00:28', '14:00:30', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (5, '1.06', '03', '2025-09-03 14:00:00.000', 444, 3, '14:00:58', '14:00:59', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (6, '2.05', '03', '2025-09-03 14:14:00.000', 444, 0, '14:14:12', '14:14:12', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (7, '2.06', '03', '2025-09-03 14:14:00.000', 444, 15, '14:14:22', '14:14:26', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (8, '8.01', '03', '2025-09-02 20:08:00.000', 444, 6, '20:08:35', '0:00:00', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (9, '8.01', '04', '2025-09-02 20:10:00.000', 444, 14, '20:10:10', '0:00:00', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (10, '1.04', '16', '2025-09-03 11:02:00.000', 444, 0, '11:02:48', '11:02:48', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (11, '1.05', '16', '2025-09-03 11:02:00.000', 444, 0, '11:02:53', '11:02:53', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (12, '1.06', '16', '2025-09-03 11:02:00.000', 444, 0, '11:02:58', '11:02:58', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (13, '1.1', '16', '2025-09-03 13:40:00.000', 444, 0, '13:40:51', '13:40:51', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (14, '1.11', '16', '2025-09-03 13:40:00.000', 444, 0, '13:40:58', '13:40:58', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (15, '1.12', '16', '2025-09-03 13:40:00.000', 444, 0, '13:40:54', '13:40:54', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (16, '1.13', '16', '2025-09-03 13:41:00.000', 444, 0, '13:41:00', '13:41:00', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (17, '2.05', '16', '2025-09-03 11:03:00.000', 444, 0, '11:03:13', '11:03:13', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (18, '2.06', '16', '2025-09-03 11:03:00.000', 444, 0, '11:03:21', '11:03:22', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (19, '3.03', '16', '2025-09-03 13:03:00.000', 444, 0, '13:03:59', '13:07:20', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (20, '3.04', '16', '2025-09-03 13:04:00.000', 444, 990, '13:04:18', '13:04:20', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (21, '3.051', '16', '2025-09-03 13:03:00.000', 444, 0, '13:03:38', '13:03:43', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (22, '3.06', '16', '2025-09-03 13:33:00.000', 444, 1664, '13:33:35', '13:35:14', 1, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (23, '3.08', '16', '2025-09-03 11:03:00.000', 444, 0, '11:03:33', '11:03:34', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (24, '3.09', '16', '2025-09-03 11:03:00.000', 444, 0, '11:03:42', '11:03:44', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (25, '6.01', '16', '2025-09-03 13:35:00.000', 444, 0, '13:35:54', '13:35:54', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (26, '6.02', '16', '2025-09-03 13:36:00.000', 444, 0, '13:36:16', '13:36:18', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (27, '7.01', '16', '2025-09-03 13:04:00.000', 444, 0, '13:04:18', '13:04:38', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (28, '7.02', '16', '2025-09-03 13:12:00.000', 444, 0, '13:12:47', '13:12:48', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (29, '8.01', '16', '2025-09-02 14:38:00.000', 444, 0, '14:38:46', '0:00:00', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (30, '8.02', '16', '2025-09-03 13:35:00.000', 444, 0, '13:35:45', '13:35:45', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (31, '8.03', '16', '2025-09-03 13:36:00.000', 444, 363, '13:36:31', '13:36:37', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (32, '8.05', '16', '2025-09-03 13:40:00.000', 444, 211, '13:40:45', '13:40:47', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (33, '8.05', '16', '2025-09-03 13:40:00.000', 444, 211, '13:40:46', '13:40:47', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (34, '8.05', '16', '2025-09-03 13:40:00.000', 444, 211, '13:40:47', '13:40:47', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (35, '9.01', '16', '2025-09-03 13:42:00.000', 444, 312, '13:42:02', '13:42:34', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (36, '1.04', '17', '2025-09-02 12:32:00.000', 444, 0, '12:32:00', '12:32:00', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (37, '1.05', '17', '2025-09-02 12:32:00.000', 444, 7, '12:32:37', '12:32:44', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (38, '1.06', '17', '2025-09-02 12:33:00.000', 444, 7, '12:33:19', '12:33:20', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (39, '2.05', '17', '2025-09-02 14:54:00.000', 444, 0, '14:54:04', '14:54:04', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (40, '2.06', '17', '2025-09-02 14:54:00.000', 444, 35, '14:54:23', '14:54:38', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (41, '8.01', '17', '2025-09-03 09:25:00.000', 444, 5, '9:25:06', '0:00:00', 0, '380EE2');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (42, '1.04', '18', '2025-08-30 11:14:00.000', 444, 0, '11:14:07', '11:14:07', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (43, '1.05', '18', '2025-08-30 11:14:00.000', 444, 8, '11:14:25', '11:14:30', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (44, '1.06', '18', '2025-08-30 11:14:00.000', 444, 8, '11:14:42', '11:14:43', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (45, '2.05', '18', '2025-08-30 14:57:00.000', 444, 22, '14:57:48', '14:57:49', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (46, '2.06', '18', '2025-08-30 14:58:00.000', 444, 40, '14:57:59', '14:58:16', 0, '380EE3');
INSERT INTO dbadmin.log_file (log_id, pro_code, area_code, date_time, bill_cycle, no_of_recs, start_time, end_time, duration_min, user_id) VALUES (47, '8.01', '18', '2025-09-02 14:44:00.000', 444, 9, '14:44:01', '0:00:00', 0, '380EE2');

CREATE TABLE dbadmin.config (
    bill_cycle SMALLINT NOT NULL,
    area_code CHAR(2) NOT NULL,
    user_id CHAR(20) NOT NULL,
    entered_date DATETIME YEAR TO FRACTION(3),
    cycle_stat SMALLINT DEFAULT 1     //modified
);
INSERT INTO dbadmin.config VALUES (444, '01', '380EE2', '2025-08-28 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (444, '02', '380EE2', '2025-08-28 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (444, '03', '380EE3', '2025-08-28 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (444, '04', '380EE2', '2025-08-28 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (444, '16', '380EE2', '2025-08-30 00:00:00.000', 2);
INSERT INTO dbadmin.config VALUES (444, '17', '380EE3', '2025-08-25 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (444, '18', '380EE3', '2025-08-30 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (444, '57', '386EE46', '2025-08-30 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (445, '16', '380EE2', '2025-09-03 13:42:34.000', 1);
INSERT INTO dbadmin.config VALUES (444, '56', '380EE4', '2025-08-30 00:00:00.000', 1);
INSERT INTO dbadmin.config VALUES (444, '27', '380EE2', '2025-08-29 09:15:22.000', 1);
INSERT INTO dbadmin.config VALUES (444, '48', '380EE3', '2025-08-29 10:30:45.000', 1);
INSERT INTO dbadmin.config VALUES (444, '49', '380EE4', '2025-08-29 11:45:18.000', 1);
INSERT INTO dbadmin.config VALUES (444, '53', '380EE5', '2025-08-29 13:20:33.000', 1);
INSERT INTO dbadmin.config VALUES (444, '37', '380EE2', '2025-08-29 14:35:56.000', 1);
INSERT INTO dbadmin.config VALUES (444, '43', '380EE3', '2025-08-29 15:50:12.000', 1);
INSERT INTO dbadmin.config VALUES (444, '21', '380EE4', '2025-08-30 08:25:37.000', 1);
INSERT INTO dbadmin.config VALUES (444, '42', '380EE5', '2025-08-30 09:40:51.000', 1);
INSERT INTO dbadmin.config VALUES (444, '38', '380EE2', '2025-08-30 10:55:14.000', 1);
INSERT INTO dbadmin.config VALUES (444, '46', '380EE3', '2025-08-30 12:10:28.000', 1);
INSERT INTO dbadmin.config VALUES (444, '31', '380EE4', '2025-08-30 13:25:43.000', 1);
INSERT INTO dbadmin.config VALUES (444, '41', '380EE5', '2025-08-30 14:40:07.000', 1);
INSERT INTO dbadmin.config VALUES (444, '44', '380EE2', '2025-08-30 15:55:21.000', 1);
INSERT INTO dbadmin.config VALUES (444, '59', '380EE3', '2025-08-31 08:10:34.000', 1);
INSERT INTO dbadmin.config VALUES (444, '50', '380EE4', '2025-08-31 09:25:48.000', 1);
INSERT INTO dbadmin.config VALUES (444, '76', '380EE5', '2025-08-31 10:40:02.000', 1);
INSERT INTO dbadmin.config VALUES (444, '19', '380EE2', '2025-08-31 11:55:16.000', 1);
INSERT INTO dbadmin.config VALUES (444, '77', '380EE3', '2025-08-31 13:10:29.000', 1);
INSERT INTO dbadmin.config VALUES (444, '22', '380EE4', '2025-08-31 14:25:43.000', 1);
INSERT INTO dbadmin.config VALUES (444, '30', '380EE5', '2025-08-31 15:40:57.000', 1);
INSERT INTO dbadmin.config VALUES (444, '35', '380EE2', '2025-09-01 08:15:11.000', 1);
INSERT INTO dbadmin.config VALUES (444, '40', '380EE3', '2025-09-01 09:30:25.000', 1);
INSERT INTO dbadmin.config VALUES (444, '71', '380EE4', '2025-09-01 10:45:38.000', 1);
INSERT INTO dbadmin.config VALUES (444, '29', '380EE5', '2025-09-01 12:00:52.000', 1);
INSERT INTO dbadmin.config VALUES (444, '78', '380EE2', '2025-09-01 13:16:06.000', 1);
INSERT INTO dbadmin.config VALUES (444, '33', '380EE3', '2025-09-01 14:31:19.000', 1);
INSERT INTO dbadmin.config VALUES (444, '45', '380EE4', '2025-09-01 15:46:33.000', 1);
INSERT INTO dbadmin.config VALUES (444, '47', '380EE5', '2025-09-02 08:21:47.000', 1);
INSERT INTO dbadmin.config VALUES (444, '25', '380EE2', '2025-09-02 09:37:01.000', 1);
INSERT INTO dbadmin.config VALUES (444, '28', '380EE3', '2025-09-02 10:52:14.000', 1);
INSERT INTO dbadmin.config VALUES (444, '54', '380EE4', '2025-09-02 12:07:28.000', 1);
INSERT INTO dbadmin.config VALUES (444, '79', '380EE5', '2025-09-02 13:22:42.000', 1);
INSERT INTO dbadmin.config VALUES (444, '52', '380EE2', '2025-09-02 14:37:55.000', 1);
INSERT INTO dbadmin.config VALUES (444, '26', '380EE3', '2025-09-02 15:53:09.000', 1);
INSERT INTO dbadmin.config VALUES (444, '20', '380EE4', '2025-09-03 08:28:23.000', 1);
INSERT INTO dbadmin.config VALUES (444, '23', '380EE5', '2025-09-03 09:43:37.000', 1);
INSERT INTO dbadmin.config VALUES (444, '36', '380EE2', '2025-09-03 10:58:50.000', 1);
INSERT INTO dbadmin.config VALUES (444, '24', '380EE3', '2025-09-03 12:14:04.000', 1);
INSERT INTO dbadmin.config VALUES (444, '32', '380EE4', '2025-09-03 13:29:18.000', 1);
INSERT INTO dbadmin.config VALUES (444, '34', '380EE5', '2025-09-03 14:44:31.000', 1);
INSERT INTO dbadmin.config VALUES (444, '69', '380EE2', '2025-09-03 15:59:45.000', 1);
INSERT INTO dbadmin.config VALUES (444, '61', '380EE3', '2025-09-04 08:34:59.000', 1);
INSERT INTO dbadmin.config VALUES (444, '39', '380EE4', '2025-09-04 09:50:13.000', 1);
INSERT INTO dbadmin.config VALUES (444, '66', '380EE5', '2025-09-04 11:05:26.000', 1);
INSERT INTO dbadmin.config VALUES (444, '62', '380EE2', '2025-09-04 12:20:40.000', 1);
INSERT INTO dbadmin.config VALUES (444, '63', '380EE3', '2025-09-04 13:35:54.000', 1);
INSERT INTO dbadmin.config VALUES (444, '64', '380EE4', '2025-09-04 14:51:07.000', 1);
INSERT INTO dbadmin.config VALUES (444, '65', '380EE5', '2025-09-04 16:06:21.000', 1);
INSERT INTO dbadmin.config VALUES (444, '80', '380EE2', '2025-09-05 08:41:35.000', 1);
INSERT INTO dbadmin.config VALUES (444, '82', '380EE3', '2025-09-05 09:56:48.000', 1);
INSERT INTO dbadmin.config VALUES (444, '83', '380EE4', '2025-09-05 11:12:02.000', 1);
INSERT INTO dbadmin.config VALUES (444, '85', '380EE5', '2025-09-05 12:27:16.000', 1);
INSERT INTO dbadmin.config VALUES (444, '84', '380EE2', '2025-09-05 13:42:29.000', 1);
INSERT INTO dbadmin.config VALUES (444, '86', '380EE3', '2025-09-05 14:57:43.000', 1);
INSERT INTO dbadmin.config VALUES (444, '87', '380EE4', '2025-09-05 16:12:57.000', 1);
INSERT INTO dbadmin.config VALUES (444, '88', '380EE5', '2025-09-06 08:48:11.000', 1);
INSERT INTO dbadmin.config VALUES (444, '89', '380EE2', '2025-09-06 10:03:24.000', 1);
INSERT INTO dbadmin.config VALUES (444, '55', '380EE3', '2025-09-06 11:18:38.000', 1);
INSERT INTO dbadmin.config VALUES (444, '51', '380EE4', '2025-09-06 12:33:52.000', 1);
INSERT INTO dbadmin.config VALUES (444, '58', '380EE5', '2025-09-06 13:49:05.000', 1);
INSERT INTO dbadmin.config VALUES (444, '60', '380EE2', '2025-09-06 15:04:19.000', 1);

CREATE TABLE dbadmin.tmp_rdngs (
    tmp_rd_id SERIAL PRIMARY KEY,     //Modified
    acc_nbr CHAR(10),
    inst_id CHAR(8),
    area_cd CHAR(2),
    added_blcy CHAR(3),
    mtr_seq SMALLINT,
    mtr_type CHAR(6),
    prv_date DATE,
    rdng_date DATE,
    prsnt_rdn INTEGER,
    prv_rdn INTEGER,
    mtr_nbr CHAR(10),
    units INTEGER,
    rate DECIMAL(7,2),
    computed_chg DECIMAL(15,2),
    mnt_chg DECIMAL(15,2),
    acode CHAR(1),
    m_factor DECIMAL(8,3),
    bill_stat CHAR(1),
    err_stat SMALLINT,
    mtr_stat CHAR(1),
    rdn_stat CHAR(1),
    user_id CHAR(8),
    entered_dtime DATETIME YEAR TO FRACTION(3),
    edited_user_id CHAR(8),
    edited_dtime DATETIME YEAR TO FRACTION(3)
);
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100808', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19128505', 'KVA', 121, 1500.00, 181500.00, 181500.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100808', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19128505', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100808', '57', '444', 1, '2025-08-01', '2025-09-01', 1396026, 1421783, '19128505', 'KWD', 25757, 41.00, 1056037.00, 1056037.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100808', '57', '444', 1, '2025-08-01', '2025-09-01', 597525, 607906, '19128505', 'KWO', 10381, 31.00, 321811.00, 321811.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100808', '57', '444', 1, '2025-08-01', '2025-09-01', 502851, 512702, '19128505', 'KWP', 9851, 47.00, 462997.00, 462997.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100816', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19129220', 'KVA', 155, 1400.00, 217000.00, 217000.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100816', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19129220', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100816', '57', '444', 1, '2025-08-01', '2025-09-01', 1930140, 1957929, '19129220', 'KWD', 27789, 15.00, 416835.00, 416835.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100816', '57', '444', 1, '2025-08-01', '2025-09-01', 1002292, 1009320, '19129220', 'KWO', 7028, 12.00, 84336.00, 84336.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100816', '57', '444', 1, '2025-08-01', '2025-09-01', 443988, 445636, '19129220', 'KWP', 1648, 28.00, 46144.00, 46144.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100840', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19132469', 'KVA', 12, 1500.00, 18000.00, 18000.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100840', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19132469', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100840', '57', '444', 1, '2025-08-01', '2025-09-01', 67957, 69125, '19132469', 'KWD', 1168, 41.00, 47888.00, 47888.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100840', '57', '444', 1, '2025-08-01', '2025-09-01', 29135, 29677, '19132469', 'KWO', 542, 31.00, 16802.00, 16802.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100840', '57', '444', 1, '2025-08-01', '2025-09-01', 19520, 19846, '19132469', 'KWP', 326, 47.00, 15322.00, 15322.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100018', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '9102761', 'KVA', 61, 1400.00, 85400.00, 85400.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100018', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '9102761', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100018', '57', '444', 1, '2025-08-01', '2025-09-01', 469054, 469889, '9102761', 'KWD', 835, 15.00, 12525.00, 12525.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100018', '57', '444', 1, '2025-08-01', '2025-09-01', 78576, 78952, '9102761', 'KWO', 376, 12.00, 4512.00, 4512.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100018', '57', '444', 1, '2025-08-01', '2025-09-01', 45545, 45803, '9102761', 'KWP', 258, 28.00, 7224.00, 7224.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100026', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19128152', 'KVA', 0, 1500.00, 0.00, 0.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100026', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19128152', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100026', '57', '444', 1, '2025-08-01', '2025-09-01', 2666, 2666, '19128152', 'KWD', 0, 41.00, 0.00, 0.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100026', '57', '444', 1, '2025-08-01', '2025-09-01', 2387, 2387, '19128152', 'KWO', 0, 31.00, 0.00, 0.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100026', '57', '444', 1, '2025-08-01', '2025-09-01', 1396, 1396, '19128152', 'KWP', 0, 47.00, 0.00, 0.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100034', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '212564228', 'KVA', 0, 1400.00, 0.00, 0.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100034', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '212564228', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100034', '57', '444', 1, '2025-08-01', '2025-09-01', 37483, 37483, '212564228', 'KWD', 0, 15.00, 0.00, 0.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100034', '57', '444', 1, '2025-08-01', '2025-09-01', 4281, 4281, '212564228', 'KWO', 0, 12.00, 0.00, 0.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100034', '57', '444', 1, '2025-08-01', '2025-09-01', 2604, 2604, '212564228', 'KWP', 0, 28.00, 0.00, 0.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100050', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19133553', 'KVA', 36, 1500.00, 54000.00, 54000.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100050', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '19133553', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100050', '57', '444', 1, '2025-08-01', '2025-09-01', 408254, 417785, '19133553', 'KWD', 9531, 41.00, 390771.00, 390771.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100050', '57', '444', 1, '2025-08-01', '2025-09-01', 158370, 161902, '19133553', 'KWO', 3532, 31.00, 109492.00, 109492.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100050', '57', '444', 1, '2025-08-01', '2025-09-01', 98081, 100236, '19133553', 'KWP', 2155, 47.00, 101285.00, 101285.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100069', '57', '444', 1, '2025-08-01', '2025-09-01', 1479384, 1491183, '9102403', 'KWD', 11799, 41.00, 483759.00, 483759.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100069', '57', '444', 1, '2025-08-01', '2025-09-01', 473708, 477396, '9102403', 'KWO', 3688, 31.00, 114328.00, 114328.00, '', 1.000, '0', 128, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100069', '57', '444', 1, '2025-08-01', '2025-09-01', 541191, 544793, '9102403', 'KWP', 3602, 47.00, 169294.00, 169294.00, '', 1.000, '0', 0, '0', '0', 'CEB');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100085', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '210094139', 'KVA', 120, 1500.00, 180000.00, 180000.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100085', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '210094139', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100085', '57', '444', 1, '2025-08-01', '2025-09-01', 4125710, 4150261, '210094139', 'KWD', 24551, 41.00, 1006591.00, 1006591.00, '', 1.000, '0', 0, '4', '0', 'RRC');
INSERT INTO dbadmin.tmp_rdngs (acc_nbr, area_cd, added_blcy, mtr_seq, prv_date, rdng_date, prv_rdn, prsnt_rdn, mtr_nbr, mtr_type, units, rate, computed_chg, mnt_chg, acode, m_factor, bill_stat, err_stat, mtr_stat, rdn_stat, user_id) VALUES ('1870100069', '57', '444', 1, '2025-08-01', '2025-09-01', 0, 0, '9102403', 'KVAH', 0, 0.00, 0.00, 0.00, '', 0.000, '0', 0, '0', '0', 'CEB');

-- Create TMP_PAYMENTS table for temporary payment records
CREATE TABLE dbadmin.tmp_payments (
    id SERIAL PRIMARY KEY,
    agent_code CHAR(4),
    cent_code CHAR(3),
    acc_nbr CHAR(10),
    counter VARCHAR(3),
    lot CHAR(2),
    stub_no SMALLINT,
    actl_pay_date DATE,
    credit_date DATE,
    pay_mode CHAR(1),
    bank_code CHAR(4),
    branch_code CHAR(3),
    chq_no VARCHAR(8),
    bank_account VARCHAR(15),
    paid_amt DECIMAL(15, 2),
    up_date DATE,
    crnt_cycle CHAR(3)
);

-- Insert sample temporary payment data
INSERT INTO dbadmin.tmp_payments (agent_code, cent_code, acc_nbr, counter, lot, stub_no, actl_pay_date, credit_date, pay_mode, paid_amt) 
VALUES ('CEBC', 'QC0', '4170001370', 'CB', '', 95, '2022-07-22', '2022-07-22', 'Q', 110795.00);
INSERT INTO dbadmin.tmp_payments (agent_code, cent_code, acc_nbr, counter, lot, stub_no, actl_pay_date, credit_date, pay_mode, paid_amt) 
VALUES ('CEBC', 'QC0', '4170002253', 'CB', '', 110, '2022-07-22', '2022-07-22', 'Q', 104857.30);
INSERT INTO dbadmin.tmp_payments (agent_code, cent_code, acc_nbr, counter, lot, stub_no, actl_pay_date, credit_date, pay_mode, paid_amt) 
VALUES ('CEBC', 'QC0', '4170003489', 'CB', '', 111, '2022-07-22', '2022-07-22', 'Q', 506820.00);

CREATE TABLE testdb:dbadmin.amnd_types (
  amd_type CHAR(4),
  amd_desc VARCHAR(45,0),
  uptbl_name VARCHAR(25,0),
  field_name VARCHAR(15,0),
  nval_tblnm VARCHAR(25,0),
  nval_fldnm VARCHAR(15,0),
  dt_type CHAR(1),
  val_jrnl CHAR(1),
  amdt_status CHAR(1));

  INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('BFBL', 'Change of B/F Balance', 'MON_TOT', 'BF_BAL', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('AREA', 'Change of Area Code', 'CUSTOMER', 'AREA_CD', 'AREAS', 'AREA_NAME', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('MNBR', 'Meter Number', 'MTR_DETAIL', 'MTR_NBR', 'NOT DEFINED', ' ', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TRFC', 'Tariff Change', 'CUSTOMER', 'TARIFF', 'NOT DEFINED', ' ', 'C', '1', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CNTR', 'Augmentation', 'CUSTOMER', 'CNTR_DMND', 'RANGE_AMND', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('AGRN', 'Change of Agr.number', 'CUSTOMER', 'AGRMNT_NO', 'NOT DEFINED', ' ', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TRCB', 'Transformer/D Cable', 'INST_INFO', 'TR_CB', 'NOT DEFINED', ' ', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TMET', 'Type of metering', 'INST_INFO', 'TYPE_MET', 'RANGE_AMND', ' ', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CUST', 'Change cust status', 'CUSTOMER', 'CST_ST', 'CUST_STATUS', 'CST_DESC', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('LNST', 'Change Loan status', 'LOAN_MAS', 'LN_ST', 'NOT DEFINED', ' ', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('NOLN', 'Number of loans', 'CUSTOMER', 'NO_LOANS', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TSEC', 'Total security dep', 'CUSTOMER', 'TOT_SEC_DEP', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('WLKO', 'Change of Walk Order', 'CUSTOMER', 'WLK_ORD', 'NOT DEFINED', ' ', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('SDEP', 'Initial Deposit', 'CUSTOMER', 'DEPOSIT_AMT', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('LINS', 'Loan Instal. amount', 'LOAN_MAS', 'INST_AMT', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('LBAL', 'Loan Balance', 'LOAN_MAS', 'LN_BAL', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TVLT', 'Transformer Volts', 'INST_INFO', 'TRPNL_VOLT', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TVLT', 'Transformer Volts', 'INST_INFO', 'TRPNL_VOLT', 'NOT DEFINED', ' ', 'N', '0', 'I');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('EAMT', 'Amendment of Es. Amt', 'CUSTOMER', 'EST_AMNT', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('ADEP', 'Additional deposit', 'CUSTOMER', 'ADD_DEP_AMT', 'NOT DEFINED', ' ', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('ITYP', 'Change of Indutry', 'CUSTOMER', 'IND_TYPE', 'INDUSTRY', 'IND_DESC', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TLNO', 'Change of Telephone', 'CUSTOMER', 'TEL_NBR', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('FRFC', 'SSSSSSSSSSS', 'CUSTOMER', 'TARIFF', 'NOT DEFINED', '', 'C', '1', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('IDDT', 'Initial Deposit Date', 'CUSTOMER', 'DEP_DATE', 'NOT DEFINED', '', 'D', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('DPIV', 'Initial Deposit PIV', 'CUSTOMER', 'DEP_PIV_NBR', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CCAT', 'Change of category', 'CUSTOMER', 'CUS_CAT', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TELN', 'change og telephone', 'CUSTOMER', 'TEL_NBR', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('GSTE', 'Exemption of GST', 'CUSTOMER', 'GST_APL', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('VATE', 'Tax Customer', 'CUSTOMER', 'TAX_INV', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CGST', 'GST Change', 'CUSTOMER', 'GST_APL', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CAUT', 'Change Auth. Letter', 'CUSTOMER', 'AUTH_LETTER', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('TAXN', 'VAT Number', 'CUSTOMER', 'TAX_NUM', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CCCD', 'Change Customer Code', 'CUSTOMER', 'CUST_CD', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CTYP', 'Change Customer Type', 'CUSTOMER', 'CUST_TYPE', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CNET', 'Change Net Type', 'CUSTOMER', 'NET_TYPE', 'NOT DEFINED', ' ', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CCCT', 'Change of Category Code', 'CUSTOMER', 'CAT_CODE', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('SOFF', 'Change setoff status of Net Plus', 'NETMETER', 'SETOFF', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CSCH', 'Change of scheme', 'NETMETER', 'SCHM', 'NOT DEFINED', '', 'C', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CCAP', 'Change of Generation Capacity', 'NETMETER', 'GEN_CAP', 'NOT DEFINED', '', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CRAT', 'Change of rate', 'NETMETER', 'RATE1', 'NOT DEFINED', '', 'N', '0', 'A');

INSERT INTO dbadmin.amnd_types
(amd_type, amd_desc, uptbl_name, field_name, nval_tblnm, nval_fldnm, dt_type, val_jrnl, amdt_status)
VALUES
('CAGR', 'Change Agreement Date', 'NETMETER', 'AGRMNT_DATE', 'NOT DEFINED', '', 'D', '0', 'A');

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