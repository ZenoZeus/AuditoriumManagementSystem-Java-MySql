-- drop database auditest;


create DATABASE auditorium_booking;
use auditorium_booking;
drop database auditorium_booking;

-- Create the users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('admin', 'user') NOT NULL,
    phone VARCHAR(15)
);

-- Create the bookings table
CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    status ENUM('Pending', 'Booked', 'Completed', 'Cancelled') NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15)
);
select * from users;
INSERT INTO users (username, password, role, phone)
VALUES ('admin', 'admin123', 'admin', '123-456-7890');
select * from bookings;
