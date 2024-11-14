INSERT INTO employees (first_name, last_name, department, position, salary)
VALUES
-- IT Department with Developers
('Jane', 'Doe', 'IT', 'Developer', 75000.00),
('Mike', 'Johnson', 'IT', 'Developer', 72000.00),
('John', 'Smith', 'IT', 'Senior Developer', 95000.00),

-- Engineering Department (exactly 4 engineers)
('Emily', 'Davis', 'Engineering', 'Engineer', 98000.00),
('David', 'Miller', 'Engineering', 'Engineer', 78000.00),
('Lisa', 'Wilson', 'Engineering', 'Engineer', 108000.00),
('Michael', 'Taylor', 'Engineering', 'Engineer', 76000.00),

-- Managers across departments (exactly one in each required department)
('Robert', 'Brown', 'HR', 'Manager', 85000.00),
('Kevin', 'Lee', 'Marketing', 'Manager', 88000.00),
('Laura', 'Hall', 'Sales', 'Manager', 86000.00),
('Steven', 'King', 'Operations', 'Manager', 90000.00),

-- Smith family members (exactly 4)
-- Note: John Smith is already added in IT department
('Thomas', 'Smith', 'Marketing', 'Marketing Specialist', 62000.00),
('Anna', 'Smith', 'Sales', 'Sales Representative', 58000.00),
('Robert', 'Smith', 'Customer Support', 'Support Specialist', 54000.00),

-- John variations (exactly 2)
-- Note: John Smith is already added
('Johnny', 'Wilson', 'Sales', 'Representative', 86000.00),

-- Additional employees to maintain diversity
('William', 'White', 'HR', 'HR Coordinator', 55000.00),
('Patricia', 'Martinez', 'Finance', 'Financial Analyst', 78000.00),
('Elizabeth', 'Robinson', 'Finance', 'Accountant', 68000.00);