# Object Oriented Software Development Lab - Final Mini Project Report

**Project Title:** Hotel Management System  
**Platform:** Java Desktop Application using JavaFX  
**Build Tool:** Maven  

## 1. Introduction

The Hotel Management System is a desktop-based application developed using Java and JavaFX to automate core hotel operations such as room management, booking, checkout, reporting, billing, and persistence. The project was designed as an Object Oriented Software Development Lab mini project and demonstrates both fundamental and advanced Java concepts in a realistic hotel management scenario.

The application provides a tab-based graphical user interface for managing room records, customer bookings, reports, and billing information. It also supports multiple persistence mechanisms including text files, object serialization, and JDBC-based SQLite storage. By combining GUI development with object-oriented design, collections, multithreading, synchronization, and file/database handling, the project aligns well with the OSDL lab manual progression.

## 2. Objectives

- To design and implement a modular hotel management application.
- To apply object-oriented programming concepts such as encapsulation, inheritance, abstraction, interfaces, and polymorphism.
- To build a user-friendly JavaFX interface using layouts, controls, events, and tab-based navigation.
- To implement permanent storage using text files, serialization, and JDBC.
- To demonstrate multithreading and synchronization in booking and checkout operations.
- To provide reporting and billing support for hotel operations.
- To use Maven for dependency management and project build execution.

## 3. System Overview

The system is organized into multiple functional modules:

### Room Management

This module allows the user to add rooms, maintain room type and price, and display all rooms along with their booking status. The system supports standard and deluxe room categories.

### Booking Management

This module handles customer room booking and checkout operations using room number and date selection. It updates room availability automatically and prevents invalid or duplicate operations.

### Billing Management

This module generates invoice records for successful bookings, tracks active and closed bills, calculates room charge, service charge, total payable amount, and displays invoice details in a separate billing dashboard.

### Reports and Analytics

This module provides a room allocation report and summary statistics such as total rooms, occupied rooms, available rooms, active bookings, and occupancy rate.

### Persistence Management

The application supports:

- Text-based file storage
- Object serialization/deserialization
- JDBC-based SQLite storage

This ensures that the application can save and restore hotel data beyond runtime memory.

## 4. Technologies Used

- **Java** - Core and advanced programming concepts
- **JavaFX** - GUI creation using controls, layouts, scenes, tabs, events, and FXML
- **FXML** - Scene Builder-compatible UI definition for the billing module
- **Maven** - Dependency and build management
- **SQLite JDBC** - Database persistence using JDBC
- **File Handling and Serialization** - Permanent object/data storage
- **CSS** - Styling of the JavaFX interface

## 5. System Architecture

The project follows a modular layered design:

- **UI Layer:** JavaFX screens created in `MainApp.java` and `billing-view.fxml`
- **Controller Layer:** `BillingController.java` manages the FXML-based billing view
- **Service Layer:** `BookingSystem.java` contains the main business logic for rooms, bookings, reports, and billing flow
- **Persistence Layer:** `DatabaseService.java` handles JDBC storage; file and serialization logic are also present in `BookingSystem.java`
- **Model Layer:** `Room`, `StandardRoom`, `DeluxeRoom`, `BillingRecord`, `RoomType`, and `Pair`

This architecture separates interface concerns from data and processing logic, making the application easier to maintain and extend.

## 6. OSDL Concepts Implemented

The project implements the following concepts from the OSDL lab manual:

### Week 1: OOP Concepts

- **Encapsulation:** Private fields with controlled access through methods in `Room`, `BillingRecord`, and other classes.
- **Inheritance:** `StandardRoom` and `DeluxeRoom` extend the abstract base class `Room`.
- **Polymorphism:** `calculatePrice()` behaves differently depending on the actual room subclass object.
- **Abstraction:** `Room` is defined as an abstract class.
- **Interface-based design:** `Amenities` is implemented by room-related classes through the abstract room hierarchy.

### Week 2: Wrapper Classes and Enumeration

- **Enum usage:** `RoomType` is implemented as an enumeration with constants such as `STANDARD` and `DELUXE`.
- **Wrapper classes/autoboxing:** The application uses wrapper objects like `Integer`, `Double`, and `Boolean` throughout its models and business logic.

### Week 3: Multithreaded Programming

- **Thread creation:** Booking and checkout tasks are handled asynchronously using `Thread`.
- **Sleep simulation:** `Thread.sleep()` is used to simulate processing delay and room-cleaning delay.
- **Concurrent execution:** Booking and checkout actions are processed without freezing the GUI.

