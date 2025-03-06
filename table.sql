use module5;
-- nhân viên
CREATE TABLE Employee (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fullName VARCHAR(200) NOT NULL,
    dateOfBirth DATE,
    address VARCHAR(500),
    phoneNumber VARCHAR(20) UNIQUE,
     email VARCHAR(200) UNIQUE,
    username VARCHAR(200) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'SalesStaff', 'BusinessStaff', 'WarehouseStaff') NOT NULL
);
-- khách hàng
create table customer (
id int primary key auto_increment,
customerName varchar (200) not null,
phoneNumber VARCHAR(20) UNIQUE,
address VARCHAR(500),
dateOfBirth DATE,
  email VARCHAR(200) UNIQUE
);
-- nhà cung cấp
CREATE TABLE Supplier (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    phoneNumber VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(200) UNIQUE
);
-- sản phẩm
CREATE TABLE Product (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    retailPrice INT NOT NULL,
    imageURL VARCHAR(500),
    stockQuantity INT NOT NULL,
    supplier_id INT,
    screenSize VARCHAR(50),
    rearCamera VARCHAR(100),
    frontCamera VARCHAR(100),
    cpu VARCHAR(50),
    storage VARCHAR(50),
    description TEXT,
      qrCode VARCHAR(255) UNIQUE,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(id) ON DELETE SET NULL
);
-- quản lý bán hàng
create table Sales(
id INT PRIMARY KEY AUTO_INCREMENT,
    saleDate DATE NOT NULL,
    totalAmount INT NOT NULL,
    customer_id INT,
    employee_id INT,
    paymentMethod ENUM('Cash', 'Bank Transfer', 'Credit Card') NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(id) ON DELETE SET NULL,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE SET NULL
);
-- chi tiết  đơn hàng
CREATE TABLE SalesDetails (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sales_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unitPrice INT NOT NULL,
    FOREIGN KEY (sales_id) REFERENCES Sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);
-- chi tiết nhập kho
CREATE TABLE StockIn (
    employee_id INT,
    id INT PRIMARY KEY AUTO_INCREMENT,
    stockInDate DATE NOT NULL,
    supplier_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unitPrice INT NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(id) ON DELETE SET NULL,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);
-- chi tiets xuất kho
CREATE TABLE StockOut (
    employee_id INT,
    id INT PRIMARY KEY AUTO_INCREMENT,
    stockOutDate DATE NOT NULL,
    product_id INT,
    quantity INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);