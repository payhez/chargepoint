CREATE TABLE driver (
  driverIdentifier VARCHAR(80) NOT NULL,
  credit DECIMAL(10,2),
  name VARCHAR(255),
  surname VARCHAR(255),
  phoneNumber VARCHAR(255),
  PRIMARY KEY (driverIdentifier),
  CHECK (CHAR_LENGTH(driverIdentifier) >= 20)
);

-- Insert six sample records:
INSERT INTO driver (driverIdentifier, credit, name, surname, phoneNumber) VALUES
('12345678901234567890', 100.50, 'John', 'Doe', '123456789'),
('22345678901234567890', 200.75, 'Jane', 'Smith', '987654321'),
('32345678901234567890', 300.00, 'Bob', 'Brown', '555555555'),
('42345678901234567890', 150.25, 'Alice', 'Green', '444444444'),
('52345678901234567890', 250.00, 'Charlie', 'Black', '333333333'),
('62345678901234567890', 350.60, 'Diana', 'White', '222222222');