### Week 4: Synchronization

- **Synchronized methods:** Shared booking and room data are protected using `synchronized` methods in `BookingSystem`.
- **Data consistency:** This prevents race conditions when multiple threads access shared hotel data.

### Week 5: Input/Output Streams

- **Character streams:** Text file saving/loading is implemented using `FileWriter`, `FileReader`, and `BufferedReader`.

### Week 6: Serialization and Deserialization

- **Serializable objects:** `Room` implements `Serializable`.
- **Serialization:** Rooms are stored using `ObjectOutputStream`.
- **Deserialization:** Stored room objects are read back using `ObjectInputStream`.

### Week 7: Generics

- **Generic class:** `Pair<T, U>` is implemented and used to associate room numbers with guest names and booking dates.
- **Generic method:** `displayValue()` demonstrates generic method usage.

### Week 8: Collection Framework

- **ArrayList:** Used to store rooms and billing records.
- **HashMap:** Used to map room numbers to customer details and booking dates.
- **Iterator:** Used to traverse room collections for display and report generation.

### Week 9 and Week 10: JavaFX GUI and Final Application

- **Stage and Scene:** The GUI is launched through JavaFX `Application`, `Stage`, and `Scene`.
- **Controls:** `Label`, `TextField`, `Button`, `ComboBox`, `DatePicker`, `ListView`, `TableView`, and `TabPane` are used.
- **Layouts:** `VBox`, `HBox`, `GridPane`, and `ScrollPane` are used for structured screen design.
- **Event Handling:** Buttons use `setOnAction()` handlers for all user operations.
- **FXML usage:** The billing dashboard is loaded with `FXMLLoader`, making it compatible with Scene Builder.

### Additional Advanced Feature

- **JDBC persistence:** Although the week-wise manual does not require a database for the final basic app, this project includes JDBC-based SQLite persistence as an additional feature.

## 7. Working of the System

The application works in the following sequence:

1. The user launches the JavaFX application.
2. The dashboard screen opens with navigation tabs for home, rooms, customers, reports, and billing.
3. The user can add room details such as room number, room type, price, and availability.
4. The user can book a room by entering customer details and selecting check-in and checkout dates.
5. Booking runs asynchronously in a background thread and updates room status.
6. A billing record is automatically generated for successful bookings.
7. During checkout, the booking is closed, the room is released, and the related bill is marked closed.
8. The reports section displays room allocation and occupancy analytics.
9. The user can persist the application state using text, serialization, or JDBC storage.
10. The user can reload persisted data in future sessions.

## 8. Files and Major Components

- `MainApp.java` - Main JavaFX application and primary UI logic
- `BookingSystem.java` - Core room, booking, report, synchronization, file, and serialization logic
- `DatabaseService.java` - JDBC save/load support using SQLite
- `BillingRecord.java` - Billing model and invoice calculations
- `BillingController.java` - Controller for the billing FXML screen
- `billing-view.fxml` - FXML-based billing interface
- `styles.css` - JavaFX CSS styling
- `Room.java`, `StandardRoom.java`, `DeluxeRoom.java`, `RoomType.java`, `Amenities.java`, `Pair.java` - Supporting model and utility classes
- `pom.xml` - Maven configuration file

## 9. Advantages

- User-friendly tab-based interface
- Good coverage of OSDL lab-manual concepts
- Modular object-oriented design
- Supports multiple persistence techniques
- Includes billing and reporting features
- Demonstrates multithreading without freezing the GUI
- Maven-based project structure improves build and dependency management
- FXML support makes the project closer to real JavaFX development practices

## 10. Limitations

- It is a desktop-only application and not web-based
- It is intended for a single-user local workflow
- Customer management is integrated into booking flow rather than being a separate full-profile module
- Payment processing is not implemented as a dedicated standalone module
- The concurrency model demonstrates threads and synchronization, but not full wait/notify-based booking queues

## 11. Conclusion

The Hotel Management System successfully demonstrates the practical application of major Object Oriented Software Development Lab concepts in a single integrated project. It combines Java OOP principles, collections, generics, multithreading, synchronization, file handling, serialization, JavaFX GUI development, FXML, Maven, and JDBC persistence in a realistic desktop application.

The project is not only suitable as a final mini project submission but also provides a solid foundation for further enhancement into a larger real-world hotel operations system.
